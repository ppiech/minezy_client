package org.minezy.android.ui;

import org.minezy.android.model.Email;

import java.util.List;

public interface EmailsView extends ActivityController {
    public void setEmails(List<Email> emails);

}
