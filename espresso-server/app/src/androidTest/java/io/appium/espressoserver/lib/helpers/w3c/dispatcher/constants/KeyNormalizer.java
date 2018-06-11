package io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants;

import java.util.HashMap;
import java.util.Map;

public class KeyNormalizer {
    private static Map<String, String> normalizedKeyMap = new HashMap<>();
    private static KeyNormalizer instance;

    private KeyNormalizer() {
        normalizedKeyMap.put("\uE000", NormalizedKeys.UNIDENTIFIED);
        normalizedKeyMap.put("\uE001", NormalizedKeys.CANCEL);
        normalizedKeyMap.put("\uE002", NormalizedKeys.HELP);
        normalizedKeyMap.put("\uE003", NormalizedKeys.BACKSPACE);
        normalizedKeyMap.put("\uE004", NormalizedKeys.TAB);
        normalizedKeyMap.put("\uE005", NormalizedKeys.CLEAR);
        normalizedKeyMap.put("\uE006", NormalizedKeys.RETURN);
        normalizedKeyMap.put("\uE007", NormalizedKeys.ENTER);
        normalizedKeyMap.put("\uE008", NormalizedKeys.SHIFT);
        normalizedKeyMap.put("\uE009", NormalizedKeys.CONTROL);
        normalizedKeyMap.put("\uE00A", NormalizedKeys.ALT);
        normalizedKeyMap.put("\uE00B", NormalizedKeys.PAUSE);
        normalizedKeyMap.put("\uE00C", NormalizedKeys.ESCAPE);
        normalizedKeyMap.put("\uE00D", NormalizedKeys.WHITESPACE );
        normalizedKeyMap.put("\uE00E", NormalizedKeys.PAGEUP);
        normalizedKeyMap.put("\uE00F", NormalizedKeys.PAGEDOWN);
        normalizedKeyMap.put("\uE010", NormalizedKeys.END);
        normalizedKeyMap.put("\uE011", NormalizedKeys.HOME);
        normalizedKeyMap.put("\uE012", NormalizedKeys.ARROW_LEFT);
        normalizedKeyMap.put("\uE013", NormalizedKeys.ARROW_UP);
        normalizedKeyMap.put("\uE014", NormalizedKeys.ARROW_RIGHT);
        normalizedKeyMap.put("\uE015", NormalizedKeys.ARROW_DOWN);
        normalizedKeyMap.put("\uE016", NormalizedKeys.INSERT);
        normalizedKeyMap.put("\uE017", NormalizedKeys.DELETE);
        normalizedKeyMap.put("\uE018", NormalizedKeys.SEMICOLON);
        normalizedKeyMap.put("\uE019", NormalizedKeys.EQUALS);
        normalizedKeyMap.put("\uE01A", NormalizedKeys.ZERO);
        normalizedKeyMap.put("\uE01B", NormalizedKeys.ONE);
        normalizedKeyMap.put("\uE01C", NormalizedKeys.TWO);
        normalizedKeyMap.put("\uE01D", NormalizedKeys.THREE);
        normalizedKeyMap.put("\uE01E", NormalizedKeys.FOUR);
        normalizedKeyMap.put("\uE01F", NormalizedKeys.FIVE);
        normalizedKeyMap.put("\uE020", NormalizedKeys.SIX);
        normalizedKeyMap.put("\uE021", NormalizedKeys.SEVEN);
        normalizedKeyMap.put("\uE022", NormalizedKeys.EIGHT);
        normalizedKeyMap.put("\uE023", NormalizedKeys.NINE);
        normalizedKeyMap.put("\uE024", NormalizedKeys.ASTERISK);
        normalizedKeyMap.put("\uE025", NormalizedKeys.PLUS);
        normalizedKeyMap.put("\uE026", NormalizedKeys.COMMA);
        normalizedKeyMap.put("\uE027", NormalizedKeys.HYPHEN);
        normalizedKeyMap.put("\uE028", NormalizedKeys.PERIOD);
        normalizedKeyMap.put("\uE029", NormalizedKeys.FORWARD_SLASH);
        normalizedKeyMap.put("\uE031", NormalizedKeys.F1);
        normalizedKeyMap.put("\uE032", NormalizedKeys.F2);
        normalizedKeyMap.put("\uE033", NormalizedKeys.F3);
        normalizedKeyMap.put("\uE034", NormalizedKeys.F4);
        normalizedKeyMap.put("\uE035", NormalizedKeys.F5);
        normalizedKeyMap.put("\uE036", NormalizedKeys.F6);
        normalizedKeyMap.put("\uE037", NormalizedKeys.F7);
        normalizedKeyMap.put("\uE038", NormalizedKeys.F8);
        normalizedKeyMap.put("\uE039", NormalizedKeys.F9);
        normalizedKeyMap.put("\uE03A", NormalizedKeys.F10);
        normalizedKeyMap.put("\uE03B", NormalizedKeys.F11);
        normalizedKeyMap.put("\uE03C", NormalizedKeys.F12);
        normalizedKeyMap.put("\uE03D", NormalizedKeys.META);
        normalizedKeyMap.put("\uE040", NormalizedKeys.ZENKAKU_HANKAKU);
        normalizedKeyMap.put("\uE050", NormalizedKeys.SHIFT);
        normalizedKeyMap.put("\uE051", NormalizedKeys.CONTROL);
        normalizedKeyMap.put("\uE052", NormalizedKeys.ALT);
        normalizedKeyMap.put("\uE053", NormalizedKeys.META);
        normalizedKeyMap.put("\uE054", NormalizedKeys.PAGEUP);
        normalizedKeyMap.put("\uE055", NormalizedKeys.PAGEDOWN);
        normalizedKeyMap.put("\uE056", NormalizedKeys.END);
        normalizedKeyMap.put("\uE057", NormalizedKeys.HOME);
        normalizedKeyMap.put("\uE058", NormalizedKeys.ARROW_LEFT);
        normalizedKeyMap.put("\uE059", NormalizedKeys.ARROW_UP);
        normalizedKeyMap.put("\uE05A", NormalizedKeys.ARROW_RIGHT);
        normalizedKeyMap.put("\uE05B", NormalizedKeys.ARROW_DOWN);
        normalizedKeyMap.put("\uE05C", NormalizedKeys.INSERT);
        normalizedKeyMap.put("\uE05D", NormalizedKeys.DELETE);
    }

    public String getNormalizedKey(String key) {
        if (normalizedKeyMap.containsKey(key)) {
            return normalizedKeyMap.get(key);
        }
        return key;
    }

    public static synchronized KeyNormalizer getInstance () {
        if (instance == null) {
            instance = new KeyNormalizer();
        }
        return instance;
    }
}
