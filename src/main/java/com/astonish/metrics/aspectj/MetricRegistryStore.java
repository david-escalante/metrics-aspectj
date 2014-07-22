/**
 * Copyright (C) 2013 Astonish Results (mcarrier@astonish.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.astonish.metrics.aspectj;

import com.codahale.metrics.MetricRegistry;

/**
 * Stores the {@link MetricRegistry} for access from the aspects.
 */
public class MetricRegistryStore {
    private static final MetricRegistryStore INSTANCE = new MetricRegistryStore();
    private MetricRegistry registry;

    /**
     * Hidden private constructor for singleton instance.
     */
    private MetricRegistryStore() {

    }

    /**
     * @return the singleton instance
     */
    public static MetricRegistryStore getInstance() {
        return INSTANCE;
    }

    /**
     * @return the {@link MetricRegistry}
     */
    public MetricRegistry getRegistry() {
        if (null == registry) {
            throw new RuntimeException("MetricRegistry was never set.");
        }

        return registry;
    }

    /**
     * @param registry
     *            the {@link MetricRegistry} to set
     */
    public void setRegistry(MetricRegistry registry) {
        if (null != this.registry) {
            throw new RuntimeException("MetricRegistry can only be set once.");
        }

        this.registry = registry;
    }
}
