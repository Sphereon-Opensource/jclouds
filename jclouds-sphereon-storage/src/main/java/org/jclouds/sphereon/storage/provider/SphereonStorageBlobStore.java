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

package org.jclouds.sphereon.storage.provider;

import com.google.common.base.Supplier;
import com.sphereon.sdk.storage.model.BackendRequest;
import com.sphereon.sdk.storage.model.BackendResponse;
import com.sphereon.sdk.storage.model.ContainerResponse;
import com.sphereon.sdk.storage.model.StreamResponse;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.BlobAccess;
import org.jclouds.blobstore.domain.MultipartUpload;
import org.jclouds.blobstore.domain.ContainerAccess;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.MultipartPart;
import org.jclouds.blobstore.domain.MutableBlobMetadata;
import org.jclouds.blobstore.domain.internal.MutableStorageMetadataImpl;
import org.jclouds.blobstore.domain.internal.PageSetImpl;
import org.jclouds.blobstore.internal.BaseBlobStore;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.collect.Memoized;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.Location;
import org.jclouds.io.MutableContentMetadata;
import org.jclouds.io.Payload;
import org.jclouds.io.PayloadSlicer;
import org.jclouds.location.Provider;
import org.jclouds.logging.Logger;
import org.jclouds.logging.slf4j.SLF4JLogger;
import org.jclouds.sphereon.storage.SphereonStorageApi;
import org.jclouds.sphereon.storage.provider.functions.InfoResponseToMetadata;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.ByteStreams.toByteArray;

@Singleton
public class SphereonStorageBlobStore extends BaseBlobStore {

    private static final Logger logger = new SLF4JLogger.SLF4JLoggerFactory().getLogger(SphereonStorageBlobStore.class.getName());

    private final SphereonStorageApi storageApi;
    private final Supplier<Credentials> credentialsSupplier;
    private final InfoResponseToMetadata infoResponseToMetadata;
    private final Blob.Factory blobFactory;

    @Inject
    protected SphereonStorageBlobStore(BlobStoreContext context,
                                       BlobUtils blobUtils,
                                       Supplier<Location> defaultLocation,
                                       @Memoized Supplier<Set<? extends Location>> locations,
                                       @Provider Supplier<Credentials> credentialsSupplier,
                                       PayloadSlicer slicer,
                                       Blob.Factory blobFactory,
                                       SphereonStorageApi storageApi,
                                       InfoResponseToMetadata infoResponseToMetadata) {
        super(context, blobUtils, defaultLocation, locations, slicer);
        this.credentialsSupplier = credentialsSupplier;
        this.blobFactory = checkNotNull(blobFactory, "blobFactory");
        this.storageApi = checkNotNull(storageApi, "storageApi");
        this.infoResponseToMetadata = checkNotNull(infoResponseToMetadata, "infoResponseToMetadata");
    }

    @Override
    public boolean createContainerInLocation(Location location, String container) {
        return createContainerInLocation(location, container, CreateContainerOptions.NONE);
    }

    @Override
    public boolean createContainerInLocation(Location location, String container, CreateContainerOptions options) {
        if (options.isPublicRead()) {
            throw new UnsupportedOperationException("unsupported in Sphereon Storage");
        }
        ContainerResponse containerResponse = storageApi.createContainer(container);
        return containerResponse != null && containerResponse.getState() == ContainerResponse.StateEnum.CREATED;
    }

    @Override
    public boolean containerExists(String container) {
        ContainerResponse containerResponse = storageApi.getContainer(container);
        return containerResponse != null && containerResponse.getState() != ContainerResponse.StateEnum.DELETED;
    }

    @Override
    protected boolean deleteAndVerifyContainerGone(final String container) {
        ContainerResponse containerResponse = storageApi.deleteContainer(container);
        return containerResponse != null && containerResponse.getState() == ContainerResponse.StateEnum.DELETED;
    }

    @Override
    public String putBlob(String container, Blob blob) {
        return putBlob(container, blob, PutOptions.NONE);
    }

