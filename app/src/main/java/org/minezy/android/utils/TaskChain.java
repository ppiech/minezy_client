package org.minezy.android.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Utility for executing multiple tasks one after another in different threads.
 * <p/>
 * Example:
 * <pre>
 *     TaskChainFactory factory = new TaskChainFactory();
 *     factory.create().main(new Runnable() {
 *         public void run() {
 *             // Prepare for background work
 *             ...
 *         }
 *     }).background(new Callable<Stuff>() {
 *         public Stuff call() {
 *             // Perform work in background thread
 *             ...
 *         }
 *     }).main(new Runnable() {
 *         public void run() {
 *             // Process results in main thread.
 *             ...
 *         }
 *     }).execute();
 * </pre>
 *
 * @param <V>
 *     Return type for the chain.
 */
public class TaskChain<V> extends FutureTask<V> {

    private final Executor mMain;
    private final Executor mBackground;
    private final TaskChain<?> mPrevious;

    private volatile Executor mNextExecutor;
    private volatile TaskChain<V> mNext;
    private volatile Throwable mError;

    static final Object STUB_RETURN_VALUE = new Object();

    static final Callable<Object> CALLABLE_NO_OP = new Callable<Object>() {
        public Object call() {
            return null;
        }
    };

    TaskChain(Executor main, Executor background) {
        super((Callable<V>) CALLABLE_NO_OP);
        mMain = main;
        mBackground = background;
        mPrevious = null;
    }

    private TaskChain(TaskChain<?> previous, Runnable runnable) {
        super(runnable, (V) STUB_RETURN_VALUE);
        mMain = null;
        mBackground = null;
        mPrevious = previous;
    }

    private TaskChain(TaskChain<?> previous, Callable<V> callable) {
        super(callable);
        mMain = null;
        mBackground = null;
        mPrevious = previous;
    }

    public void execute() {
        // This method can be called only on the first executor in the chain.
        if (mNext != null) {
            mNextExecutor.execute(mNext);
        } else {
            throw new IllegalStateException("There are no tasks to execute in the chain.");
        }
    }

    public TaskChain<V> main(Runnable runnable) {
        return chain(mMain, runnable);
    }

    public <R> TaskChain main(Callable<R> callable) {
        return chain(mMain, callable);
    }

    public TaskChain<V> background(Runnable runnable) {
        return chain(mBackground, runnable);
    }

    public <R> TaskChain background(Callable<R> callable) {
        return chain(mBackground, callable);
    }

    public TaskChain<V> chain(Executor executor, Runnable runnable) {
        if (mNext != null) {
            mNext.chain(executor, runnable);
        } else {
            mNextExecutor = executor;
            mNext = new TaskChain<>(this, runnable);
        }
        return this;
    }

    public <R> TaskChain chain(Executor executor, Callable<R> callable) {
        if (mNext != null) {
            mNext.chain(executor, callable);
        } else {
            mNextExecutor = executor;
            mNext = (TaskChain<V>) new TaskChain<>(this, callable);
        }
        return this;
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
        return super.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        super.get(timeout, unit);

        if (mNext != null) {
            // TODO: calculate remaining wait time?
            return mNext.get(timeout, unit);
        }

        return super.get(timeout, unit);
    }

    @Override
    protected void setException(Throwable t) {
        mError = t;
        super.setException(t);
    }

    //    @Override
//    protected void set(V v) {
//        if (mPrevious != null) {
//            ((TaskChain<V>) mPrevious).set(v);
//        }
//        super.set(v);
//    }
//
//    @Override
//    protected void setException(Throwable t) {
//        if (mPrevious != null) {
//            ((TaskChain<V>) mPrevious).setException(t);
//        }
//        super.setException(t);
//    }

}
