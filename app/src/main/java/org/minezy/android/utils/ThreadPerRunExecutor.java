package org.minezy.android.utils;

import java.util.concurrent.Executor;

public class ThreadPerRunExecutor implements Executor {

    private final String mName;

    public ThreadPerRunExecutor() {
        this(null);
    }

    public ThreadPerRunExecutor(String name) {
        mName = name;
    }

    @Override
    public void execute(Runnable runnable) {
        Thread t = mName != null ? new Thread(runnable, mName) : new Thread(runnable);
        t.start();
    }


}