    @Override
    public String putBlob(String container, Blob from, PutOptions putOptions) {
        MutableContentMetadata contentMetadata = checkNotNull(from.getPayload().getContentMetadata());
        long length = checkNotNull(contentMetadata.getContentLength());

        if (length != 0 && (putOptions.isMultipart() || !from.getPayload().isRepeatable())) {
            // JCLOUDS-912 prevents using single-part uploads with InputStream payloads.
            // Work around this with multi-part upload which buffers parts in-memory.
            try {
                byte[] bytes = toByteArray(from.getPayload().openStream());
                from.setPayload(bytes);
                from.getPayload().setContentMetadata(contentMetadata);
            } catch (IOException e) {
                throw new IllegalArgumentException("failed to read stream", e);
            }
        }

        if (putOptions.getBlobAccess() != BlobAccess.PRIVATE) {
            throw new UnsupportedOperationException("blob access not supported by Sphereon Storage");
        }
        String path = checkNotNull(from.getMetadata().getName(), "name");

        StreamResponse streamResponse = storageApi.createStream(container, path, from);
        if (streamResponse != null) {
            String eTag = String.valueOf(streamResponse.getStreamLocation().hashCode());
            return eTag;
        } else {
            return null;
        }
    }

    @Override
    public Blob getBlob(String container, String key, GetOptions getOptions) {
        Payload payload = storageApi.getStream(container, key, getOptions);
        if (payload == null) {
            return null;
        }

        BlobMetadata blobMetadata = blobMetadata(container, key);
        Blob blob = blobFactory.create((MutableBlobMetadata) blobMetadata);
        blob.setPayload(payload);
        return blob;
    }

    @Override
    public void removeBlob(String container, String key) {
        storageApi.deleteStream(container, key);
    }

    @Override
    public BlobMetadata blobMetadata(String container, String key) {
        PageSet<? extends MutableBlobMetadata> storageMetadata = infoResponseToMetadata.apply(storageApi.listStreams(container, ListContainerOptions.Builder.prefix(key).maxResults(1)));
        for (MutableBlobMetadata metadata : storageMetadata) {
            if (metadata.getName().equalsIgnoreCase(key)) {
                return metadata;
            }
        }
        return null;
    }

    @Override
    public boolean blobExists(String container, String key) {
        return blobMetadata(container, key) != null;
    }

    @Override
    public PageSet<? extends StorageMetadata> list() {
        // Returning empty collection util it turns out we actually need this. It does not specify a containerName, so can't do this really at the moment
        return new PageSetImpl<MutableStorageMetadataImpl>(Collections.emptySet(), null);
    }

    @Override
    public PageSet<? extends StorageMetadata> list(String container) {
        return list(container, ListContainerOptions.NONE);
    }

    @Override
    public PageSet<? extends StorageMetadata> list(String container, ListContainerOptions listContainerOptions) {
        return infoResponseToMetadata.apply(storageApi.listStreams(container, listContainerOptions));
    }

    @Override
    public ContainerAccess getContainerAccess(String container) {
        return ContainerAccess.PRIVATE;
    }

    @Override
    public void setContainerAccess(String container, ContainerAccess containerAccess) {
        throw new UnsupportedOperationException("unsupported in Sphereon Storage");
    }

    @Override
    public BlobAccess getBlobAccess(String container, String key) {
        return BlobAccess.PRIVATE;
    }

    @Override
    public void setBlobAccess(String container, String key, BlobAccess blobAccess) {
        throw new UnsupportedOperationException("unsupported in Sphereon Storage");
    }

    @Override
    public MultipartUpload initiateMultipartUpload(String container, BlobMetadata blobMetadata, PutOptions putOptions) {
        throw new UnsupportedOperationException("unsupported in Sphereon Storage");
    }

    @Override
    public void abortMultipartUpload(MultipartUpload multipartUpload) {
        throw new UnsupportedOperationException("unsupported in Sphereon Storage");
    }

    @Override
    public String completeMultipartUpload(MultipartUpload multipartUpload, List<MultipartPart> list) {
        throw new UnsupportedOperationException("unsupported in Sphereon Storage");
    }

    @Override
    public MultipartPart uploadMultipartPart(MultipartUpload multipartUpload, int partNumber, Payload payload) {
        throw new UnsupportedOperationException("unsupported in Sphereon Storage");
    }

    @Override
    public List<MultipartPart> listMultipartUpload(MultipartUpload multipartUpload) {
        throw new UnsupportedOperationException("unsupported in Sphereon Storage");
    }

    @Override
    public List<MultipartUpload> listMultipartUploads(String container) {
        throw new UnsupportedOperationException("unsupported in Sphereon Storage");
    }

    @Override
    public long getMinimumMultipartPartSize() {
        return 0;
    }

    @Override
    public long getMaximumMultipartPartSize() {
        return 0;
    }

    @Override
    public int getMaximumNumberOfParts() {
        return 0;
    }
}
