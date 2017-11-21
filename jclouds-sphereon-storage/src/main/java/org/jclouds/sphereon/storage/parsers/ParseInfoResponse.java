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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.TypeLiteral;
import com.sphereon.sdk.model.InfoResponse;
import org.jclouds.http.functions.ParseJson;
import org.jclouds.json.internal.GsonWrapper;
import org.jclouds.sphereon.storage.provider.config.OffsetDateTimeTypeAdapter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;

@Singleton
public class ParseInfoResponse extends ParseJson<InfoResponse> {

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeTypeAdapter()).create();

    @Inject
    public ParseInfoResponse(GsonWrapper json, TypeLiteral<InfoResponse> type) {
        super(new GsonWrapper(GSON), type);
    }
}
