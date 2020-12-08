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
import java.util.function.Function;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.services.DefaultKeycloakSessionFactory;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ConfigUtil {

    private static final Logger logger = Logger.getLogger(ConfigUtil.class);

    public static <T extends Config> T createConfigFromQueryParams(HttpRequest httpRequest, Class<T> configClass) {
        T config;
        try {
            config = configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to construct " + configClass.getName(), e);
        }

        for (Field f : configClass.getDeclaredFields()) {
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
                    f.set(config, val);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fill the field " + qpf.paramName());
                }
            }

            QueryParamIntFill qpfInt = f.getAnnotation(QueryParamIntFill.class);
            if (qpfInt != null) {
                String valStr = httpRequest.getUri().getQueryParameters().getFirst(qpfInt.paramName());
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
                    f.set(config, val);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fill the field " + qpfInt.paramName());
                }
            }
        }

        return config;
    }


    public static int findFreeEntityIndex(Function<Integer, Boolean> finder) {
        int lastFound = -1;
        int lastFailed = -1;
        int current = 100;

        // Try to find very first entity
        if (finder.apply(0)) {
            lastFound = 0;
        } else {
            return 0;
        }

        while (lastFailed == -1 || (lastFailed - lastFound > 1)) {
            logger.info("Current: " + current);
            boolean found = finder.apply(current);
            if (found) {
                lastFound = current;
            } else {
                lastFailed = current;
            }
            if (lastFailed == -1) {
                current = current * 2;
            } else {
                current = (lastFound + lastFailed) / 2;
            }
        }

        return lastFailed;
    }
}
