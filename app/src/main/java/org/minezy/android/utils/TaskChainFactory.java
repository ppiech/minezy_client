package org.minezy.android.utils;

import java.util.concurrent.Executor;

public class TaskChainFactory {

    private final Executor mMain;
    private final Executor mBackground;

    public TaskChainFactory() {
        this(null);
    }

    public TaskChainFactory(String name) {
        this(new MainLooperExecutor(), new ThreadPerRunExecutor(name));
    }

    public TaskChainFactory(Executor main, Executor background) {
        mMain = main;
        mBackground = background;
    }

    public TaskChain<Void> create() {
        TaskChain<Void> chain = new TaskChain<Void>(mMain, mBackground);
        ImmediateExecutor.INSTANCE.execute(chain);
        return chain;
    }

}
