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

package org.jclouds.sphereon.storage.provider.functions;

import autovalue.shaded.org.apache.commons.lang.StringUtils;
import com.sphereon.sdk.model.InfoResponse;
import com.sphereon.sdk.model.StreamData;
import com.sphereon.sdk.model.StreamLocation;
import org.jclouds.blobstore.domain.MutableBlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
import org.jclouds.blobstore.domain.internal.PageSetImpl;

import javax.inject.Singleton;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.function.Function;

@Singleton
public class InfoResponseToMetadata implements Function<InfoResponse, PageSet<MutableBlobMetadata>> {

    public PageSet<MutableBlobMetadata> apply(InfoResponse from) {
        final PageSetImpl<MutableBlobMetadata> mutableStorageMetadata = new PageSetImpl<>(Collections.emptySet(), null);

        if (from == null) {
            return mutableStorageMetadata;
        }

        for (StreamData info : from.getStreamData()) {
            MutableBlobMetadata metadata = new MutableBlobMetadataImpl();

            metadata.setContainer(info.getStreamLocation().getContainerId());
            metadata.setId(info.getStreamLocation().getId());
            metadata.setName(buildName(info.getStreamLocation()));
            metadata.setLocation(null); // sphereon regions not supported

            metadata.setType(StringUtils.isNotEmpty(info.getStreamLocation().getFileName()) ? StorageType.BLOB : StorageType.FOLDER);
            metadata.getContentMetadata().setContentType(info.getContentType());

            metadata.setETag(info.getEtag());
            metadata.setSize(info.getStreamLength());
            if (info.getTimeCreated() != null) {
                metadata.setCreationDate(Date.from(Instant.ofEpochSecond(info.getTimeCreated().toEpochSecond())));
            }
            if (info.getTimeModified() != null) {
                metadata.setLastModified(Date.from(Instant.ofEpochSecond(info.getTimeModified().toEpochSecond())));
            }

            metadata.setUserMetadata(info.getUserMetadata());
            mutableStorageMetadata.add(metadata);
        }

        return mutableStorageMetadata;
    }

    private String buildName(StreamLocation streamLocation) {
        String folder = streamLocation.getFolderPath();
        String filename = streamLocation.getFileName();
        if (StringUtils.isEmpty(folder) || StringUtils.endsWith(folder, "/")) {
            return String.format("%s%s", folder, filename);
        } else {
            return String.format("%s/%s", folder, filename);
        }
    }
}
