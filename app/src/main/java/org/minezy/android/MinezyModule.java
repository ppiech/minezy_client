package org.minezy.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.minezy.android.ui.ContactsActivityPresenter;
import org.minezy.android.ui.EmailsActivityPresenter;
import org.minezy.android.utils.MainLooperExecutor;
import org.minezy.android.utils.TaskChainFactory;
import org.minezy.android.utils.ThreadPerRunExecutor;

import java.util.concurrent.Executor;

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
    @Named("thread per run")
    TaskChainFactory provideTaskChainFactory(@Named("main") Executor main,
                                             @Named("thread per run") Executor background) {
        return new TaskChainFactory();
    }

    @Provides
    @Singleton
    @Named("main")
    Executor provideMainExecutor() {
        return new MainLooperExecutor();
    }

    @Provides
    @Named("thread per run")
    Executor provideThreadPerRunExecutor() {
        return new ThreadPerRunExecutor();
    }

    @Provides
    @Singleton
    @Named("main")
    Scheduler provideMainScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Provides
    @Named("io")
    Scheduler provideThreadScheduler() {
        return Schedulers.io();
    }

}
