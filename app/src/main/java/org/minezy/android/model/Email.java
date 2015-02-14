package org.minezy.android.model;

import java.util.Date;

public class Email {
    private final String mId;
    private final Date mDate;
    private final String mSubject;


    public Email(String id, Date date, String subject) {
        mId = id;
        mDate = date;
        mSubject = subject;
    }

    public String getId() {
        return mId;
    }

    public Date getDate() {
        return mDate;
    }

    public String getSubject() {
        return mSubject;
    }

    @Override
    public String toString() {
        return mSubject;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Email && ((Email) o).mId.equals(mId);
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }
}
