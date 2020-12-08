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
import org.keycloak.models.KeycloakSession;
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
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
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
        logger.infof("Trigger creating realms with the configuration: %s", config);

        return Response.ok("{ \"status\": \"OK\" }").build();
    }

    @Override
    public void close() {
    }
}
