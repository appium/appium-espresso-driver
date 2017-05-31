package io.appium.espressoserver;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import io.appium.espressoserver.lib.exceptions.DuplicateRouteException;
import io.appium.espressoserver.lib.http.Server;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoServerRunnerTest {

    @Test
    public void startEspressoServer() throws InterruptedException, IOException, DuplicateRouteException {
        new Server();
        // TODO: Figure out how to keep Runner open forever
        Thread.sleep(3000000);
    }
}
