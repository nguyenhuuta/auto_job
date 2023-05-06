package com.autojob.base;

import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.MessageListView;
import rx.subjects.BehaviorSubject;

/**
 * Created by OpenYourEyes on 12/04/2023
 */
public abstract class BaseController {
    public AccountModel accountModel;

    public BaseController(AccountModel accountModel) {
        this.accountModel = accountModel;
    }

    public abstract BehaviorSubject<MessageListView> triggerCurrentHistory();


    public void startJob(int jobId) {

    }

    public void stopJob() {

    }
}
