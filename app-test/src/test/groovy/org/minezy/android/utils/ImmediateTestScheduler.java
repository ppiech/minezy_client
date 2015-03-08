package org.minezy.android.utils;

import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.BooleanSubscription;
import rx.subscriptions.Subscriptions;

public class ImmediateTestScheduler extends Scheduler {

    public static ImmediateTestScheduler sCurrent;
    public long sDelay;
    public TimeUnit sDelayTimeUnit;

    @Override
    public Worker createWorker() {
        return new InnerImmediateScheduler();
    }

    private class InnerImmediateScheduler extends Scheduler.Worker implements Subscription {

        final BooleanSubscription innerSubscription = new BooleanSubscription();

        @Override
        public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
            sDelay = delayTime;
            sDelayTimeUnit = unit;
            try {
                return schedule(action);
            } finally {
                sDelay = -1;
                sDelayTimeUnit = TimeUnit.NANOSECONDS;
            }
        }

        @Override
        public Subscription schedule(Action0 action) {
            ImmediateTestScheduler previous = sCurrent;
            sCurrent = ImmediateTestScheduler.this;
            try {
                action.call();
                return Subscriptions.unsubscribed();
            } finally {
                sCurrent = previous;
            }
        }

        @Override
        public void unsubscribe() {
            innerSubscription.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return innerSubscription.isUnsubscribed();
        }

    }

}