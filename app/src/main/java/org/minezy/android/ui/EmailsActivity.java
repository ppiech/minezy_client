package org.minezy.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.minezy.android.MinezyApplication;
import org.minezy.android.R;
import org.minezy.android.model.Email;

import java.util.List;

public class EmailsActivity extends ActionBarActivity implements EmailsActivityController {

    public static final String ARG_CONTACT = "contact";

    private EmailsActivityPresenter mPresenter;
    private EmailAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emails_activity);
        mAdapter = new EmailAdapter(this);

        ((ListView) findViewById(R.id.emailsList)).setAdapter(mAdapter);

        mPresenter = ((MinezyApplication) getApplication()).getObjectGraph().get(EmailsActivityPresenter.class);
        //mPresenter = new EmailsActivityPresenter(this, getIntent());
        mPresenter.onCreate(this, getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
        mPresenter = null;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void setEmails(List<Email> emails) {
        mAdapter.clear();
        mAdapter.addAll(emails);
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
            mPresenter.onEmailsItemUpdate(new EmailsItemController(convertView, getItem(position), position));
            return convertView;
        }
    }
}
