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

import com.google.inject.TypeLiteral;
import com.sphereon.sdk.storage.model.StreamLocation;
import com.sphereon.sdk.storage.model.StreamResponse;
import org.jclouds.http.functions.ParseJson;
import org.jclouds.json.Json;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ParseStreamResponse extends ParseJson<StreamResponse> {
    @Inject
    public ParseStreamResponse(Json json, TypeLiteral<StreamResponse> type) {
        super(json, type);
    }
}
