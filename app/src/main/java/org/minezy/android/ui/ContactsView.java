package org.minezy.android.ui;

import org.json.JSONObject;
import org.minezy.android.model.Contact;

import java.util.List;

import rx.Observable;

public interface ContactsView extends ActivityController {

    public void setContacts(List<Contact> contacts);

    public void setWebviewData(JSONObject json);

    public void setActiveContact(Contact contact);

    public Observable<Void> getWebViewFinished();
}