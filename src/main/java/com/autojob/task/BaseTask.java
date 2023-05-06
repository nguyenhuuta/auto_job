package com.autojob.task;

import com.autojob.utils.Logger;
import org.openqa.selenium.WebDriver;

public abstract class BaseTask<I extends IBaseTaskCallback> extends BaseWebViewModel implements Runnable {
    private final int taskId;
    private I baseTaskCallback;

    public BaseTask(int taskId,WebDriver webDriver, I callback) {
        super(webDriver);
        this.taskId = taskId;
        this.baseTaskCallback = callback;
    }

    @Override
    public void run() {

    }

    public int getTaskId() {
        return taskId;
    }

    public I getCallback() {
        return baseTaskCallback;
    }

    public void setCallback(I baseTaskCallback) {
        this.baseTaskCallback = baseTaskCallback;
    }

    public void print(String tag, String message) {
        Logger.d(tag, message);
    }

    public void print(String message) {
        Logger.d(getTag(), message);
    }

    public void print(String message,Throwable throwable) {
        Logger.d(getTag(), message,throwable);
    }
}
