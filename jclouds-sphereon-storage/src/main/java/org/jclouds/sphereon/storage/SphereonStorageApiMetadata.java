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

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.rest.internal.BaseHttpApiMetadata;
import org.jclouds.sphereon.storage.provider.config.BlobStoreContextModule;

import java.net.URI;
import java.util.Properties;

import static org.jclouds.reflect.Reflection2.typeToken;

public class SphereonStorageApiMetadata extends BaseHttpApiMetadata<SphereonStorageApi> {

    public static final String API_VERSION = "0.8";

    public SphereonStorageApiMetadata() {
        this(builder());
    }

    protected SphereonStorageApiMetadata(Builder builder) {
        super(builder);
    }

    private static Builder builder() {
        return new Builder();
    }

    public static Properties defaultProperties() {
        Properties properties = BaseHttpApiMetadata.defaultProperties();
        return properties;
    }

    @Override
    public Builder toBuilder() {
        return builder().fromApiMetadata(this);
    }

    public static class Builder extends BaseHttpApiMetadata.Builder<SphereonStorageApi, Builder> {
        protected Builder() {
            super(SphereonStorageApi.class);
            id("sphereon-storage")
                    .name("Sphereon Storage Api")
                    .identityName("Account Name")
                    .credentialName("Access Key")
                    .documentation(URI.create("https://store.sphereon.com/store"))
                    .version(API_VERSION)
                    .defaultEndpoint(String.format("http://gw.api.cloud.sphereon.com/bucket-storage/%s/", API_VERSION))
                    .defaultProperties(SphereonStorageApiMetadata.defaultProperties())
                    .view(typeToken(BlobStoreContext.class))
                    .defaultModules(ImmutableSet.<Class<? extends Module>>builder()
                            .add(BlobStoreContextModule.class).build());
        }

        @Override
        public SphereonStorageApiMetadata build() {
            return new SphereonStorageApiMetadata(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

}