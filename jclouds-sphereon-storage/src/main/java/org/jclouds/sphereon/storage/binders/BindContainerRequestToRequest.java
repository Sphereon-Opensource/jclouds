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

import com.sphereon.sdk.storage.model.ContainerRequest;
import org.jclouds.http.HttpRequest;
import org.jclouds.json.Json;
import org.jclouds.rest.Binder;
import org.jclouds.sphereon.storage.reference.SphereonStorageConstants;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class BindContainerRequestToRequest implements Binder {

    protected final Json jsonBinder;
    private final String backendId;

    @Inject
    public BindContainerRequestToRequest(@Named(SphereonStorageConstants.BACKEND_ID) String backendId, Json jsonBinder) {
        this.jsonBinder = checkNotNull(jsonBinder, "jsonBinder");
        this.backendId = checkNotNull(backendId, "backendId");
    }

    @Override
    public <R extends HttpRequest> R bindToRequest(R request, Object input) {
        checkArgument(checkNotNull(input, "input") instanceof String, "this binder is only valid for String!");
        checkNotNull(request, "request");
        String container = String.class.cast(input);

        ContainerRequest containerRequest = new ContainerRequest();
        containerRequest.setName(container);
        containerRequest.setBackendId(backendId);
        containerRequest.setCreationMode(ContainerRequest.CreationModeEnum.ALLOW_EXISTING);

        String json = jsonBinder.toJson(containerRequest);
        request.setPayload(json);

        return request;
    }
}
