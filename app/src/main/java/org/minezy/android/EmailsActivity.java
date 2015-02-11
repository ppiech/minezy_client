package org.minezy.android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static java.util.logging.Logger.getLogger;

public class EmailsActivity extends ActionBarActivity {

    public static final String ARG_CONTACT = "contact";

    private static final List<Email> INVALID_EMAILS_LIST =
        Arrays.asList(new Email[]{});

    private EmailAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emails_activity);
        mAdapter = new EmailAdapter(this);

        ((ListView) findViewById(R.id.emailsList)).setAdapter(mAdapter);

        String contact = getContactFromIntent();
        if (contact != null) {
            new RetrieveEmailsTask(new MinezyApiV1(this)).execute(getContactForUserAccount(), contact);
        }
    }

    private String getContactForUserAccount() {
        return PreferenceManager.getDefaultSharedPreferences(this)
            .getString(getString(R.string.pref_account_email),
                getString(R.string.pref_default_account_email));
    }

    private String getContactFromIntent() {
        if (getIntent() != null) {
            return getIntent().getStringExtra(ARG_CONTACT);
        }
        return null;
    }

    private class RetrieveEmailsTask extends AsyncTask<String, Void, List<Email>> {
        private final MinezyApiV1 mMinezyApiV1;

        public RetrieveEmailsTask(MinezyApiV1 minezyApiV1) {
            mMinezyApiV1 = minezyApiV1;
        }

        @Override
        protected List<Email> doInBackground(String... contacts) {
            try {
                return mMinezyApiV1.getEmailsWithLeftAndRight(contacts[0], contacts[1]);
            } catch (MinezyApiV1.MinezyApiException | MinezyConnection.MinezyConnectionException e) {
                getLogger(getClass().getName()).log(Level.SEVERE, "Error retrieving emails:", e);
                return INVALID_EMAILS_LIST;
            }
        }

        @Override
        protected void onPostExecute(List<Email> emails) {
            super.onPostExecute(emails);
            if (emails.size() > 0) {
                mAdapter.addAll(emails);
            }
        }
    }

    class EmailAdapter extends ArrayAdapter<Email> {
        private LayoutInflater mInflater;

        public EmailAdapter(Context context) {
            super(context, R.layout.emails_item);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.emails_item, parent, false);
            }

            String subject = getItem(position).getSubject();

            TextView textView = (TextView) convertView.findViewById(R.id.messageTextView);
            textView.setText(subject);

//            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
//            imageView.setImageDrawable(TextDrawable.builder()
//                .buildRound(name.isEmpty() ? "" : name.substring(0, 1), ColorGenerator.MATERIAL.getRandomColor()));

            return convertView;
        }
    }
}
