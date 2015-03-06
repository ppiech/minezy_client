package org.minezy.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
        Arrays.asList(new Contact[]{new Contact("invalid", "invalid")});

    @Inject
    MinezyApiV1 mMinezyApiV1;

    @Inject
    @Named("thread per run")
    TaskChainFactory mTaskChainFactory;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    private ContactsActivityController mController;
    private List<Contact> mContacts;
    private boolean mWebViewLoading;
    private Runnable mOnWebViewLoadedTask;

    public ContactsActivityPresenter() {
    }

    private String getString(int resId) {
        return mController.getContext().getString(resId);
    }

    private String getEmailForUserAccount() {
        return mSharedPreferences.getString(getString(R.string.pref_account_email),
            getString(R.string.pref_default_account_email));
    }

    private Contact getContactForUserAccount() {
        return new Contact(getEmailForUserAccount(), getString(R.string.user_display_name));
    }

    public void onCreate(ContactsActivityController controller) {
        mController = controller;
        mTaskChainFactory.create()
            .param(getEmailForUserAccount())
            .background(new Parametrized<String, List<Contact>>() {
                @Override
                public List<Contact> perform(String left) throws Exception {
                    try {
                        return mMinezyApiV1.getContacts(left);
                    } catch (MinezyApiV1.MinezyApiException | MinezyConnection.MinezyConnectionException e) {
                        getLogger(getClass().getName()).log(Level.SEVERE, "Error retrieving contacts:", e);
                        return INVALID_CONTACTS_LIST;
                    }
                }
            })
            .main(new Parametrized<List<Contact>, Void>() {
                @Override
                public Void perform(List<Contact> contacts) throws Exception {
                    mContacts = contacts;
                    if (contacts.size() > 0) {
                        mController.setContacts(contacts);
                        Runnable webViewTask = new Runnable() {
                            @Override
                            public void run() {
                                mController.setWebviewData(makeGraphDataForContacts(mContacts, null));
                            }
                        };
                        if (mWebViewLoading) {
                            mOnWebViewLoadedTask = webViewTask;
                        } else {
                            webViewTask.run();
                        }
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

    public void onContactsItemSelected(final ContactsItemController item) {
        Runnable webViewTask = new Runnable() {
            @Override
            public void run() {
                mController.setWebviewData(makeGraphDataForContacts(mContacts, item.getContact()));
            }
        };
        if (mWebViewLoading) {
            mOnWebViewLoadedTask = webViewTask;
        } else {
            webViewTask.run();
        }

    }

    public void onContactsItemClicked(ContactsItemController item) {
        Intent intent = new Intent(mController.getContext(), EmailsActivity.class);
        intent.putExtra(EmailsActivity.ARG_CONTACT, item.getContact().getEmail());
        mController.startActivity(intent);
    }

    public void onWebViewPageStarted(String url) {
        mWebViewLoading = true;
    }

    public void onWebViewPageFinished(String url) {
        mWebViewLoading = false;
        if (mOnWebViewLoadedTask != null) {
            mOnWebViewLoadedTask.run();
            mOnWebViewLoadedTask = null;
        }
    }

    private String getContactInitials(Contact contact) {
        String name = contact.getName();
        return name.isEmpty() ? "" : name.substring(0, 1);
    }

    private JSONObject makeGraphDataForContacts(List<Contact> contacts, Contact selected) {
        try {
            JSONArray nodes = new JSONArray();
            JSONArray links = new JSONArray();
            int indexInNodes = 0;
            nodes.put(makeContactJson(getContactForUserAccount()));
            indexInNodes++;
            for (Contact contact : contacts) {
                nodes.put(makeContactJson(contact));
                links.put(makeLinkJson(0, indexInNodes, 1));
                indexInNodes++;
            }
            JSONObject dataJson = new JSONObject();
            dataJson.put("nodes", nodes);
            dataJson.put("links", links);
            return dataJson;
        } catch (JSONException e) {
            Log.e("Minezy", "Error building json", e);
        }
        return new JSONObject();
    }

    private JSONObject makeContactJson(Contact contact) throws JSONException {
        JSONObject contactJson = new JSONObject();
        contactJson.put("name", contact.getName());
        contactJson.put("group", 1);
        return contactJson;
    }

    private JSONObject makeLinkJson(int source, int target, int value) throws JSONException {
        JSONObject linkJson = new JSONObject();
        linkJson.put("source", source);
        linkJson.put("target", target);
        linkJson.put("value", value);
        return linkJson;
    }
}

