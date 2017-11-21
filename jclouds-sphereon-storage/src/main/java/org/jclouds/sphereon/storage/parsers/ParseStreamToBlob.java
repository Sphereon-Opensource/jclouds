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

package org.jclouds.sphereon.storage.parsers;

import com.google.common.base.Function;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.http.HttpResponse;
import org.jclouds.io.Payload;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class ParseStreamToBlob implements Function<HttpResponse, Payload> {

    private final Blob.Factory blobFactory;

    @Inject
    public ParseStreamToBlob(Blob.Factory blobFactory) {
        this.blobFactory = checkNotNull(blobFactory, "blobFactory");
    }

    public Payload apply(HttpResponse from) {
        checkNotNull(from, "request");
        return from.getPayload();
    }
}
