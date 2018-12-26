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

package io.appium.espressoserver.lib.handlers;

import android.app.Instrumentation;
import android.util.Base64;

import java.nio.charset.StandardCharsets;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.ClipboardHelper;
import io.appium.espressoserver.lib.model.ClipboardDataType;
import io.appium.espressoserver.lib.model.GetClipboardParams;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class GetClipboard implements RequestHandler<GetClipboardParams, String> {
    private final Instrumentation mInstrumentation = getInstrumentation();

    @Override
    public String handle(GetClipboardParams params) throws AppiumException {
        try {
            return getClipboardResponse(params.getContentType());
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException(e);
        }
    }

    // Clip feature should run with main thread
    private String getClipboardResponse(ClipboardDataType contentType) {
        GetClipboardRunnable runnable = new GetClipboardRunnable(contentType);
        mInstrumentation.runOnMainSync(runnable);
        return runnable.getContent();
    }

    private static String toBase64String(String s) {
        return Base64.encodeToString(s.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }

    private class GetClipboardRunnable implements Runnable {
        private final ClipboardDataType contentType;
        private volatile String content;

        GetClipboardRunnable(ClipboardDataType contentType) {
            this.contentType = contentType;
        }

        @Override
        public void run() {
            switch (contentType) {
                case PLAINTEXT:
                    content = toBase64String(new ClipboardHelper(mInstrumentation.getTargetContext()).getTextData());
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Only '%s' content types are supported. '%s' is given instead",
                            ClipboardDataType.supportedDataTypes(), contentType));
            }
        }

        public String getContent() {
            return content;
        }
    }
}
