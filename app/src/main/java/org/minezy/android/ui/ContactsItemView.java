package org.minezy.android.ui;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.minezy.android.R;
import org.minezy.android.model.Contact;

public class ContactsItemView {

    private final View mItemView;
    private final Contact mContact;
    private final int mPosition;

    public ContactsItemView(View itemView, Contact contact, int position) {

        mItemView = itemView;
        mContact = contact;
        mPosition = position;
    }

    public Contact getContact() {
        return mContact;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setName(String name) {
        TextView textView = (TextView) mItemView.findViewById(R.id.nameTextView);
        textView.setText(name);
    }

    public void setImageDrawable(Drawable drawable) {
        ImageView imageView = (ImageView) mItemView.findViewById(R.id.imageView);
        imageView.setImageDrawable(drawable);
    }
}
