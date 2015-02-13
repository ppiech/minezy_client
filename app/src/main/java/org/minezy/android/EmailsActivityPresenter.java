package org.minezy.android;

import android.content.Intent;
import android.preference.PreferenceManager;

import org.minezy.android.utils.AsyncTaskUtil;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static java.util.logging.Logger.getLogger;

public class EmailsActivityPresenter {
    private static final List<Email> INVALID_EMAILS_LIST =
        Arrays.asList(new Email[]{});

    private final AsyncTaskUtil mAsyncTaskUtil;
    private final MinezyApiV1 mMinezyApiV1;

    private final EmailsActivityController mController;
    private final Intent mIntent;

    public EmailsActivityPresenter(EmailsActivityController controller, Intent intent) {
        this(controller, intent, new AsyncTaskUtil(), new MinezyApiV1(controller.getContext()));
    }

    public EmailsActivityPresenter(EmailsActivityController controller, Intent intent, AsyncTaskUtil asyncTaskUtil,
                                   MinezyApiV1 minezyApiV1) {
        mAsyncTaskUtil = asyncTaskUtil;
        mMinezyApiV1 = minezyApiV1;
        mController = controller;
        mIntent = intent;
    }


    private String getString(int resId) {
        return mController.getContext().getString(resId);
    }

    private String getContactForUserAccount() {
        return PreferenceManager.getDefaultSharedPreferences(mController.getContext()).
            getString(getString(R.string.pref_account_email),
                getString(R.string.pref_default_account_email));
    }


    private String getToContactEmail() {
        return mIntent.getStringExtra(EmailsActivity.ARG_CONTACT);
    }

    public void onCreate() {
        if (mIntent != null) {
            mAsyncTaskUtil.execute(
                new AsyncTaskUtil.Executable<List<Email>>() {
                    @Override
                    public List<Email> doInBackground() {
                        try {
                            return mMinezyApiV1
                                .getEmailsWithLeftAndRight(getContactForUserAccount(), getToContactEmail());
                        } catch (MinezyApiV1.MinezyApiException | MinezyConnection.MinezyConnectionException e) {
                            getLogger(getClass().getName()).log(Level.SEVERE, "Error retrieving emails:", e);
                            return INVALID_EMAILS_LIST;
                        }
                    }

                    @Override
                    public void onPostExecute(List<Email> result) {
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

    public void onEmailssItemSelected(EmailsItemController item) {
    }
}

