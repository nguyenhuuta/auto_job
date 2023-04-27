package com.webseleniumdriver.task;

public interface IBaseTaskCallback<T> {

    void onDoneTask(int taskId,T object);

    void onFailure(int taskId,String message);
}
