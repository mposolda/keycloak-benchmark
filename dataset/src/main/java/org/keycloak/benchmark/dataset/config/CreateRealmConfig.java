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

package org.keycloak.benchmark.dataset.config;

import java.lang.reflect.Field;

import org.jboss.resteasy.spi.HttpRequest;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CreateRealmConfig implements Config {

    @QueryParamFill(paramName = "realm-prefix", defaultValue = DatasetConstants.DEFAULT_REALM_PREFIX) private String realmPrefix;

    @QueryParamIntFill(paramName = "start", defaultValue = DatasetConstants.DEFAULT_REALM_START) private Integer start;

    @QueryParamIntFill(paramName = "count", defaultValue = -1, required = true) private Integer count;

    @QueryParamFill(paramName = "realm-role-prefix", defaultValue = DatasetConstants.DEFAULT_REALM_ROLE_PREFIX) private String realmRolePrefix;

    @QueryParamIntFill(paramName = "realm-roles-per-realm", defaultValue = DatasetConstants.DEFAULT_REALM_ROLES_PER_REALM) private Integer realmRolesPerRealm;

    @QueryParamFill(paramName = "client-prefix", defaultValue = DatasetConstants.DEFAULT_CLIENT_PREFIX) private String clientPrefix;

    @QueryParamIntFill(paramName = "clients-per-realm", defaultValue = DatasetConstants.DEFAULT_CLIENTS_PER_REALM) private Integer clientsPerRealm;

    @QueryParamFill(paramName = "client-role-prefix", defaultValue = DatasetConstants.DEFAULT_CLIENT_ROLE_PREFIX) private String clientRolePrefix;

    @QueryParamIntFill(paramName = "client-roles-per-client", defaultValue = DatasetConstants.DEFAULT_CLIENT_ROLES_PER_CLIENT) private Integer clientRolesPerClient;

    @QueryParamFill(paramName = "group-prefix", defaultValue = DatasetConstants.DEFAULT_GROUPS_PREFIX) private String groupPrefix;

    @QueryParamIntFill(paramName = "groups-per-realm", defaultValue = DatasetConstants.DEFAULT_GROUPS_PER_REALM) private Integer groupsPerRealm;

    @QueryParamFill(paramName = "user-prefix", defaultValue = DatasetConstants.DEFAULT_USERS_PREFIX) private String userPrefix;

    @QueryParamIntFill(paramName = "users-per-realm", defaultValue = DatasetConstants.DEFAULT_USERS_PER_REALM) private Integer usersPerRealm;

    @QueryParamIntFill(paramName = "groups-per-user", defaultValue = DatasetConstants.DEFAULT_GROUPS_PER_USER) private Integer groupsPerUser;

    @QueryParamIntFill(paramName = "realm-roles-per-user", defaultValue = DatasetConstants.DEFAULT_REALM_ROLES_PER_USER) private Integer realmRolesPerUser;

    @QueryParamIntFill(paramName = "client-roles-per-user", defaultValue = DatasetConstants.DEFAULT_CLIENT_ROLES_PER_USER) private Integer clientRolesPerUser;

    @QueryParamIntFill(paramName = "password-hash-iterations", defaultValue = DatasetConstants.DEFAULT_HASH_ITERATIONS) private Integer passwordHashIterations;


    public void autoFillFromQueryParams(HttpRequest httpRequest) {
        for (Field f : getClass().getDeclaredFields()) {
            QueryParamFill qpf = f.getAnnotation(QueryParamFill.class);
            if (qpf != null) {
                String val = httpRequest.getUri().getQueryParameters().getFirst(qpf.paramName());
                if (val == null) {
                    if (qpf.required()) {
                        throw new IllegalStateException("Required parameter " + qpf.paramName() + " not present");
                    }
                    val = qpf.defaultValue();
                }
                f.setAccessible(true);
                try {
                    f.set(this, val);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fill the field " + qpf.paramName());
                }
            }

            QueryParamIntFill qpfInt = f.getAnnotation(QueryParamIntFill.class);
            if (qpfInt != null) {
                String valStr = httpRequest.getUri().getQueryParameters().getFirst(qpf.paramName());
                Integer val;
                if (valStr == null) {
                    if (qpfInt.required()) {
                        throw new IllegalStateException("Required parameter " + qpfInt.paramName() + " not present");
                    }
                    val = qpfInt.defaultValue();
                } else {
                    val = Integer.parseInt(valStr);
                }
                f.setAccessible(true);
                try {
                    f.set(this, val);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fill the field " + qpfInt.paramName());
                }
            }
        }
    }



    @Override
    public String toString() {
        return new StringBuilder("CreateRealm [ ")
                .append("\n realm-prefix = " + realmPrefix)
                .append("\n start; = " + start)
                .append("\n count = " + count)
                .append("\n realm-role-prefix = " + realmRolePrefix)
                .append("\n realm-roles-per-realm = " + realmRolesPerRealm)
                .append("\n client-prefix = " + clientPrefix)
                .append("\n clients-per-realm = " + clientsPerRealm)
                .append("\n client-role-prefix = " + clientRolePrefix)
                .append("\n client-roles-per-client = " + clientRolesPerClient)
                .append("\n group-prefix = " + groupPrefix)
                .append("\n groups-per-realm = " + groupsPerRealm)
                .append("\n user-prefix = " + userPrefix)
                .append("\n users-per-realm = " + usersPerRealm)
                .append("\n groups-per-user = " + groupsPerUser)
                .append("\n realm-roles-per-user = " + realmRolesPerUser)
                .append("\n client-roles-per-user = " + clientRolesPerUser)
                .append("\n password-hash-iterations = " + passwordHashIterations)
                .append("\n]")
                .toString();
    }
}
