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

package org.jclouds.sphereon.storage.binders;

import com.google.common.base.Preconditions;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.http.HttpRequest;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.MultipartForm;
import org.jclouds.io.payloads.Part;
import org.jclouds.rest.Binder;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

@Singleton
public class BindBlobToRequest implements Binder {
    private static final String BOUNDARY_HEADER = "multipart_boundary";
    public static final String MULTIPART_STREAM = "stream";


    private Blob.Factory blobFactory;

    @Inject
    public BindBlobToRequest(Blob.Factory blobFactory) {
        this.blobFactory = checkNotNull(blobFactory, "blobFactory");
    }

    @Override
    public <R extends HttpRequest> R bindToRequest(R request, Object input) {
        checkArgument(checkNotNull(input, "input") instanceof Blob, "this binder is only valid for Blob");
        checkNotNull(request, "request");
        Blob blob = Blob.class.cast(input);

        Payload payload = blob.getPayload();
        checkArgument(payload.getContentMetadata().getContentLength() != null && payload.getContentMetadata().getContentLength() >= 0, "size must be set");

        String name = blob.getMetadata().getName();
        String contentType = payload.getContentMetadata().getContentType();
        Part dataPart = Part.create(MULTIPART_STREAM, payload, Part.PartOptions.Builder.filename(name).contentType(contentType));

        request.setPayload(new MultipartForm(BOUNDARY_HEADER, dataPart));

        // HeaderPart
        request.toBuilder().replaceHeader(CONTENT_TYPE, "Multipart/related; boundary= " + BOUNDARY_HEADER).build();

        return request;
    }
}
