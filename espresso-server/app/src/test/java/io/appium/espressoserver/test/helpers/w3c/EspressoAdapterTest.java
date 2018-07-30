package io.appium.espressoserver.test.helpers.w3c;

import org.junit.Before;
import org.junit.Test;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys;

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_5;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_A;
import static android.view.KeyEvent.KEYCODE_COMMA;
import static android.view.KeyEvent.KEYCODE_GRAVE;
import static android.view.KeyEvent.KEYCODE_Z;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EspressoAdapterTest {

    EspressoW3CActionAdapter adapter;

    @Before
    public void before() {
        adapter = new EspressoW3CActionAdapter();
    }

    @Test
    public void shouldMapNormalizedKeyToAndroidKey() throws AppiumException {
        String normalizedKey = NormalizedKeys.COMMA;
        int keyCode = adapter.getKeyCode(normalizedKey, 0);
        assertEquals(keyCode, KEYCODE_COMMA);
    }

    @Test
    public void shouldMapAlphanumericUnicodeToAndroidKey() throws AppiumException {
        String[] inputKeys = new String[]{ "a", "A", "z", "Z", "0", "5", "9" };
        int[] outKeyCodes = new int[]{ KEYCODE_A, KEYCODE_A, KEYCODE_Z, KEYCODE_Z, KEYCODE_0, KEYCODE_5, KEYCODE_9 };
        for (int i=0; i<inputKeys.length; i++) {
            assertEquals(adapter.getKeyCode(inputKeys[i], 0), outKeyCodes[i]);
        }
    }

    @Test
    public void shouldMapSpecialUnicodeCharactersToAndroidKey() throws AppiumException {
        assertEquals(adapter.getKeyCode("`", 0), KEYCODE_GRAVE);
    }

    @Test
    public void shouldThrowIfUnicodeHasNoMapping() throws AppiumException {
        try {
            adapter.getKeyCode("/u1234", 0);
        } catch (InvalidArgumentException e) {
            assertTrue(e.getMessage().contains("does not map"));
            return;
        }

        fail("Should have called 'InvalidArgumentException'");
    }
}
