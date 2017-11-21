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

package org.jclouds.sphereon.storage;/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteSource;
import com.sphereon.sdk.model.OAuth2Credentials;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.integration.internal.BaseBlobLiveTest;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.internal.PayloadEnclosingImpl;
import org.jclouds.io.ByteStreams2;
import org.jclouds.io.Payloads;
import org.jclouds.io.payloads.ByteSourcePayload;
import org.jclouds.sphereon.storage.reference.SphereonStorageConstants;
import org.jclouds.utils.TestUtils;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Test(groups = {"live"})
public class SphereonStorageBlobLiveTest extends BaseBlobLiveTest {
    private static final String sphereonEndpoint = System.getProperty("sphereon-storage.endpoint", "https://sandbox.xill.io/v2");
    private static final String sysHttpStreamMD5 = System.getProperty("jclouds.blobstore.httpstream.md5");

    public SphereonStorageBlobLiveTest() {
        provider = "sphereon-storage";
    }

    static {
        // to support localhost.fiddler
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "8888");
        System.setProperty("https.proxyPort", "8888");
    }

    @Override
    protected Properties setupProperties() {
        Properties properties = super.setupProperties();
        properties.setProperty("sphereon-storage.endpoint", sphereonEndpoint);
        properties.setProperty("sphereon-storage.identity", "sphereon-storage");
        properties.setProperty("sphereon-storage.credential", "sphereon-storage");
        properties.setProperty("jclouds.identity", "API User");

        OAuth2Credentials accessCredentials = new OAuth2Credentials();
        accessCredentials.setToken("b6573248-ee72-304c-9dde-364ef4802530");

        properties.setProperty(SphereonStorageConstants.BACKEND_ID, "backend");
        properties.setProperty(SphereonStorageConstants.CREDENTIAL, accessCredentials.getToken());
        properties.setProperty("sphereon-storage.endpoint", "http://localhost.fiddler:19780/");
        return properties;
    }

    @Override
    @Parameters({"jclouds.blobstore.httpstream.url", "jclouds.blobstore.httpstream.md5"})
    public void testCopyUrl(String httpStreamUrl, String httpStreamMD5) throws Exception {
        httpStreamUrl = checkNotNull(httpStreamUrl != null ? httpStreamUrl : sphereonEndpoint, "httpStreamUrl");
        httpStreamMD5 = checkNotNull(httpStreamMD5 != null ? httpStreamMD5 : sysHttpStreamMD5, "httpStreamMd5");

        HttpResponse response = view.utils().http()
                .invoke(HttpRequest.builder().method("GET").endpoint(httpStreamUrl).build());
        long length = response.getPayload().getContentMetadata().getContentLength();

        checkNotNull(response.getPayload().getContentMetadata().getContentType());
        assertEquals(response.getPayload().getContentMetadata().getContentType(), "application/x-gzip");

        String name = "hello";
        HashCode md5 = HashCode.fromBytes(BaseEncoding.base16().lowerCase().decode(httpStreamMD5));
        byte[] payload = ByteStreams2.toByteArrayAndClose(response.getPayload().getInput());

        Blob blob = view.getBlobStore().blobBuilder(name).payload(payload).contentLength(length)
                .contentType(response.getPayload().getContentMetadata().getContentType())
                .contentMD5(md5).build();
        String container = getContainerName();
        try {
            assertNotNull(view.getBlobStore().putBlob(container, blob));
            checkMD5(container, name, md5.asBytes());
        } finally {
            returnContainer(container);
        }
    }

    @Test(groups = "live")
    public void testPutBlobWithMd5() throws IOException, InterruptedException {
        String containerName = getContainerName();
        String blobName = "md5test";
        try {
            long contentLength = 32 * 1024L;
            ByteSource byteSource = TestUtils.randomByteSource().slice(0, contentLength);
            ByteSourcePayload payload = Payloads.newByteSourcePayload(byteSource);
            PayloadEnclosingImpl payloadImpl = new PayloadEnclosingImpl(payload);

            BlobStore blobStore = view.getBlobStore();

            // This would trigger server side validation of md5
            HashCode hcMd5 = byteSource.hash(Hashing.md5());

            Blob blob = blobStore.blobBuilder(blobName).payload(payloadImpl.getPayload()).contentType("image/jpeg")
                    .contentLength(contentLength).contentLanguage("en").contentDisposition("attachment")
                    .contentMD5(hcMd5).userMetadata(ImmutableMap.of("Adrian", "powderpuff")).build();

            blobStore.putBlob(containerName, blob);
            checkMD5(containerName, blobName, hcMd5.asBytes());

        } finally {
            returnContainer(containerName);
        }
    }
}