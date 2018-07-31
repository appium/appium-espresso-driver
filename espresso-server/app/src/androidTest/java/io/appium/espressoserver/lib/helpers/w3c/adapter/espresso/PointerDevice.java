package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

public class PointerDevice {
    private final int deviceId;
    private final long startTime;

    public PointerDevice(int deviceId, long startTime) {
        this.deviceId = deviceId;
        this.startTime = startTime;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public long getStartTime() {
        return startTime;
    }
}
