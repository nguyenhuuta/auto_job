package com.autojob.base;

import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.MessageListView;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

/**
 * Created by OpenYourEyes on 12/04/2023
 */
public abstract class BaseController {
    public AccountModel accountModel;

    public BaseController(AccountModel accountModel) {
        this.accountModel = accountModel;
    }

    public abstract void runNow();

    public void changeJob(int jobId) {

    }

    public void stopJob() {

    }

    public abstract void bringDriverToFront();
}

