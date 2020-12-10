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

import static org.keycloak.benchmark.dataset.config.DatasetOperation.CREATE_CLIENTS;
import static org.keycloak.benchmark.dataset.config.DatasetOperation.CREATE_REALMS;
import static org.keycloak.benchmark.dataset.config.DatasetOperation.CREATE_USERS;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DatasetConfig implements Config {

    // Used when creating many realms as a prefix. For example when prefix us "foo", we will create realms like "foo0", "foo1" etc.
    @QueryParamFill(paramName = "realm-prefix", defaultValue = DatasetConstants.DEFAULT_REALM_PREFIX, operations = { CREATE_REALMS })
    private String realmPrefix;

    // Realm-name is required when creating many clients or users. The realm where clients/users will be created must already exists
    @QueryParamFill(paramName = "realm-name",  required = true, operations = { CREATE_CLIENTS, CREATE_USERS })
    private String realmName;

    // NOTE: Start index is not available as parameter as it will be "auto-detected" based on already created realms (clients, users)
    private Integer start;

    // Count of entities to be created
    @QueryParamIntFill(paramName = "count", required = true, operations = { CREATE_REALMS, CREATE_CLIENTS, CREATE_USERS })
    private Integer count;

    // Prefix for realm roles to create in every realm (in case of CREATE_REALMS) or to assign to users (in case of CREATE_USERS)
    @QueryParamFill(paramName = "realm-role-prefix", defaultValue = DatasetConstants.DEFAULT_REALM_ROLE_PREFIX, operations = { CREATE_REALMS, CREATE_USERS })
    private String realmRolePrefix;

    // Count of realm roles to be created in every created realm
    @QueryParamIntFill(paramName = "realm-roles-per-realm", defaultValue = DatasetConstants.DEFAULT_REALM_ROLES_PER_REALM, operations = { CREATE_REALMS })
    private Integer realmRolesPerRealm;

    // Prefix for newly created clients (in case of CREATE_REALMS and CREATE_CLIENTS). In case of CREATE_USERS it is used to find the clients with clientRoles, which will be assigned to users
    @QueryParamFill(paramName = "client-prefix", defaultValue = DatasetConstants.DEFAULT_CLIENT_PREFIX, operations = { CREATE_REALMS, CREATE_CLIENTS, CREATE_USERS })
    private String clientPrefix;

    @QueryParamIntFill(paramName = "clients-per-realm", defaultValue = DatasetConstants.DEFAULT_CLIENTS_PER_REALM, operations = { CREATE_REALMS })
    private Integer clientsPerRealm;

    // Count of clients created in every DB transaction
    @QueryParamIntFill(paramName = "clients-per-transaction", defaultValue = 10, operations = { CREATE_REALMS, CREATE_CLIENTS })
    private Integer clientsPerTransaction;

    // Prefix of clientRoles to be created (in case of CREATE_REALMS and CREATE_CLIENTS). In case of CREATE_USERS it is used to find the clientRoles, which will be assigned to users
    @QueryParamFill(paramName = "client-role-prefix", defaultValue = DatasetConstants.DEFAULT_CLIENT_ROLE_PREFIX, operations = { CREATE_REALMS, CREATE_CLIENTS, CREATE_USERS })
    private String clientRolePrefix;

    @QueryParamIntFill(paramName = "client-roles-per-client", defaultValue = DatasetConstants.DEFAULT_CLIENT_ROLES_PER_CLIENT, operations = { CREATE_REALMS, CREATE_CLIENTS })
    private Integer clientRolesPerClient;

    @QueryParamFill(paramName = "group-prefix", defaultValue = DatasetConstants.DEFAULT_GROUPS_PREFIX, operations = { CREATE_REALMS, CREATE_USERS })
    private String groupPrefix;

    @QueryParamIntFill(paramName = "groups-per-realm", defaultValue = DatasetConstants.DEFAULT_GROUPS_PER_REALM, operations = { CREATE_REALMS })
    private Integer groupsPerRealm;

    @QueryParamFill(paramName = "user-prefix", defaultValue = DatasetConstants.DEFAULT_USERS_PREFIX, operations = { CREATE_REALMS, CREATE_USERS })
    private String userPrefix;

    // Count of users to be created in every realm (In case of CREATE_REALMS) or in specified realm (In case of CREATE_USERS)
    @QueryParamIntFill(paramName = "users-per-realm", defaultValue = DatasetConstants.DEFAULT_USERS_PER_REALM, operations = { CREATE_REALMS })
    private Integer usersPerRealm;

    // Count of groups assigned to every user
    @QueryParamIntFill(paramName = "groups-per-user", defaultValue = DatasetConstants.DEFAULT_GROUPS_PER_USER, operations = { CREATE_REALMS, CREATE_USERS })
    private Integer groupsPerUser;

    // Count of realm roles assigned to every user. The roles assigned are not random, but depends on the "index" of the current user and total amount of roles available and assigned to each user
    @QueryParamIntFill(paramName = "realm-roles-per-user", defaultValue = DatasetConstants.DEFAULT_REALM_ROLES_PER_USER, operations = { CREATE_REALMS, CREATE_USERS })
    private Integer realmRolesPerUser;

    // Count of client roles assigned to every user. The roles assigned are not random, but depends on the "index" of the current user and total amount of roles available and assigned to each user
    @QueryParamIntFill(paramName = "client-roles-per-user", defaultValue = DatasetConstants.DEFAULT_CLIENT_ROLES_PER_USER, operations = { CREATE_REALMS, CREATE_USERS })
    private Integer clientRolesPerUser;

    // Password policy with the amount of password hash iterations. It is 20000 by default
    @QueryParamIntFill(paramName = "password-hash-iterations", defaultValue = DatasetConstants.DEFAULT_HASH_ITERATIONS, operations = { CREATE_REALMS })
    private Integer passwordHashIterations;

    // Transaction timeout used for transactions for creating objects
    @QueryParamIntFill(paramName = "transaction-timeout", defaultValue = DatasetConstants.DEFAULT_TRANSACTION_TIMEOUT_SEC, operations = { CREATE_REALMS, CREATE_CLIENTS, CREATE_USERS })
    private Integer transactionTimeoutInSeconds;

    // Count of users created in every transaction
    @QueryParamIntFill(paramName = "users-per-transaction", defaultValue = DatasetConstants.DEFAULT_USERS_PER_TRANSACTION, operations = { CREATE_REALMS, CREATE_USERS })
    private Integer usersPerTransaction;

    // Count of worker threads concurrently creating entities
    @QueryParamIntFill(paramName = "threads-count", defaultValue = 5, operations = { CREATE_REALMS, CREATE_CLIENTS, CREATE_USERS })
    private Integer threadsCount;

    // String representation of this configuration (cached here to not be computed in runtime)
    private String toString = "DatasetConfig []";

    public String getRealmPrefix() {
        return realmPrefix;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getCount() {
        return count;
    }

    public String getRealmRolePrefix() {
        return realmRolePrefix;
    }

    public Integer getRealmRolesPerRealm() {
        return realmRolesPerRealm;
    }

    public String getClientPrefix() {
        return clientPrefix;
    }

    public Integer getClientsPerRealm() {
        return clientsPerRealm;
    }

    public Integer getClientsPerTransaction() {
        return clientsPerTransaction;
    }

    public String getClientRolePrefix() {
        return clientRolePrefix;
    }

    public Integer getClientRolesPerClient() {
        return clientRolesPerClient;
    }

    public String getGroupPrefix() {
        return groupPrefix;
    }

    public Integer getGroupsPerRealm() {
        return groupsPerRealm;
    }

    public String getUserPrefix() {
        return userPrefix;
    }

    public Integer getUsersPerRealm() {
        return usersPerRealm;
    }

    public Integer getGroupsPerUser() {
        return groupsPerUser;
    }

    public Integer getRealmRolesPerUser() {
        return realmRolesPerUser;
    }

    public Integer getClientRolesPerUser() {
        return clientRolesPerUser;
    }

    public Integer getPasswordHashIterations() {
        return passwordHashIterations;
    }

    public Integer getTransactionTimeoutInSeconds() {
        return transactionTimeoutInSeconds;
    }

    public Integer getUsersPerTransaction() {
        return usersPerTransaction;
    }

    public Integer getThreadsCount() {
        return threadsCount;
    }

    public void setToString(String toString) {
        this.toString = toString;
    }

    @Override
    public String toString() {
        return toString;
    }
}
