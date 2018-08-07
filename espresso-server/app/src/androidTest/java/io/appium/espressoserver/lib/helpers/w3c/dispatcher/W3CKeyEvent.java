package io.appium.espressoserver.lib.helpers.w3c.dispatcher;


public class W3CKeyEvent {
    private String key;
    private String code;
    private int location;
    private int keyCode;
    private int charCode;
    private int which;
    private boolean altKey;
    private boolean shiftKey;
    private boolean ctrlKey;
    private boolean metaKey;
    private boolean repeat;
    private boolean isComposing = false;

    public String logMessage() {
        return String.format(
                "key=[%s] code=[%s] altKey=[%s] shiftKey=[%s] ctrlKey=[%s] metaKey=[%s]",
                key, code, altKey, shiftKey, ctrlKey, metaKey
        );
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public boolean isAltKey() {
        return altKey;
    }

    public void setAltKey(boolean altKey) {
        this.altKey = altKey;
    }

    public boolean isShiftKey() {
        return shiftKey;
    }

    public void setShiftKey(boolean shiftKey) {
        this.shiftKey = shiftKey;
    }

    public boolean isCtrlKey() {
        return ctrlKey;
    }

    public void setCtrlKey(boolean ctrlKey) {
        this.ctrlKey = ctrlKey;
    }

    public boolean isMetaKey() {
        return metaKey;
    }

    public void setMetaKey(boolean metaKey) {
        this.metaKey = metaKey;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isComposing() {
        return isComposing;
    }

    public void setComposing(boolean composing) {
        isComposing = composing;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getCharCode() {
        return charCode;
    }

    public void setCharCode(int charCode) {
        this.charCode = charCode;
    }

    public int getWhich() {
        return which;
    }

    public void setWhich(int which) {
        this.which = which;
    }
}