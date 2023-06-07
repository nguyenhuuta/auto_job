package com.autojob.base;

import com.autojob.model.entities.AccountModel;
import com.autojob.model.entities.MessageListView;

import java.util.Date;

/**
 * Created by OpenYourEyes on 19/05/2023
 */

public interface WebDriverCallback {
    void updateListView(int type, MessageListView message);

    void triggerLogin(AccountModel accountModel, boolean needLogin);

    void expiredCookie(AccountModel shop);
}
