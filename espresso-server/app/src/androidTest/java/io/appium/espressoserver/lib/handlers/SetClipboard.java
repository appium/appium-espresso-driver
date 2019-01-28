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
import io.appium.espressoserver.lib.model.SetClipboardParams;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class SetClipboard implements RequestHandler<SetClipboardParams, Void> {
    private final Instrumentation mInstrumentation = getInstrumentation();

    @Override
    public Void handle(SetClipboardParams params) throws AppiumException {
        if (params.getContent() == null) {
            throw new InvalidArgumentException("The 'content' argument is mandatory");
        }
        try {
            mInstrumentation.runOnMainSync(new SetClipboardRunnable(
                    params.getContenttype(), params.getLabel(), fromBase64String(params.getContent())));
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException(e);
        }
        return null;
    }

    private static String fromBase64String(String s) {
        return new String(Base64.decode(s, Base64.DEFAULT), StandardCharsets.UTF_8);
    }

    // Clip feature should run with main thread
    private class SetClipboardRunnable implements Runnable {
        private final ClipboardDataType contentType;
        private final String label;
        private final String content;

        SetClipboardRunnable(ClipboardDataType contentType, String label, String content) {
            this.contentType = contentType;
            this.label = label;
            this.content = content;
        }

        @Override
        public void run() {
            switch (contentType) {
                case PLAINTEXT:
                    new ClipboardHelper(mInstrumentation.getTargetContext()).setTextData(label, content);
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Only '%s' content types are supported. '%s' is given instead",
                                    ClipboardDataType.Companion.supportedDataTypes(), contentType));
            }
        }
    }
}
