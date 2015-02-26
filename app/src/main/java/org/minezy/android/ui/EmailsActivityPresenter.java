package org.minezy.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;

import org.minezy.android.R;
import org.minezy.android.data.MinezyApiV1;
import org.minezy.android.data.MinezyConnection;
import org.minezy.android.model.Email;
import org.minezy.android.utils.Parametrized;
import org.minezy.android.utils.TaskChainFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.inject.Inject;
import javax.inject.Named;

import static java.util.logging.Logger.getLogger;

public class EmailsActivityPresenter {
    private static final List<Email> INVALID_EMAILS_LIST =
        Arrays.asList(new Email[]{});

    @Inject
    @Named("raw thread")
    TaskChainFactory mTaskChainFactory;

    @Inject
    MinezyApiV1 mMinezyApiV1;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    private EmailsActivityController mController;
    private Intent mIntent;

    public EmailsActivityPresenter() {
    }

    private String getString(int resId) {
        return mController.getContext().getString(resId);
    }

    private String getContactForUserAccount() {
        return mSharedPreferences.getString(getString(R.string.pref_account_email),
            getString(R.string.pref_default_account_email));
    }


    private String getToContactEmail() {
        return mIntent.getStringExtra(EmailsActivity.ARG_CONTACT);
    }

    public void onCreate(EmailsActivityController controller, Intent intent) {
        mController = controller;
        mIntent = intent;
        if (mIntent != null) {
            mTaskChainFactory.create()
                .background(new Callable<List<Email>>() {
                    @Override
                    public List<Email> call() throws Exception {
                        try {
                            return mMinezyApiV1.getEmailsWithLeftAndRight(getContactForUserAccount(),
                                getToContactEmail());
                        } catch (MinezyApiV1.MinezyApiException | MinezyConnection.MinezyConnectionException e) {
                            getLogger(getClass().getName()).log(Level.SEVERE, "Error retrieving emails:", e);
                            return INVALID_EMAILS_LIST;
                        }
                    }
                })
                .main(new Parametrized<List<Email>, Void>() {
                    @Override
                    public Void perform(List<Email> result) throws Exception {
                        if (result.size() > 0) {
                            mController.setEmails(result);
                        }
                        return null;
                    }
                }).execute();
        }
    }

    public void onDestroy() {

    }

    public void onEmailsItemUpdate(EmailsItemController item) {
        item.setName(item.getEmail().getSubject());
    }
}

