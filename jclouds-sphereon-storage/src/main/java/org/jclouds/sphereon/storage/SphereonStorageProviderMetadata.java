/*
 * Copyright 2017 Sphereon B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jclouds.sphereon.storage;

import com.google.auto.service.AutoService;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.internal.BaseProviderMetadata;

import java.net.URI;
import java.util.Properties;

import static org.jclouds.sphereon.storage.reference.SphereonStorageConstants.SPHEREON_STORAGE;

@AutoService(ProviderMetadata.class)
public class SphereonStorageProviderMetadata extends BaseProviderMetadata {

    public SphereonStorageProviderMetadata() {
        super(builder());
    }

    public SphereonStorageProviderMetadata(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Builder toBuilder() {
        return builder().fromProviderMetadata(this);
    }

    public static Properties defaultProperties() {
        Properties properties = new Properties();
        return properties;
    }

    public static final class Builder extends BaseProviderMetadata.Builder {

        private Builder() {
            id(SPHEREON_STORAGE)
                    .name("Sphereon Storage")
                    .apiMetadata(new SphereonStorageApiMetadata())
                    .homepage(URI.create("https://www.sphereon.com"))
                    .console(URI.create("https://store.sphereon.com/store"))
                    .defaultProperties(SphereonStorageProviderMetadata.defaultProperties());
        }

        @Override
        public SphereonStorageProviderMetadata build() {
            return new SphereonStorageProviderMetadata(this);
        }

        @Override
        public Builder fromProviderMetadata(ProviderMetadata in) {
            super.fromProviderMetadata(in);
            return this;
        }
    }
}