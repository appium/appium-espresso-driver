package io.appium.espressoserver.lib.handlers;

import android.graphics.Bitmap;
import android.util.Base64;
import android.view.View;

import java.io.ByteArrayOutputStream;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.viewaction.RootViewFinder;

public class Screenshot implements RequestHandler<AppiumParams, String> {

    @Override
    public String handle(AppiumParams params) throws AppiumException {
        return takeScreenshot();
    }


    private String takeScreenshot() throws AppiumException {
        try {
            // Create bitmap screen capture
            View rootView = (new RootViewFinder()).getRootView();
            rootView.setDrawingCacheEnabled(true);
            Bitmap bitmapScreenCap = Bitmap.createBitmap(rootView.getDrawingCache());
            rootView.setDrawingCacheEnabled(false);

            // Stream the bitmap to byte array stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int quality = 100;
            bitmapScreenCap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            byte[] bytes = outputStream.toByteArray();

            // Encode the byte array stream to base 64
            return Base64.encodeToString(bytes, Base64.DEFAULT);

        } catch (Exception e) {
            throw new AppiumException(String.format("Could not get screenshot %s", e.getCause()));
        }
    }

}
