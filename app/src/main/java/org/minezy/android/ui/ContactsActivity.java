package org.minezy.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;

import org.json.JSONObject;
import org.minezy.android.MinezyApplication;
import org.minezy.android.R;
import org.minezy.android.model.Contact;
import org.minezy.android.rx.WebViewLoadingEvent;
import org.minezy.android.rx.WebViewObservable;

import java.util.Arrays;
import java.util.List;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;
import rx.Observable;
import rx.functions.Func1;


public class ContactsActivity extends ActionBarActivity implements ContactsView {

    private static final List<Contact> INVALID_CONTACTS_LIST =
        Arrays.asList(new Contact[]{new Contact("<invalid>", "<invalid>")});

    private ContactsPresenter mPresenter;

    private ContactAdapter mCotactsAdapter;
    private WebView mWebView;

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
                mPresenter.onContactsItemClicked(new ContactsItemView(view, mCotactsAdapter.getItem(i), i));
            }
        });

        getContactListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mPresenter.onContactsItemSelected(new ContactsItemView(view, mCotactsAdapter.getItem(i), i));
            }
        });

        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings settings = mWebView.getSettings();
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        // Create web view client before loading page to capture the first pageFinished() event.
        WebViewObservable.pageLoading(mWebView, true).subscribe();
        mWebView.loadUrl("file:///android_asset/graph.html");
        mPresenter = ((MinezyApplication) getApplication()).getObjectGraph().get(ContactsPresenter.class);
        mPresenter.onCreate(this);
    }

    @Override
    public Observable<Void> getWebViewFinished() {
        return WebViewObservable
            .pageLoading(mWebView, true)
            .filter(new Func1<WebViewLoadingEvent, Boolean>() {
                @Override
                public Boolean call(WebViewLoadingEvent webViewLoadingEvent) {
                    return webViewLoadingEvent.type() == WebViewLoadingEvent.Type.FINISHED;
                }
            })
            .map(new Func1<WebViewLoadingEvent, Void>() {
                @Override
                public Void call(WebViewLoadingEvent webViewLoadingEvent) {
                    return null;
                }
            });
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

    public void setActiveContact(Contact contact) {
        mWebView.loadUrl(
            "javascript: unhighlight_all(); highlight('" + contact.getName() + "', 2);");
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

            mPresenter.onContactsItemUpdate(new ContactsItemView(convertView, getItem(position), position));
            return convertView;
        }
    }
}
