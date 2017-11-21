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

import com.sphereon.sdk.model.ContainerResponse;
import com.sphereon.sdk.model.InfoResponse;
import com.sphereon.sdk.model.StreamLocation;
import org.jclouds.Fallbacks;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.io.Payload;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.annotations.SkipEncoding;
import org.jclouds.sphereon.storage.binders.BindBlobToRequest;
import org.jclouds.sphereon.storage.binders.BindContainerRequestToRequest;
import org.jclouds.sphereon.storage.binders.BindGetOptionsToRequest;
import org.jclouds.sphereon.storage.binders.BindListOptionsToRequest;
import org.jclouds.sphereon.storage.fallbacks.NullOn404OR409;
import org.jclouds.sphereon.storage.parsers.ParseContainerResponse;
import org.jclouds.sphereon.storage.parsers.ParseInfoResponse;
import org.jclouds.sphereon.storage.parsers.ParseStreamLocation;
import org.jclouds.sphereon.storage.parsers.ParseStreamToBlob;
import org.jclouds.sphereon.storage.provider.RequestHeaderFilter;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.Closeable;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

/**
 * Provides access to Sphereon Storage Blob via REST API.
 */
@RequestFilters(RequestHeaderFilter.class)
@SkipEncoding({'/', '$'})
@Path("/")
public interface SphereonStorageApi extends Closeable {

    /**
     * The Create Container
     */
    @Named("CreateContainer")
    @POST
    @Path("containers")
    @Produces(APPLICATION_JSON)
    @ResponseParser(ParseContainerResponse.class)
    @Fallback(NullOn404OR409.class)
    ContainerResponse createContainer(@BinderParam(BindContainerRequestToRequest.class) String container);

    /**
     * The Get Container
     */
    @Named("GetContainer")
    @GET
    @Path("containers/{container}")
    @ResponseParser(ParseContainerResponse.class)
    @Fallback(Fallbacks.NullOnNotFoundOr404.class)
    ContainerResponse getContainer(@PathParam("container") String container);

    /**
     * The Delete Container
     */
    @Named("DeleteContainer")
    @DELETE
    @Path("containers/{container}")
    @ResponseParser(ParseContainerResponse.class)
    @Fallback(Fallbacks.NullOnNotFoundOr404.class)
    ContainerResponse deleteContainer(@PathParam("container") String container);


    /**
     * The Create Stream
     */
    @Named("CreateStream")
    @POST
    @Path("streams/path/{container}/{path}")
    @ResponseParser(ParseStreamLocation.class)
    @Fallback(NullOn404OR409.class)
    StreamLocation createStream(@PathParam("container") String container,
                                @PathParam("path") String path,
                                @BinderParam(BindBlobToRequest.class) Blob blob);

    /**
     * The Get Stream
     */
    @Named("GetStream")
    @GET
    @Path("streams/path/{container}/{path}")
    @Consumes({APPLICATION_OCTET_STREAM, APPLICATION_JSON})
    @ResponseParser(ParseStreamToBlob.class)
    @Fallback(Fallbacks.NullOnNotFoundOr404.class)
    Payload getStream(@PathParam("container") String container,
                      @PathParam("path") String path,
                      @BinderParam(BindGetOptionsToRequest.class) GetOptions options);

    /**
     * The Delete Stream
     */
    @Named("DeleteStream")
    @DELETE
    @Path("streams/path/{container}/{path}")
    @Fallback(Fallbacks.VoidOnNotFoundOr404.class)
    void deleteStream(@PathParam("container") String container,
                      @PathParam("path") String name);


    /**
     * The List Streams operation
     */
    @Named("ListStreams")
    @GET
    @Path("info/path/{container}/")
    @ResponseParser(ParseInfoResponse.class)
    @Fallback(Fallbacks.NullOnNotFoundOr404.class)
    InfoResponse listStreams(@PathParam("container") String container,
                             @BinderParam(BindListOptionsToRequest.class) ListContainerOptions options);

}
