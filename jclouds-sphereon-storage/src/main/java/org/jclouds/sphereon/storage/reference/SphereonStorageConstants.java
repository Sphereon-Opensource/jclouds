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

package org.jclouds.sphereon.storage.reference;

public final class SphereonStorageConstants {

    public static final String SPHEREON_STORAGE = "sphereon-storage";
    public static final String BACKEND_ID = "jclouds." + SPHEREON_STORAGE;
    public static final String IDENTITY = SPHEREON_STORAGE + ".identity";
    public static final String STORAGE_CLIENT = "storage-client";
    public static final String CREDENTIAL = SPHEREON_STORAGE + ".credential";


    public static final String API_VERSION = "0.8";
    public static final String DEFAULT_ENDPOIMT = String.format("http://gw-dev.api.cloud.sphereon.com/bucket-storage/%s", API_VERSION);

    // String.format("https://gw.api.cloud.sphereon.com/bucket-storage/%s/", API_VERSION);


    private SphereonStorageConstants() {}
}
