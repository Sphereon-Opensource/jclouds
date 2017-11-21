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

import autovalue.shaded.org.apache.commons.lang.StringUtils;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.http.HttpRequest;
import org.jclouds.rest.Binder;

import java.net.URI;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class BindListOptionsToRequest implements Binder {
    @Override
    public <R extends HttpRequest> R bindToRequest(R request, Object input) {
        checkArgument(checkNotNull(input, "input") instanceof ListContainerOptions, "this binder is only valid for ListContainerOptions");
        checkNotNull(request, "request");

        ListContainerOptions options = ListContainerOptions.class.cast(input);


        String path = "";
        if (options.getDir() != null) {
            path = options.getDir();
            if (!StringUtils.isEmpty(path) && !path.endsWith("/")) {
                path = path + "/";
            }
        }

        if (options.getPrefix() != null) {
            path = options.getPrefix() + path;
        }

        URI requestEndpoint = request.getEndpoint();
        request = (R) request.toBuilder().replacePath(requestEndpoint.getPath() + path).build();

        return request;
    }
}
