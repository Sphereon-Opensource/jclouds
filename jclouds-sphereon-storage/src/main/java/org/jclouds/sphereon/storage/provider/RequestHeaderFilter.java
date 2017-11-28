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

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import org.jclouds.domain.Credentials;
import org.jclouds.http.HttpException;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpRequestFilter;
import org.jclouds.location.Provider;
import org.jclouds.logging.Logger;
import org.jclouds.logging.slf4j.SLF4JLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RequestHeaderFilter implements HttpRequestFilter {

    private static final Logger logger = new SLF4JLogger.SLF4JLoggerFactory().getLogger(RequestHeaderFilter.class.getName());

    private final Supplier<Credentials> credentials;

    @Inject
    public RequestHeaderFilter(@Provider Supplier<Credentials> credentials) {
        this.credentials = credentials;
    }

    @Override
    public HttpRequest filter(HttpRequest request) throws HttpException {
        String oauth2Credentials = credentials.get().credential;

        HttpRequest.Builder builder = request.toBuilder().replaceHeader(HttpHeaders.ACCEPT, MediaType.ANY_TYPE.type());
        if (!Strings.isNullOrEmpty(oauth2Credentials)) {
            if (!oauth2Credentials.toLowerCase().startsWith("bearer")) {
                oauth2Credentials = "Bearer " + oauth2Credentials;
            }
            builder.replaceHeader(HttpHeaders.AUTHORIZATION, oauth2Credentials);
        }
        request = builder.build();
        logger.debug("<< %s", request);
        return request;
    }
}
