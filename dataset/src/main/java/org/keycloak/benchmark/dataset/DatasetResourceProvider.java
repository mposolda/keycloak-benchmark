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

import java.util.Arrays;
import java.util.Collections;
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
        CreateRealmContext context = new CreateRealmContext(config);

        createAndSetRealm(context, startIndex, session);
        createRealmRoles(context);
        createClients(context, session);
        createGroups(context);
        createUsers(context, session);

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

    private void createUsers(CreateRealmContext context, KeycloakSession session) {
        RealmModel realm = context.getRealm();
        CreateRealmConfig config = context.getConfig();

        for (int i = 0; i < config.getUsersPerRealm(); i++) {
            String username = config.getUserPrefix() + i;
            UserModel user = session.users().addUser(realm, username);
            user.setEnabled(true);
            user.setFirstName(username + "-first");
            user.setLastName(username + "-last");
            user.setEmail(username + String.format("@%s.com", realm.getName()));

            // Set<String> emptySet = Collections.emptySet();

            // UserResource.updateUserFromRep(user, rep, emptySet, realm, session, false);

            // TODO:mposolda finish...
//            RepresentationToModel.createFederatedIdentities(rep, session, realm, user);
//            RepresentationToModel.createGroups(rep, realm, user);
//            RepresentationToModel.createCredentials(rep, session, realm, user, true);
            String password = String.format("%s-password", username);
            session.userCredentialManager().updateCredential(realm, user, UserCredentialModel.password(password, false));


            // TODO:mposolda finish...
//
//            List<RoleModel> roles = new ArrayList(realm.getRoles());
//
//            Collections.shuffle(roles);
//
//            for (RoleModel role : roles.subList(0, 4)) {
//                if (role.getName().startsWith("role-")) {
//                    user.grantRole(role);
//                }
//            }
//
//            List<ClientModel> clients = new ArrayList<>(realm.getClients());
//
//            Collections.shuffle(clients);
//
//            for (ClientModel client : clients.subList(0, 4)) {
//                if (client.getClientId().startsWith("client-")) {
//                    List<RoleModel> clientRoles = new ArrayList(client.getRoles());
//
//                    Collections.shuffle(clientRoles);
//
//                    for (RoleModel role : clientRoles.subList(0, 4)) {
//                        if (role.getName().startsWith("client")) {
//                            user.grantRole(role);
//                        }
//                    }
//                }
//            }
        }
    }

}
