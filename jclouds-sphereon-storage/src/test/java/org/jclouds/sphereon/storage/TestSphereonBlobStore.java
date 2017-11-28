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

import com.google.common.net.MediaType;
import com.sphereon.sdk.storage.model.OAuth2Credentials;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.http.HttpResponseException;
import org.jclouds.io.ByteStreams2;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.InputStreamPayload;
import org.jclouds.sphereon.storage.reference.SphereonStorageConstants;
import org.testng.Assert;
import org.testng.annotations.Test;


import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;

public class TestSphereonBlobStore {

    private static final BlobStore blobStore = getBlobStore();
    private static final String container = "container-name" + System.currentTimeMillis();
    private static final String filename1 = "file1.txt";
    private static final String filename2 = "folder/file2.txt";

    private static final String sphereonStorageEndpoint = "http://localhost.fiddler:19780/";
    private static final boolean FIDDLER_ENABLED = true;

    static {
        // to support localhost.fiddler
        if (FIDDLER_ENABLED) {
            System.setProperty("http.proxyHost", "127.0.0.1");
            System.setProperty("https.proxyHost", "127.0.0.1");
            System.setProperty("http.proxyPort", "8888");
            System.setProperty("https.proxyPort", "8888");
        }
    }

    private static BlobStore getBlobStore() {
        String provider = "sphereon-storage";
        ContextBuilder contextBuilder = ContextBuilder.newBuilder(provider);
        contextBuilder = sphereonStorageProviderSettings(contextBuilder);

        BlobStoreContext blobStoreContext = contextBuilder.buildView(BlobStoreContext.class);
        BlobStore blobStore = blobStoreContext.getBlobStore();

        return blobStore;
    }

    private static ContextBuilder sphereonStorageProviderSettings(ContextBuilder contextBuilder) {
        OAuth2Credentials accessCredentials = new OAuth2Credentials();
        accessCredentials.setToken("b6573248-ee72-304c-9dde-364ef4802530");

        Properties properties = new Properties();
        properties.setProperty(SphereonStorageConstants.BACKEND_ID, "backend");
        properties.setProperty(SphereonStorageConstants.IDENTITY, SphereonStorageConstants.STORAGE_CLIENT);
        properties.setProperty(SphereonStorageConstants.CREDENTIAL, accessCredentials.getToken());
        properties.setProperty(SphereonStorageConstants.ENDPOINT, sphereonStorageEndpoint);
        return contextBuilder.overrides(properties);
    }

    @Test(priority = 1)
    public void assertContainerNotExists() {
        boolean exists = blobStore.containerExists(container);
        Assert.assertFalse(exists);
    }

    @Test(priority = 2)
    public void createContainer() {
        boolean created = blobStore.createContainerInLocation(null, container);
        Assert.assertTrue(created);
    }

    @Test(priority = 3)
    public void createContainerAgain() {
        boolean created = blobStore.createContainerInLocation(null, container);
        Assert.assertFalse(created);
    }

    @Test(priority = 3)
    public void assertContainerCreated() {
        boolean exists = blobStore.containerExists(container);
        Assert.assertTrue(exists);
    }

    @Test(priority = 3)
    public void containerEmptyList() {
        PageSet<? extends StorageMetadata> list = blobStore.list(container);
        Assert.assertTrue(list.isEmpty());
    }

    @Test(priority = 4)
    public void putAndGetBlob() throws IOException {
        File file = loadResource("file1.txt");
        byte[] bytes = Files.readAllBytes(file.toPath());

        try (InputStream inputStream = new FileInputStream(file)) {
            Payload payload = new InputStreamPayload(inputStream);

            BlobBuilder blobBuilder = blobStore.blobBuilder(filename1);
            BlobBuilder.PayloadBlobBuilder payloadBlobBuilder = blobBuilder.payload(payload);
            payloadBlobBuilder.contentType(MediaType.PLAIN_TEXT_UTF_8);
            payloadBlobBuilder.contentLength(file.length());
            Blob blob = payloadBlobBuilder.build();

            String eTag = blobStore.putBlob(container, blob);
            Assert.assertNotNull(eTag);


            boolean exists = blobStore.blobExists(container, filename1);
            Assert.assertTrue(exists);

            Blob retrieved = blobStore.getBlob(container, filename1);

            assertBlobs(blob, retrieved, bytes);
        }
    }

