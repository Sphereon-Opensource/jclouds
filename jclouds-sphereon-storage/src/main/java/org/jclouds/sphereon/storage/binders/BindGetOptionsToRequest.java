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

import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.http.HttpRequest;
import org.jclouds.rest.Binder;

import java.net.URI;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class BindGetOptionsToRequest implements Binder {
    @Override
    public <R extends HttpRequest> R bindToRequest(R request, Object input) {
        checkArgument(checkNotNull(input, "input") instanceof GetOptions, "this binder is only valid for GetOptions");
        checkNotNull(request, "request");

        GetOptions options = GetOptions.class.cast(input);

        URI requestEndpoint = request.getEndpoint();
        String path = requestEndpoint.getPath();

        request = (R) request.toBuilder().replacePath(path).build();

        return request;
    }
}
