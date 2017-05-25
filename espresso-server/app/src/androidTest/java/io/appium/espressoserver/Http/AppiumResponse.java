package io.appium.espressoserver.Http;

public class AppiumResponse {
    private boolean success;
    private String value;
    private int status;

    public AppiumResponse (int status, boolean success, String value) {
        this.status = status;
        this.success = success;
        this.value = value;
    }

    public boolean isSuccess () {
        return success;
    }

    public void setSuccess (boolean success) {
        this.success = success;
    }

    public void setStatus (int status) {
      this.status = status;
    }

    public int getStatus () {
      return this.status;
    }

    public String getValue () {
        return value;
    }

    public void setValue (String value) {
        this.value = value;
    }
}
