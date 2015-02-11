package org.minezy.android;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsItemController {

    private final View mItemView;

    public ContactsItemController(View itemView) {
        mItemView = itemView;
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
