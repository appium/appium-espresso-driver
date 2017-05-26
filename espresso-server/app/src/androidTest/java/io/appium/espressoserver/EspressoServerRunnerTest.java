package io.appium.espressoserver;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import io.appium.espressoserver.lib.Http.Server;
import io.appium.espressoserver.lib.Exceptions.ServerErrorException;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoServerRunnerTest {

    // TODO: Can we make this rule optional via ENV variable?
    /*@Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);*/

    @Test
    public void startEspressoServer() throws InterruptedException, IOException, ServerErrorException {
        new Server();
        // TODO: Figure out how to keep Runner open forever
        Thread.sleep(300000);
    }
}
