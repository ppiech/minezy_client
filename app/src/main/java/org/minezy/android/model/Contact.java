package org.minezy.android.model;

public class Contact {
    private final String mEmail;
    private final String mName;

    public Contact(String email, String name) {
        mEmail = email;
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public String getEmail() {
        return mEmail;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Contact && ((Contact) o).mEmail.equals(mEmail);
    }

    @Override
    public int hashCode() {
        return mEmail.hashCode();
    }
}
