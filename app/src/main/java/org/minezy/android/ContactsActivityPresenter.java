package org.minezy.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static java.util.logging.Logger.getLogger;

public class ContactsActivityPresenter {

    private static final List<Contact> INVALID_CONTACTS_LIST =
        Arrays.asList(new Contact[]{new Contact("<invalid>", "<invalid>")});

    private final ContactsActivityController mController;

    public ContactsActivityPresenter(ContactsActivityController controller) {
        mController = controller;
    }

    private class RetrieveContactsTask extends AsyncTask<String, Void, List<Contact>> {
        private final MinezyApiV1 mMinezyApiV1;

        public RetrieveContactsTask(MinezyApiV1 minezyApiV1) {
            mMinezyApiV1 = minezyApiV1;
        }

        @Override
        protected List<Contact> doInBackground(String... left) {
            try {
                return mMinezyApiV1.getContactsWithLeft(left[0]);
            } catch (MinezyApiV1.MinezyApiException | MinezyConnection.MinezyConnectionException e) {
                getLogger(getClass().getName()).log(Level.SEVERE, "Error retrieving contacts:", e);
                return INVALID_CONTACTS_LIST;
            }
        }

        @Override
        protected void onPostExecute(List<Contact> contacts) {
            super.onPostExecute(contacts);
            if (contacts.size() > 0) {
                mController.setContacts(contacts);
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


    public void onCreate() {
        new RetrieveContactsTask(new MinezyApiV1(mController.getContext())).execute(getContactForUserAccount());
    }

    public void onDestroy() {

    }

    public boolean onMenuItemSelected(int id) {
        if (id == R.id.action_settings) {
            mController.startActivity(new Intent(mController.getContext(), SettingsActivity.class));
            return true;
        }
        return false;
    }

    public void onContactsItemUpdate(ContactsItemController item) {
        item.setName(item.getContact().getName());
        item.setImageDrawable(TextDrawable.builder()
            .buildRound(getContactInitials(item.getContact()), ColorGenerator.MATERIAL.getRandomColor()));
    }

    public void onContactsItemSelected(ContactsItemController item) {
        Intent intent = new Intent(mController.getContext(), EmailsActivity.class);
        intent.putExtra(EmailsActivity.ARG_CONTACT, item.getContact().getEmail());
        mController.startActivity(intent);
    }

    private String getContactInitials(Contact contact) {
        String name = contact.getName();
        return name.isEmpty() ? "" : name.substring(0, 1);
    }
}

