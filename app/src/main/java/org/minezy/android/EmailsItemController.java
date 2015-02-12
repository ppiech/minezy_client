package org.minezy.android;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class EmailsItemController {

    private final View mItemView;
    private final Email mEmail;
    private final int mPosition;

    public EmailsItemController(View itemView, Email email, int position) {
        mItemView = itemView;
        mEmail = email;
        mPosition = position;
    }

    public Email getEmail() {
        return mEmail;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setName(String name) {
        TextView textView = (TextView) mItemView.findViewById(R.id.messageTextView);
        textView.setText(name);
    }

    public void setImageDrawable(Drawable drawable) {
        ImageView imageView = (ImageView) mItemView.findViewById(R.id.imageView);
        imageView.setImageDrawable(drawable);
    }
}