    @Test(priority = 5)
    public void putAndGetBlobAgain() {
        byte[] payload = "TEXT".getBytes();
        BlobBuilder blobBuilder = blobStore.blobBuilder(filename1);
        BlobBuilder.PayloadBlobBuilder payloadBlobBuilder = blobBuilder.payload(payload);
        payloadBlobBuilder.contentType(MediaType.PLAIN_TEXT_UTF_8);
        Blob blob = payloadBlobBuilder.build();

        String eTag = blobStore.putBlob(container, blob);
        Assert.assertNull(eTag);
    }

    @Test(priority = 5)
    public void putAndGetBlobFolder() throws IOException {
        byte[] payload = "TEXT".getBytes();
        BlobBuilder blobBuilder = blobStore.blobBuilder(filename2);
        BlobBuilder.PayloadBlobBuilder payloadBlobBuilder = blobBuilder.payload(payload);
        payloadBlobBuilder.contentType(MediaType.PLAIN_TEXT_UTF_8);
        Blob blob = payloadBlobBuilder.build();

        PutOptions putOptions = PutOptions.Builder.multipart();

        String eTag = blobStore.putBlob(container, blob, putOptions);
        Assert.assertNotNull(eTag);

        boolean exists = blobStore.blobExists(container, filename2);
        Assert.assertTrue(exists);

        Blob retrieved = blobStore.getBlob(container, filename2);

        assertBlobs(blob, retrieved, payload);
    }

    @Test(priority = 6)
    public void list() {
        ListContainerOptions options = ListContainerOptions.Builder.prefix(filename1);
        PageSet<? extends StorageMetadata> list = blobStore.list(container, options);

        Assert.assertEquals(list.size(), 1);
        for (StorageMetadata metadata : list) {
            Assert.assertEquals(metadata.getName(), filename1);
        }

        list = blobStore.list(container);

        Assert.assertEquals(list.size(), 2);
        for (StorageMetadata metadata : list) {
            if (filename1.equalsIgnoreCase(metadata.getName())) {
                Assert.assertEquals(metadata.getName(), filename1);
            } else {
                Assert.assertEquals(metadata.getName(), filename2);
            }
        }
    }

    @Test(priority = 7)
    public void removeBlob() {
        PageSet<? extends StorageMetadata> list = blobStore.list(container);
        int sizeBefore = list.size();

        blobStore.removeBlob(container, filename1);

        boolean exists = blobStore.blobExists(container, filename1);
        Assert.assertFalse(exists);

        list = blobStore.list(container);
        Assert.assertEquals(list.size(), sizeBefore - 1);
    }

    @Test(priority = 8, expectedExceptions = HttpResponseException.class)
    public void deleteContainerTry() {
        boolean deleted = blobStore.deleteContainerIfEmpty(container);
        Assert.assertFalse(deleted);
    }

    @Test(priority = 9)
    public void deleteContainer() {
        blobStore.removeBlob(container, filename2);

        blobStore.deleteContainer(container);


        boolean exists = blobStore.blobExists(container, filename1);
        Assert.assertFalse(exists);

        exists = blobStore.blobExists(container, filename2);
        Assert.assertFalse(exists);
    }

    @Test(priority = 10)
    public void assertContainerDeleted() {
        boolean exists = blobStore.containerExists(container);
        Assert.assertFalse(exists);
    }

    private void assertBlobs(Blob blob, Blob retrieved, byte[] payload) throws IOException {
        Assert.assertNotNull(retrieved.getMetadata().getContainer());
        Assert.assertEquals(retrieved.getMetadata().getName(), blob.getMetadata().getName());
        Assert.assertEquals(retrieved.getMetadata().getType(), blob.getMetadata().getType());
        Assert.assertNotNull(retrieved.getMetadata().getSize());
        Assert.assertNotNull(retrieved.getMetadata().getLastModified());
        Assert.assertNotNull(retrieved.getMetadata().getContentMetadata().getContentType());

        Assert.assertEquals(ByteStreams2.toByteArrayAndClose(retrieved.getPayload().openStream()), payload);
    }

    private File loadResource(String filename) {
        try {
            URL url = getClass().getClassLoader().getResource(filename);
            File file = new File(url.toURI());
            return file;
        } catch (URISyntaxException e) {
            Assert.fail(e.getMessage());
            return null;
        }
    }
}
