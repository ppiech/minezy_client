package org.minezy.android.ui;

import org.minezy.android.model.Contact;

import java.util.List;

public interface ContactsActivityController extends ActivityController {

    public void setContacts(List<Contact> contacts);
}