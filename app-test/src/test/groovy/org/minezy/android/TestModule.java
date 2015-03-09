package org.minezy.android;

import android.content.Context;
import android.content.SharedPreferences;

import org.minezy.android.data.MinezyApiV1;
import org.minezy.android.data.MinezyConnection;
import org.minezy.android.ui.ContactsActivityPresenter;
import org.minezy.android.ui.EmailsActivityPresenter;
import org.minezy.android.utils.ImmediateTestScheduler;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module(
    injects = {ContactsActivityPresenter.class, EmailsActivityPresenter.class, MinezyApiV1.class},
    overrides = true,
    library = true)
public class TestModule {
    public Context context;
    public SharedPreferences sharedPreferences;
    public MinezyApiV1 apiV1;
    public MinezyConnection connection;
    public Scheduler mainScheduler = new ImmediateTestScheduler();
    public Scheduler ioScheduler = new ImmediateTestScheduler();

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
    @Singleton
    @Named("main")
    Scheduler provideMainScheduler() {
        return mainScheduler;
    }

    @Provides
    @Named("io")
    Scheduler provideThreadScheduler() {
        return ioScheduler;
    }

}
