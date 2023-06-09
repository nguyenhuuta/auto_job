package com.autojob.tiktok;

import com.autojob.base.BaseController;
import com.autojob.base.WebDriverCallback;
import com.autojob.model.entities.AccountModel;

import java.util.concurrent.Executors;

/**
 * Created by OpenYourEyes on 15/05/2023
 */
public class TiktokController extends BaseController {
    TiktokParentTask task;

    public TiktokController(AccountModel model, WebDriverCallback webDriverCallback) {
        super(model);
        task = new TiktokParentTask(accountModel, webDriverCallback);
    }

    @Override
    public void runNow() {
        new Thread(() -> task.startWeb()).start();

    }

    @Override
    public void changeJob(int jobId) {
    }

    @Override
    public void stopJob() {
    }

    @Override
    public void bringDriverToFront() {
        task.bringWebDriverToFront();
    }
}
