package org.minezy.android.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

public class MainLooperExecutor implements Executor {
    private final Handler mHandler;

    public MainLooperExecutor() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void execute(Runnable runnable) {
        mHandler.post(runnable);
    }
}
