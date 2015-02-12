package org.minezy.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static java.util.logging.Logger.getLogger;

public class EmailsActivityPresenter {

    private static final List<Email> INVALID_EMAILS_LIST =
        Arrays.asList(new Email[]{});

    private final EmailsActivityController mController;
    private final Intent mIntent;

    public EmailsActivityPresenter(EmailsActivityController controller, Intent intent) {
        mController = controller;
        mIntent = intent;
    }

    private class RetrieveEmailsList extends AsyncTask<String, Void, List<Email>> {
        private final MinezyApiV1 mMinezyApiV1;

        public RetrieveEmailsList(MinezyApiV1 minezyApiV1) {
            mMinezyApiV1 = minezyApiV1;
        }

        @Override
        protected List<Email> doInBackground(String... contacts) {
            try {
                return mMinezyApiV1.getEmailsWithLeftAndRight(contacts[0], contacts[1]);
            } catch (MinezyApiV1.MinezyApiException | MinezyConnection.MinezyConnectionException e) {
                getLogger(getClass().getName()).log(Level.SEVERE, "Error retrieving emails:", e);
                return INVALID_EMAILS_LIST;
            }
        }

        @Override
        protected void onPostExecute(List<Email> emails) {
            super.onPostExecute(emails);
            if (emails.size() > 0) {
                mController.setEmails(emails);
            }
        }
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
            new RetrieveEmailsList(new MinezyApiV1(mController.getContext()))
                .execute(getContactForUserAccount(), getToContactEmail());
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

