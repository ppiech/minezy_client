package org.minezy.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import org.minezy.android.R;
import org.minezy.android.data.MinezyApiV1;
import org.minezy.android.data.MinezyConnection;
import org.minezy.android.model.Contact;
import org.minezy.android.utils.Parametrized;
import org.minezy.android.utils.TaskChainFactory;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.inject.Inject;
import javax.inject.Named;

import static java.util.logging.Logger.getLogger;

public class ContactsActivityPresenter {

    private static final List<Contact> INVALID_CONTACTS_LIST =
        Arrays.asList(new Contact[]{new Contact("<invalid>", "<invalid>")});

    @Inject
    MinezyApiV1 mMinezyApiV1;

    @Inject
    @Named("thread per run")
    TaskChainFactory mTaskChainFactory;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    private ContactsActivityController mController;

    public ContactsActivityPresenter() {
    }

    private String getString(int resId) {
        return mController.getContext().getString(resId);
    }

    private String getContactForUserAccount() {
        return mSharedPreferences.getString(getString(R.string.pref_account_email),
            getString(R.string.pref_default_account_email));
    }

    public void onCreate(ContactsActivityController controller) {
        mController = controller;
        mTaskChainFactory.create()
            .param(getContactForUserAccount())
            .background(new Parametrized<String, List<Contact>>() {
                @Override
                public List<Contact> perform(String left) throws Exception {
                    try {
                        return mMinezyApiV1.getContactsWithLeft(left);
                    } catch (MinezyApiV1.MinezyApiException | MinezyConnection.MinezyConnectionException e) {
                        getLogger(getClass().getName()).log(Level.SEVERE, "Error retrieving contacts:", e);
                        return INVALID_CONTACTS_LIST;
                    }
                }
            })
            .main(new Parametrized<List<Contact>, Void>() {
                @Override
                public Void perform(List<Contact> contacts) throws Exception {
                    if (contacts.size() > 0) {
                        mController.setContacts(contacts);
                    }
                    return null;
                }
            })
            .execute();
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

