package org.minezy.android.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

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
public interface TaskChain<V> extends Future<V> {

    public <P> TaskChain<P> param(P param);

    public TaskChain<V> execute();

    public TaskChain<V> main(Runnable runnable);

    public <R> TaskChain main(Callable<R> callable);

    public <R> TaskChain main(Parametrized<V, R> parametrized);

    public TaskChain<V> background(Runnable runnable);

    public <R> TaskChain background(Callable<R> callable);

    public <R> TaskChain background(Parametrized<V, R> parametrized);


}
