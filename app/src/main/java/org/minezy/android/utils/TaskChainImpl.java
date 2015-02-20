package org.minezy.android.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


class TaskChainImpl<V> extends FutureTask<V> implements TaskChain<V> {

    private final Executor mMain;
    private final Executor mBackground;

    private volatile Executor mNextExecutor;
    private volatile TaskChainImpl<V> mNext;
    private volatile V mParam;
    private volatile Throwable mError;

    static final Object STUB_RETURN_VALUE = new Object();

    static final Callable<Object> CALLABLE_NO_OP = new Callable<Object>() {
        public Object call() {
            return null;
        }
    };

    TaskChainImpl(Executor main, Executor background) {
        super((Callable<V>) CALLABLE_NO_OP);
        mMain = main;
        mBackground = background;
    }

    private TaskChainImpl(Runnable runnable) {
        super(runnable, (V) STUB_RETURN_VALUE);
        mMain = null;
        mBackground = null;
    }

    private TaskChainImpl(Callable<V> callable) {
        super(callable);
        mMain = null;
        mBackground = null;
    }

    public TaskChainImpl<V> execute() {
        // This method can be called only on the first executor in the chain.
        if (mNext != null) {
            mNextExecutor.execute(mNext);
        } else {
            throw new IllegalStateException("There are no tasks to execute in the chain.");
        }
        return this;
    }

    public TaskChain<V> main(Runnable runnable) {
        return chain(mMain, runnable);
    }

    public <R> TaskChain<R> main(Callable<R> callable) {
        return chain(mMain, callable);
    }

    public <R> TaskChain<R> main(Parametrized<V, R> parametrized) {
        return chain(mMain, parametrized);
    }

    public TaskChain<V> background(Runnable runnable) {
        return chain(mBackground, runnable);
    }

    public <R> TaskChain<R> background(Callable<R> callable) {
        return chain(mBackground, callable);
    }

    public <R> TaskChain<R> background(Parametrized<V, R> parametrized) {
        return chain(mBackground, parametrized);
    }

    TaskChain<V> chain(Executor executor, Runnable runnable) {
        if (mNext != null) {
            mNext.chain(executor, runnable);
        } else {
            mNextExecutor = executor;
            mNext = new TaskChainImpl<>(runnable);
        }
        return this;
    }

    <R> TaskChain<R> chain(Executor executor, Callable<R> callable) {
        if (mNext != null) {
            mNext.chain(executor, callable);
        } else {
            mNextExecutor = executor;
            mNext = (TaskChainImpl<V>) new TaskChainImpl<>(callable);
        }
        return (TaskChain<R>) this;
    }

    private class ParametrizedWrapper<R> implements Callable<R> {
        private final Parametrized<V, R> mParametrized;

        ParametrizedWrapper(Parametrized<V, R> parametrized) {
            mParametrized = parametrized;
        }

        @Override
        public R call() throws Exception {
            V param;
            if (mParam != null) {
                param = mParam;
            } else {
                param = TaskChainImpl.super.get();
            }

            return mParametrized.perform(param);
        }
    }

    public <R> TaskChainImpl chain(Executor executor, Parametrized<V, R> parametrized) {
        if (mNext != null) {
            mNext.chain(executor, parametrized);
        } else {
            mNextExecutor = executor;
            mNext = (TaskChainImpl<V>) new TaskChainImpl<R>(new ParametrizedWrapper<R>(parametrized));
        }
        return this;
    }

    public <P> TaskChainImpl<P> param(P param) {
        TaskChainImpl<P> retVal = (TaskChainImpl<P>) this;
        retVal.mParam = param;
        return retVal;
    }

    @Override
    protected void done() {
        if (!isCancelled() && mError == null && mNext != null) {
            mNextExecutor.execute(mNext);
        }

        super.done();
    }

    @Override
    public boolean isDone() {
        return super.isDone() && (mNext == null || mNext.isDone());
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean retVal = super.cancel(mayInterruptIfRunning);
        if (mNext != null) {
            retVal = mNext.cancel(mayInterruptIfRunning);
        }
        return retVal;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        // Check for errors and for first get to complete.
        super.get();

        // Check if next was added to the chain during execution.  If not, return this.get().
        if (mNext != null) {
            V retVal = mNext.get();
            // Do not return value from next if it was a Runnable.
            if (retVal != STUB_RETURN_VALUE) {
                return retVal;
            }
        }

        if (mParam != null) {
            return mParam;
        }
        return super.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        super.get(timeout, unit);

        if (mNext != null) {
            // TODO: calculate remaining wait time?
            V retVal = mNext.get(timeout, unit);
            // Do not return value from next if it was a Runnable.
            if (retVal != STUB_RETURN_VALUE) {
                return retVal;
            }
        }

        if (mParam != null) {
            return mParam;
        }

        return super.get(timeout, unit);
    }

    @Override
    protected void setException(Throwable t) {
        mError = t;
        super.setException(t);
    }
}
