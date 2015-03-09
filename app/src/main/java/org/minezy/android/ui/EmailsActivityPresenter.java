package org.minezy.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;

import org.minezy.android.R;
import org.minezy.android.data.MinezyApiV1;
import org.minezy.android.data.MinezyConnection;
import org.minezy.android.model.Email;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func0;

import static java.util.logging.Logger.getLogger;

public class EmailsActivityPresenter {
    private static final List<Email> INVALID_EMAILS_LIST =
        Arrays.asList(new Email[]{});

    @Inject
    @Named("main")
    Scheduler mMainScheduler;

    @Inject
    @Named("io")
    Scheduler mThreadScheduler;

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
            Observable.
                defer(new Func0<Observable<List<Email>>>() {
                    @Override
                    public Observable<List<Email>> call() {
                        try {
                            return Observable.just(mMinezyApiV1.getEmails(getContactForUserAccount(),
                                getToContactEmail()));
                        } catch (MinezyApiV1.MinezyApiException | MinezyConnection.MinezyConnectionException e) {
                            getLogger(getClass().getName()).log(Level.SEVERE, "Error retrieving emails:", e);
                            return Observable.just(INVALID_EMAILS_LIST);
                        }
                    }
                })
                .subscribeOn(mThreadScheduler)
                .observeOn(mMainScheduler)
                .subscribe(new Action1<List<Email>>() {
                    @Override
                    public void call(List<Email> result) {
                        if (result.size() > 0) {
                            mController.setEmails(result);
                        }
                    }
                });
        }
    }

    public void onDestroy() {

    }

    public void onEmailsItemUpdate(EmailsItemController item) {
        item.setName(item.getEmail().getSubject());
    }
}

