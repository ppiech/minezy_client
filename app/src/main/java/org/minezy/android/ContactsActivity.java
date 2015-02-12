package org.minezy.android;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;

import java.util.Arrays;
import java.util.List;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;


public class ContactsActivity extends ActionBarActivity implements ContactsActivityController {

    private static final List<Contact> INVALID_CONTACTS_LIST =
        Arrays.asList(new Contact[]{new Contact("<invalid>", "<invalid>")});

    private ContactsActivityPresenter mPresenter;

    private ContactAdapter mCotactsAdapter;

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_activity);
        mCotactsAdapter = new ContactAdapter(this);

        getContactListView().setAdapter(mCotactsAdapter);
        getContactListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mPresenter.onContactsItemSelected(new ContactsItemController(view, mCotactsAdapter.getItem(i), i));
            }
        });

        WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://bl.ocks.org/mbostock/raw/4062045/");

        mPresenter = new ContactsActivityPresenter(this);
        mPresenter.onCreate();
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
        if (mPresenter.onMenuItemSelected(item.getItemId())) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setContacts(List<Contact> contacts) {
        mCotactsAdapter.clear();
        mCotactsAdapter.addAll(contacts);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
        mPresenter = null;
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

            mPresenter.onContactsItemUpdate(new ContactsItemController(convertView, getItem(position), position));
            return convertView;
        }
    }
}
