package org.minezy.android.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.minezy.android.model.Contact;
import org.minezy.android.model.Email;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MinezyApiV1 {
    public MinezyApiV1(Context context) {
        this(context, new MinezyConnection(context));
    }

    MinezyApiV1(Context context, MinezyConnection connection) {
        mConnection = connection;
        mContext = context;
    }

    private final Context mContext;
    private final MinezyConnection mConnection;

    public static class MinezyApiException extends Exception {
        public MinezyApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public List<Contact> getContacts() throws MinezyApiException, MinezyConnection.MinezyConnectionException {
        String response = mConnection.requestGet("/1/100/contacts/?limit=20");
        try {
            return parseContacts(new JSONObject(response));
        } catch (JSONException e) {
            throw new MinezyApiException("Error parsing getContacts() response: " + response, e);
        }
    }

    public List<Contact> getContactsWithLeft(String left)
        throws MinezyApiException, MinezyConnection.MinezyConnectionException {
        String response = mConnection.requestGet("/1/100/contacts/?limit=20&left=" + encode(left));
        try {
            return parseContacts(new JSONObject(response));
        } catch (JSONException e) {
            throw new MinezyApiException("Error parsing getContacts() response: " + response, e);
        }
    }

    public List<Email> getEmailsWithLeftAndRight(String left, String right)
        throws MinezyApiException, MinezyConnection.MinezyConnectionException {
        String response =
            mConnection
                .requestGet("/1/100/emails/?limit=20&order=asc&left=" + encode(left) + "&right=" + encode(right));
        try {
            return parseEmails(new JSONObject(response));
        } catch (JSONException e) {
            throw new MinezyApiException("Error parsing getEmails() response: " + response, e);
        }
    }


    private List<Contact> parseContacts(JSONObject json) throws JSONException {
        JSONArray contactsJson = json.getJSONObject("contacts").getJSONArray("contact");
        List<Contact> contacts = new ArrayList<>(contactsJson.length());
        for (int i = 0; i < contactsJson.length(); i++) {
            JSONObject contactJson = contactsJson.getJSONObject(i);
            contacts.add(new Contact(contactJson.getString("email"), contactJson.getString("name")));
        }
        return contacts;
    }


    private List<Email> parseEmails(JSONObject json) throws JSONException {
        JSONArray emailsJson = json.getJSONObject("emails").getJSONArray("email");
        List<Email> emails = new ArrayList<>(emailsJson.length());
        for (int i = 0; i < emailsJson.length(); i++) {
            JSONObject emailJson = emailsJson.getJSONObject(i);
            JSONObject dateJson = emailJson.getJSONObject("date");
            long utc = dateJson.getLong("utc");
            emails.add(new Email(emailJson.getString("id"), new Date(utc), emailJson.getString("subject")));
        }
        return emails;
    }

    private String encode(String toEncode) {
        try {
            return URLEncoder.encode(toEncode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Programming error
            throw new RuntimeException("Invalid encoding.", e);
        }
    }

}
