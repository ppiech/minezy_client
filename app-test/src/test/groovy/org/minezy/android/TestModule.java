package org.minezy.android;

import android.content.Context;
import android.content.SharedPreferences;

import org.minezy.android.data.MinezyApiV1;
import org.minezy.android.data.MinezyConnection;
import org.minezy.android.ui.ContactsActivityPresenter;
import org.minezy.android.ui.EmailsActivityPresenter;
import org.minezy.android.utils.TaskChainFactory;
import org.minezy.android.utils.TestExecutor;

import java.util.concurrent.Executor;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
    injects = {ContactsActivityPresenter.class, EmailsActivityPresenter.class, MinezyApiV1.class},
    overrides = true,
    library = true)
public class TestModule {
    public Context context;
    public SharedPreferences sharedPreferences;
    public MinezyApiV1 apiV1;
    public MinezyConnection connection;
    public Executor mainExecutor = new TestExecutor();
    public Executor backgroundExecutor = new TestExecutor();
    public TaskChainFactory taskChainFactory = new TaskChainFactory(mainExecutor, backgroundExecutor);

    @Provides
    @javax.inject.Singleton
    @ForApplication
    Context provideApplicationContext() {
        return context;
    }

    @Provides
    @Named("default")
    SharedPreferences provideSharedPreferences() {
        return sharedPreferences;
    }

    @Provides
    MinezyApiV1 provideMinezyApiV1() {
        return apiV1;
    }

    @Provides
    MinezyConnection provideMinezyConnection() {
        return connection;
    }

    @Provides
    @Named("thread per run")
    TaskChainFactory provideTaskChainFactory(@Named("main") Executor main,
                                             @Named("thread per run") Executor background) {
        return taskChainFactory;
    }

    @Provides
    @Singleton
    @Named("main")
    Executor provideMainExecutor() {
        return mainExecutor;
    }

    @Provides
    @Named("thread per run")
    Executor provideThreadPerRunExecutor() {
        return backgroundExecutor;
    }

}
