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

package org.jclouds.sphereon.storage.fallbacks;

import org.jclouds.Fallback;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.in;
import static com.google.common.primitives.Ints.asList;
import static org.jclouds.http.HttpUtils.returnValueOnCodeOrNull;

public class NullOn404OR409 implements Fallback<Object> {
    public NullOn404OR409() {
    }

    public Object createOrPropagate(Throwable t) throws Exception {
        return returnValueOnCodeOrNull(checkNotNull(t, "throwable"), (Object) null, in(asList(404, 409)));
    }
}
