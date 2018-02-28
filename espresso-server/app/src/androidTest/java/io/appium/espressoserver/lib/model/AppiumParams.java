/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.espressoserver.lib.model;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class AppiumParams {
    private static final String SESSION_ID_PARAM_NAME = "sessionId";
    private static final String ELEMENT_ID_PARAM_NAME = "elementId";
    private final Map<String, String> uriParams = new HashMap<>();

    @Nullable
    public String getSessionId() {
        return getUriParameterValue(SESSION_ID_PARAM_NAME);
    }

    @Nullable
    public String getElementId() {
        return getUriParameterValue(ELEMENT_ID_PARAM_NAME);
    }

    public void setElementId(String elementId) {
        setUriParameterValue(ELEMENT_ID_PARAM_NAME, elementId);
    }

    public void initUriMapping(Map<String, String> params) {
        uriParams.clear();
        uriParams.putAll(params);
    }

    @Nullable
    public String getUriParameterValue(String name) {
        return uriParams.get(name);
    }

    private void setUriParameterValue(String name, String value) {
        uriParams.put(name, value);
    }
}
