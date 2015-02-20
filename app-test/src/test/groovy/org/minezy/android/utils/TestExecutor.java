package org.minezy.android.utils;

import java.util.concurrent.Executor;

public class TestExecutor implements Executor {
    private static TestExecutor sExecuting = null;

    public static TestExecutor executing() {
        return sExecuting;
    }

    @Override
    public void execute(Runnable runnable) {
        TestExecutor previouslyExecuting = sExecuting;
        sExecuting = this;
        try {
            runnable.run();
        } finally {
            sExecuting = previouslyExecuting;
        }
    }

}
