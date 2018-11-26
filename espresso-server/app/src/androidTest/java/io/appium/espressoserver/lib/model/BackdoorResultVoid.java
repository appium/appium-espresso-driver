package io.appium.espressoserver.lib.model;

import java.util.HashMap;
import java.util.Map;

public class BackdoorResultVoid {
    public static final BackdoorResultVoid instance = new BackdoorResultVoid();

    private BackdoorResultVoid() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object asMap(String methodName, Object receiver,
                        String errorMessage) {
        Map map = new HashMap();
        map.put("error", errorMessage);
        map.put("methodName", methodName);
        map.put("receiverClass", receiver.getClass().getName());
        map.put("receiverString", receiver.toString());
        return map;
    }
}
