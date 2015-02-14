package org.minezy.android.ui;

import android.content.Context;
import android.content.Intent;

public interface ActivityController {

    public Context getContext();

    public void finish();

    public void startActivity(Intent intent);
}
