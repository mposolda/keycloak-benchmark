/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.keycloak.benchmark.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.benchmark.dataset.config.ConfigUtil;
import org.keycloak.benchmark.dataset.config.CreateRealmConfig;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.managers.ClientManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DatasetResourceProvider implements RealmResourceProvider {

    protected static final Logger logger = Logger.getLogger(DatasetResourceProvider.class);

    private final KeycloakSession session;

    @Context
    private HttpRequest httpRequest;

    public DatasetResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return this;
    }


    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRealms(
            @QueryParam("realm-prefix") String realmPrefix,
            @QueryParam("count") Integer count, // NOTE: Start index is not available as parameter as it will be "auto-detected" based on already created realms
            @QueryParam("realm-role-prefix") String realmRolePrefix,
            @QueryParam("realm-roles-per-realm") Integer realmRolesPerRealm,
            @QueryParam("client-prefix") String clientPrefix,
            @QueryParam("clients-per-realm") Integer clientsPerRealm,
            @QueryParam("client-role-prefix") String clientRolePrefix,
            @QueryParam("client-roles-per-client") Integer clientRolesPerClient,
            @QueryParam("group-prefix") String groupPrefix,
            @QueryParam("groups-per-realm") Integer groupsPerRealm,
            @QueryParam("user-prefix") String userPrefix,
            @QueryParam("users-per-realm") String usersPerRealm,
            @QueryParam("groups-per-user") String groupsPerUser,
            @QueryParam("realm-roles-per-user") String realmRolesPerUser,
            @QueryParam("client-roles-per-user") String clientRolesPerUser,
            @QueryParam("password-hash-iterations") Integer hashIterations
            ) {
        CreateRealmConfig config = ConfigUtil.createConfigFromQueryParams(httpRequest, CreateRealmConfig.class);

        int startIndex = ConfigUtil.findFreeEntityIndex(index -> {
            String realmName = config.getRealmPrefix() + index;
            return session.realms().getRealmByName(realmName) != null;
        });
        config.setStart(startIndex);
        logger.infof("Will start creating realms from %s", config.getRealmPrefix() + startIndex);
        logger.infof("Trigger creating realms with the configuration: %s", config);
        TimerLogger timerLogger = TimerLogger.start("Start creation of realm " + config.getRealmPrefix() + startIndex);

        CreateRealmContext context = new CreateRealmContext(config);

        createAndSetRealm(context, startIndex, session);
        // TODO:mposolda debug
        timerLogger.info(logger, "Created realm %s", context.getRealm().getName());

        createRealmRoles(context);
        // TODO:mposolda debug
        timerLogger.info(logger, "Created %d roles in realm %s", context.getRealmRoles().size(), context.getRealm().getName());

        createClients(context, session);
        // TODO:mposolda debug
        timerLogger.info(logger, "Created %d clients in realm %s", context.getClients().size(), context.getRealm().getName());

        createGroups(context);
        // TODO:mposolda debug
        timerLogger.info(logger, "Created %d groups in realm %s", context.getGroups().size(), context.getRealm().getName());

        createUsers(context, timerLogger, session);

        timerLogger.info(logger, "Created %d users in realm %s", context.getUsers().size(), context.getRealm().getName());

        return Response.ok("{ \"status\": \"OK\" }").build();
    }

    @Override
    public void close() {
    }


    private void createAndSetRealm(CreateRealmContext context, int index, KeycloakSession session) {
        CreateRealmConfig config = context.getConfig();

        RealmManager realmManager = new RealmManager(session);
        RealmRepresentation rep = new RealmRepresentation();

        String realmName = config.getRealmPrefix() + index;
        rep.setRealm(realmName);
        rep.setId(realmName);
        RealmModel realm = realmManager.importRealm(rep);

        realm.setEnabled(true);
        realm.setRegistrationAllowed(true);
        realm.setAccessCodeLifespan(60);
        realm.setPasswordPolicy(PasswordPolicy.parse(session, "hashIterations(" + config.getPasswordHashIterations() + ")"));

        session.getContext().setRealm(realm);
        context.setRealm(realm);
    }

    private void createRealmRoles(CreateRealmContext context) {
        RealmModel realm = context.getRealm();

        for (int i = 0; i < context.getConfig().getRealmRolesPerRealm(); i++) {
            String roleName = context.getConfig().getRealmRolePrefix() + i;
            RoleModel role = realm.addRole(roleName);
            context.realmRoleCreated(role);
        }
    }

    private void createClients(CreateRealmContext context, KeycloakSession session) {
        RealmModel realm = context.getRealm();
        CreateRealmConfig config = context.getConfig();

        for (int i = 0; i < config.getClientsPerRealm(); i++) {
            ClientRepresentation client = new ClientRepresentation();

            String clientId = config.getClientPrefix() + i;
            client.setClientId(clientId);
            client.setName(clientId);
            client.setEnabled(true);
            client.setServiceAccountsEnabled(true);
            client.setDirectAccessGrantsEnabled(true);
            client.setSecret(clientId.concat("-secret"));
            client.setRedirectUris(Arrays.asList("*"));
            client.setPublicClient(false);
            client.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);

            ClientModel model = ClientManager.createClient(session, realm, client, true);
            context.clientCreated(model);

            for (int k = 0; k < config.getClientRolesPerClient() ; k++) {
                String roleName = clientId + "-" + config.getClientRolePrefix() + k;
                RoleModel role = model.addRole(roleName);
                context.clientRoleCreated(model, role);
            }
        }
    }

    private void createGroups(CreateRealmContext context) {
        RealmModel realm = context.getRealm();

        for (int i = 0; i < context.getConfig().getGroupsPerRealm(); i++) {
            String groupName = context.getConfig().getGroupPrefix() + i;
            GroupModel group = realm.createGroup(groupName);
            context.groupCreated(group);
        }
    }

    private void createUsers(CreateRealmContext context, TimerLogger timerLogger, KeycloakSession session) {
        RealmModel realm = context.getRealm();
        CreateRealmConfig config = context.getConfig();

        for (int i = 0; i < config.getUsersPerRealm(); i++) {
            String username = config.getUserPrefix() + i;
            UserModel user = session.users().addUser(realm, username);
            user.setEnabled(true);
            user.setFirstName(username + "-first");
            user.setLastName(username + "-last");
            user.setEmail(username + String.format("@%s.com", realm.getName()));

            String password = String.format("%s-password", username);
            session.userCredentialManager().updateCredential(realm, user, UserCredentialModel.password(password, false));

            // Detect which roles we assign to the user
            int roleIndexStartForCurrentUser = (i * config.getRealmRolesPerUser());
            for (int j = roleIndexStartForCurrentUser ; j < roleIndexStartForCurrentUser + config.getRealmRolesPerUser() ; j++) {
                int roleIndex = j % config.getRealmRolesPerRealm();
                user.grantRole(context.getRealmRoles().get(roleIndex));
                logger.tracef("Assigned role %s to the user %s", context.getRealmRoles().get(roleIndex).getName(), user.getUsername());
            }

            int clientRolesTotal = context.getClientRoles().size();
            int clientRoleIndexStartForCurrentUser = (i * config.getClientRolesPerUser());
            for (int j = clientRoleIndexStartForCurrentUser ; j < clientRoleIndexStartForCurrentUser + config.getClientRolesPerUser() ; j++) {
                int roleIndex = j % clientRolesTotal;
                user.grantRole(context.getClientRoles().get(roleIndex));
                logger.tracef("Assigned role %s to the user %s", context.getClientRoles().get(roleIndex).getName(), user.getUsername());
            }

            // Detect which groups we assign to the user
            int groupIndexStartForCurrentUser = (i * config.getGroupsPerUser());
            for (int j = groupIndexStartForCurrentUser ; j < groupIndexStartForCurrentUser + config.getGroupsPerUser() ; j++) {
                int groupIndex = j % config.getGroupsPerRealm();
                user.joinGroup(context.getGroups().get(groupIndex));
                logger.tracef("Assigned group %s to the user %s", context.getGroups().get(groupIndex).getName(), user.getUsername());
            }

            context.userCreated(user);
            if ((i + 1) % 10 == 0) {
                timerLogger.info(logger, "Created %d users in realm %s", context.getUsers().size(), context.getRealm().getName());
            }
        }
    }

}
