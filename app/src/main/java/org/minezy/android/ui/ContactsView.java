package org.minezy.android.ui;

import org.json.JSONObject;
import org.minezy.android.model.Contact;

import java.util.List;

public interface ContactsView extends ActivityController {

    public void setContacts(List<Contact> contacts);

    public void setWebviewData(JSONObject json);

    public void setActiveContact(Contact contact);
}