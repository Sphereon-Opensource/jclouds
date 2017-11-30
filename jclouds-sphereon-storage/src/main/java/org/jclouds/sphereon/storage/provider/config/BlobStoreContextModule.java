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

package org.jclouds.sphereon.storage.provider.config;


import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.attr.ConsistencyModel;
import org.jclouds.blobstore.config.BlobStoreObjectModule;
import org.jclouds.sphereon.storage.provider.SphereonStorageBlobStore;

public class BlobStoreContextModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConsistencyModel.class).toInstance(ConsistencyModel.EVENTUAL);
        bind(BlobStore.class).to(SphereonStorageBlobStore.class).in(Scopes.NO_SCOPE);
        install(new BlobStoreObjectModule());
    }
}
