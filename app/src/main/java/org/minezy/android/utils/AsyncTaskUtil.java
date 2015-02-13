package org.minezy.android.utils;

import android.os.AsyncTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncTaskUtil {

    public static abstract class Executable<V> {
        abstract public V doInBackground();

        public void onPreExecute() {
        }

        public void onPostExecute(V result) {
        }

        public void onCancelled() {
        }
    }

    public <V> Future<V> execute(final Executable<V> executable) {
        final AsyncTask<Void, Void, V> task = new AsyncTask<Void, Void, V>() {

            @Override
            protected void onPreExecute() {
                executable.onPreExecute();
            }

            @Override
            protected V doInBackground(Void... params) {
                return executable.doInBackground();
            }

            @Override
            protected void onPostExecute(V v) {
                executable.onPostExecute(v);
            }

            @Override
            protected void onCancelled() {
                executable.onCancelled();
            }
        };

        task.execute();

        return new Future<V>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return task.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }

            @Override
            public boolean isDone() {
                return task.getStatus() == AsyncTask.Status.FINISHED;
            }

            @Override
            public V get() throws InterruptedException, ExecutionException {
                return task.get();
            }

            @Override
            public V get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
                return task.get(timeout, unit);
            }
        };
    }
}
