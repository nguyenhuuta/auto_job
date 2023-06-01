package com.autojob.shopee;

import com.autojob.base.BaseController;
import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.MessageListView;
import com.autojob.task.BaseWebViewTask;
import rx.subjects.BehaviorSubject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by OpenYourEyes on 17/04/2023
 */
public class ShopeeController extends BaseController {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    ShopeeTask task;

    public ShopeeController(AccountModel model) {
        super(model);
        init();
    }

    void init() {
        task = new ShopeeTask(accountModel);
//        executorService.scheduleWithFixedDelay(task, 1, 10, TimeUnit.MINUTES);
    }

//    @Override
//    public BehaviorSubject<MessageListView> triggerCurrentHistory() {
//        return task.triggerCurrentHistory;
//    }

    @Override
    public void runNow() {
//        executorService.scheduleWithFixedDelay(task, 0, 10, TimeUnit.MINUTES);
    }

    @Override
    public void changeJob(int jobId) {
        task.currentJob = jobId;
//        executorService.execute(task);
    }

    @Override
    public void stopJob() {
        task.status = BaseWebViewTask.FORCE_STOP;
    }

    @Override
    public void bringDriverToFront() {

    }
}



