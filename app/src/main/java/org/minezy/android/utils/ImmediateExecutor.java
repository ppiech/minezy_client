package org.minezy.android.utils;

import java.util.concurrent.Executor;

public class ImmediateExecutor implements Executor {

    public static final ImmediateExecutor INSTANCE = new ImmediateExecutor();

    @Override
    public void execute(Runnable runnable) {
        runnable.run();
    }
}
