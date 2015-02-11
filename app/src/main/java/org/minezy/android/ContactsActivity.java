package org.minezy.android;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;

import static java.util.logging.Logger.getLogger;


public class ContactsActivity extends ActionBarActivity {

    private static final List<Contact> INVALID_CONTACTS_LIST =
        Arrays.asList(new Contact[]{new Contact("<invalid>", "<invalid>")});

    private ContactAdapter mCotactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_activity);

        mCotactsAdapter = new ContactAdapter(this);
        getContactListView().setAdapter(mCotactsAdapter);
        getContactListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ContactsActivity.this, EmailsActivity.class);
                Contact contact = mCotactsAdapter.getItem(i);
                intent.putExtra(EmailsActivity.ARG_CONTACT, contact.getEmail());
                startActivity(intent);
            }
        });

        new RetrieveContactsTask(new MinezyApiV1(this)).execute(getContactForUserAccount());

        WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://bl.ocks.org/mbostock/raw/4062045/");
    }

    private String getContactForUserAccount() {
        return PreferenceManager.getDefaultSharedPreferences(this)
            .getString(getString(R.string.pref_account_email),
                getString(R.string.pref_default_account_email));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
        return true;
    }

    private HListView getContactListView() {
        return (HListView) findViewById(R.id.contactsHList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RetrieveContactsTask extends AsyncTask<String, Void, List<Contact>> {
        private final MinezyApiV1 mMinezyApiV1;

        public RetrieveContactsTask(MinezyApiV1 minezyApiV1) {
            mMinezyApiV1 = minezyApiV1;
        }

        @Override
        protected List<Contact> doInBackground(String... left) {
            try {
                return mMinezyApiV1.getContactsWithLeft(left[0]);
            } catch (MinezyApiV1.MinezyApiException | MinezyConnection.MinezyConnectionException e) {
                getLogger(getClass().getName()).log(Level.SEVERE, "Error retrieving contacts:", e);
                return INVALID_CONTACTS_LIST;
            }
        }

        @Override
        protected void onPostExecute(List<Contact> contacts) {
            super.onPostExecute(contacts);
            if (contacts.size() > 0) {
                mCotactsAdapter.addAll(contacts);
            }
        }
    }

    class ContactAdapter extends ArrayAdapter<Contact> {
        private LayoutInflater mInflater;

        public ContactAdapter(Context context) {
            super(context, R.layout.contacts_item);
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
                convertView = mInflater.inflate(R.layout.contacts_item, parent, false);
            }

            String name = getItem(position).getName();

            TextView textView = (TextView) convertView.findViewById(R.id.nameTextView);
            textView.setText(name);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
            imageView.setImageDrawable(TextDrawable.builder()
                .buildRound(name.isEmpty() ? "" : name.substring(0, 1), ColorGenerator.MATERIAL.getRandomColor()));

            return convertView;
        }
    }
}
