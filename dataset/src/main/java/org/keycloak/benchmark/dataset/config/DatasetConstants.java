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

import org.keycloak.credential.hash.Pbkdf2PasswordHashProviderFactory;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DatasetConstants {

    public static final String DEFAULT_REALM_PREFIX = "realm-";

    /**
     * -1 means that we will try to "auto-guess" where to start based on realms available
     * TODO: Change to -1
     */
    public static final int DEFAULT_REALM_START = 0;

    // public static final Integer DEFAULT_REALM_COUNT = 100;

    public static final String DEFAULT_REALM_ROLE_PREFIX = "role-";

    public static final int DEFAULT_REALM_ROLES_PER_REALM = 25;

    public static final String DEFAULT_CLIENT_PREFIX = "client-";

    public static final int DEFAULT_CLIENTS_PER_REALM = 30;

    public static final String DEFAULT_CLIENT_ROLE_PREFIX = "client-role-";

    public static final int DEFAULT_CLIENT_ROLES_PER_CLIENT = 10;

    public static final String DEFAULT_GROUPS_PREFIX = "group-";

    public static final int DEFAULT_GROUPS_PER_REALM = 20;

    public static final String DEFAULT_USERS_PREFIX = "user-";

    public static final int DEFAULT_USERS_PER_REALM = 200;

    public static final int DEFAULT_HASH_ITERATIONS = Pbkdf2PasswordHashProviderFactory.DEFAULT_ITERATIONS;

    public static final int DEFAULT_REALM_ROLES_PER_USER = 4;

    public static final int DEFAULT_CLIENT_ROLES_PER_USER = 4;

    public static final int DEFAULT_GROUPS_PER_USER = 4;


}
