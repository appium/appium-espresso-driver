package io.appium.espressoserver.lib.helpers.w3c.dispatcher;

import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;

public abstract class BaseDispatchResult {

    private Callable<BaseDispatchResult> next;
    public boolean hasNext() {
        return next != null;
    }

    public abstract void perform() throws AppiumException;

    @Nullable
    public Callable<BaseDispatchResult> getNext() {
        return next;
    }

    public void setNext(Callable<BaseDispatchResult> next) {
        this.next = next;
    }
}
