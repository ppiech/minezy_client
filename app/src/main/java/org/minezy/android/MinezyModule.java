package org.minezy.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.minezy.android.ui.ContactsActivityPresenter;
import org.minezy.android.ui.EmailsActivityPresenter;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


@Module(
    injects = {EmailsActivityPresenter.class, ContactsActivityPresenter.class}
)

public class MinezyModule {

    private final MinezyApplication mApplication;

    public MinezyModule(MinezyApplication application) {
        this.mApplication = application;
    }

    @Provides
    @Singleton
    @ForApplication
    Context provideApplicationContext() {
        return mApplication;
    }

    @Provides
    @Named("default")
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication);
    }

    @Provides
    @Singleton
    @Named("main")
    Scheduler provideMainScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Provides
    @Singleton
    @Named("io")
    Scheduler provideThreadScheduler() {
        return Schedulers.io();
    }

}
