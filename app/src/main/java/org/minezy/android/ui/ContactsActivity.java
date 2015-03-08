package org.minezy.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;

import org.json.JSONException;
import org.json.JSONObject;
import org.minezy.android.MinezyApplication;
import org.minezy.android.R;
import org.minezy.android.model.Contact;

import java.util.Arrays;
import java.util.List;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;


public class ContactsActivity extends ActionBarActivity implements ContactsActivityController {

    private static final List<Contact> INVALID_CONTACTS_LIST =
        Arrays.asList(new Contact[]{new Contact("<invalid>", "<invalid>")});

    private ContactsActivityPresenter mPresenter;

    private ContactAdapter mCotactsAdapter;
    private WebView mWebView;

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            JSONObject jsonObject = new JSONObject("{" +
                                                   "  \"nodes\":[" +
                                                   "    {\"name\":\"Myriel\",\"group\":1}," +
                                                   "    {\"name\":\"Napoleon\",\"group\":1}," +
                                                   "    {\"name\":\"Mlle.Baptistine\",\"group\":1}]," +
                                                   "\"links\":[" +
                                                   "    {\"source\":1,\"target\":0,\"value\":1}," +
                                                   "    {\"source\":2,\"target\":0,\"value\":8}," +
                                                   "    {\"source\":2,\"target\":1,\"value\":10}]" +
                                                   "}");
            Log.e("Minezy", jsonObject.toString());
        } catch (JSONException e) {
            Log.e("Minezy", "json", e);
        }

        setContentView(R.layout.contacts_activity);
        mCotactsAdapter = new ContactAdapter(this);

        getContactListView().setAdapter(mCotactsAdapter);
        getContactListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mPresenter.onContactsItemClicked(new ContactsItemController(view, mCotactsAdapter.getItem(i), i));
            }
        });

        getContactListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mPresenter.onContactsItemSelected(new ContactsItemController(view, mCotactsAdapter.getItem(i), i));
            }
        });

        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings settings = mWebView.getSettings();
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("file:///android_asset/graph.html");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (mPresenter != null) {
                    mPresenter.onWebViewPageStarted(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mPresenter != null) {
                    mPresenter.onWebViewPageFinished(url);
                }
            }
        });

        mPresenter = ((MinezyApplication) getApplication()).getObjectGraph().get(ContactsActivityPresenter.class);
        mPresenter.onCreate(this);
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

    public void setWebviewData(JSONObject json) {
        mWebView.loadUrl(
            "javascript: var graph = JSON.parse('" + json.toString() +
            "'); clearGraph(); displayGraph(graph)");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
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
            try {
                return getItem(position).hashCode();
            } catch (IndexOutOfBoundsException e) {
                // Seen HList throw an IOOB, seems like a bug in the view.
                return 0;
            }
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
