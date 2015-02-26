package org.minezy.android;

import android.content.Context;
import android.content.SharedPreferences;

import org.minezy.android.data.MinezyApiV1;
import org.minezy.android.data.MinezyConnection;
import org.minezy.android.ui.ContactsActivityPresenter;
import org.minezy.android.ui.EmailsActivityPresenter;
import org.minezy.android.utils.TaskChainFactory;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module(
    injects = {ContactsActivityPresenter.class, EmailsActivityPresenter.class, MinezyApiV1.class},
    overrides = true,
    library = true)
public class TestModule {
    public Context context;
    public SharedPreferences sharedPreferences;
    public TaskChainFactory taskChainFactory;
    public MinezyApiV1 apiV1;
    public MinezyConnection connection;

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
    @Named("raw thread")
    TaskChainFactory provideTaskChainFactory() {
        return taskChainFactory;
    }

    @Provides
    MinezyApiV1 provideMinezyApiV1() {
        return apiV1;
    }

    @Provides
    MinezyConnection provideMinezyConnection() {
        return connection;
    }
}
