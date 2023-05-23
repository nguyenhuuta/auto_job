package com.autojob.tiktok;

import com.autojob.base.BaseController;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;
import com.autojob.task.BaseWebViewTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by OpenYourEyes on 15/05/2023
 */
public class TiktokController extends BaseController {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    TiktokTask task;

    public TiktokController(AccountModel model, WebDriverCallback webDriverCallback) {
        super(model);
        task = new TiktokTask(accountModel, webDriverCallback);
    }

    @Override
    public void runNow() {
        task.startWeb();
        executorService.scheduleWithFixedDelay(task, 0, 10, TimeUnit.MINUTES);
    }

    @Override
    public void changeJob(int jobId) {
        executorService.execute(task);
    }

    @Override
    public void stopJob() {
        task.status = BaseWebViewTask.FORCE_STOP;
    }

    @Override
    public void bringDriverToFront() {
        task.bringWebDriverToFront();
    }
}
