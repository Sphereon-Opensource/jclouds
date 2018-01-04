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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;
import com.google.inject.Module;
import com.sphereon.sdk.storage.model.BackendRequest;
import com.sphereon.sdk.storage.model.BackendResponse;
import com.sphereon.sdk.storage.model.BearerTokenCredentials;
import com.sphereon.sdk.storage.model.CredentialsRequest;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.http.HttpResponseException;
import org.jclouds.io.ByteStreams2;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.InputStreamPayload;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.sphereon.storage.reference.SphereonStorageConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Properties;

import static org.jclouds.sphereon.storage.reference.SphereonStorageConstants.DEFAULT_ENDPOIMT;
import static org.jclouds.sphereon.storage.reference.SphereonStorageConstants.SPHEREON_STORAGE;

@Test(singleThreaded = true)
public class TestSphereonBlobStore {

    // The backend name or ID. Please not that this backend has to exist in Sphereon for the tests to work. Creating backends is out of the scope of jclouds
    public static final String BACKEND_NAME_OR_ID = "jclouds-test-backend";
    private static final String container = "container-name" + System.currentTimeMillis();
    private static final String filename1 = "file1.txt";
    private static final String filename2 = "folder/file2.txt";

    private static final boolean FIDDLER_ENABLED = Boolean.parseBoolean(System.getProperty("sphereon-storage.test.fiddler.enabled", "false"));
    private static final String API_OAUTH2_TOKEN = System.getProperty("sphereon-storage.test.api-token", "0dbd17f1-c108-350e-807e-42d13e543b32");
    private static final String ENDPOINT = System.getProperty("sphereon-storage.test.endpoint", /*"http://localhost:19780"*/ DEFAULT_ENDPOIMT);

    private final BlobStore blobStore;
    private final SphereonStorageApi storageApi;

    public TestSphereonBlobStore() {
        ContextBuilder contextBuilder = createContextBuilder();
        BlobStoreContext blobStoreContext = contextBuilder.modules(ImmutableSet.<Module>of(new SLF4JLoggingModule())).buildView(BlobStoreContext.class);

        this.blobStore = blobStoreContext.getBlobStore();
        this.storageApi = contextBuilder.buildApi(SphereonStorageApi.class);
    }

    private ContextBuilder createContextBuilder() {
        ContextBuilder contextBuilder = ContextBuilder.newBuilder(SPHEREON_STORAGE);
        BearerTokenCredentials accessCredentials = new BearerTokenCredentials();
        accessCredentials.setToken(API_OAUTH2_TOKEN);

        Properties properties = new Properties();
        properties.setProperty(SphereonStorageConstants.BACKEND_ID, BACKEND_NAME_OR_ID);
        properties.setProperty(SphereonStorageConstants.IDENTITY, SphereonStorageConstants.STORAGE_CLIENT);
        properties.setProperty(SphereonStorageConstants.CREDENTIAL, accessCredentials.getToken());
        properties.setProperty(Constants.PROPERTY_ENDPOINT, ENDPOINT);
        if (FIDDLER_ENABLED) {
            properties.setProperty(Constants.PROPERTY_PROXY_HOST, "localhost");
            properties.setProperty(Constants.PROPERTY_PROXY_PORT, "8888");

        }
        //properties.setProperty(SphereonStorageConstants.TEMP_DIR, storagePathController.getStorageTemp(accessCredentials).getAbsolutePath());
        //properties.setProperty(SphereonStorageConstants.STORAGE_API_BASE_PATH, storageApiBasePath);
        return contextBuilder.overrides(properties);
    }


    @Test(priority = 0)
    public void assertBackendExists() {
        boolean exists = storageApi.backendExists(BACKEND_NAME_OR_ID);
        if (!exists) {
            BackendRequest backendRequest = new BackendRequest();
            backendRequest.setName(BACKEND_NAME_OR_ID);
            backendRequest.setBackendType(BackendRequest.BackendTypeEnum.SPHEREON_CLOUD_STORAGE);
            backendRequest.setDescription("Test backend");
            BackendResponse response = storageApi.createBackend(backendRequest);
            exists = response.getState() == BackendResponse.StateEnum.CREATED;
        }
        Assert.assertTrue(exists);
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
//            Assert.assertEquals(blob.getMetadata().getContentMetadata().getContentType(), "text/plain");
//            Assert.assertEquals(blob.getMetadata().getType(), StorageType.BLOB);

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

        // Try with encoded name
        String encodedFileName = URLEncoder.encode(filename2, Charsets.US_ASCII.name());
        exists = blobStore.blobExists(container, encodedFileName);
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
            System.out.println(String.format("%s: %s (%d)", metadata.getType(), metadata.getName(), metadata.getSize()));
            if (filename1.equalsIgnoreCase(metadata.getName())) {
                Assert.assertEquals(metadata.getName(), filename1);
            } else {
                Assert.assertEquals(metadata.getName(), filename2);
            }
        }
    }

    @Test(priority = 6)
    public void blobInfo() {
        BlobMetadata blobMetadata = blobStore.blobMetadata(container, filename2);

        Assert.assertNotNull(blobMetadata);
        Assert.assertNotNull(blobMetadata.getContentMetadata());
        Assert.assertEquals(blobMetadata.getName(), filename2);
        Assert.assertNotNull(blobMetadata.getContainer());
        Assert.assertNotNull(blobMetadata.getSize());
        Assert.assertEquals(blobMetadata.getContentMetadata().getContentType(), MediaType.PLAIN_TEXT_UTF_8.toString());
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
