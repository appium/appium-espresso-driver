package io.appium.espressoserver;

import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;



/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoServerRunnerTest {

    public class AppiumResponse {
        private boolean success;
        private String message;
    }

    public class App extends NanoHTTPD {

        public App() throws IOException {
            super(8080);
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
        }

        @Override
        public Response serve(IHTTPSession session) {
            Map<String, String> parms = session.getParms();
            String text = parms.get("text");
            AppiumResponse response = new AppiumResponse();

            Gson gson = new Gson();

            try {
                ViewInteraction viewInteraction = onView(withText(text));
                viewInteraction.perform(click());
                response.success = true;
                response.message = "Found element with text: " + text;
            } catch (Exception e) {
                response.success = false;
                response.message = "No element found with text: " + text;
            }
            return newFixedLengthResponse(gson.toJson(response));
        }
    }

    /**
     * A JUnit {@link Rule @Rule} to launch your activity under test. This is a replacement
     * for {@link ActivityInstrumentationTestCase2}.
     * <p>
     * Rules are interceptors which are executed for each test method and will run before
     * any of your setup code in the {@link Before @Before} method.
     * <p>
     * {@link ActivityTestRule} will create and launch of the activity for you and also expose
     * the activity under test. To get a reference to the activity you can use
     * the {@link ActivityTestRule#getActivity()} method.
     */
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void startEspressoServer() throws InterruptedException, IOException {
        new App();
        Thread.sleep(300000);
    }
}