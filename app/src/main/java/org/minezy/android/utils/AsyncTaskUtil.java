package org.minezy.android.utils;

import android.os.AsyncTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("unchecked")
public class AsyncTaskUtil {

    public interface ProgressTracker<Progress> {
        public void publishProgress(Progress... progress);
    }

    public static abstract class Executable<Params, Progress, Result> {

        abstract public Result doInBackground(Params... params);

        public Result doInBackground(ProgressTracker<Progress> progressTracker, Params... params) {
            return doInBackground(params);
        }

        public void onPreExecute() {
            // Do nothing by default
        }

        public void onPostExecute(Result result) {
            // Do nothing by default
        }

        public void onCancelled() {
            // Do nothing by default
        }

        protected void onProgressUpdate(Progress... values) {
            // Do nothing by default
        }

    }

    public <Params, Progress, Result> Future<Result> execute(final Executable<Params, Progress, Result> executable,
                                                             Params... params) {
        final AsyncTask<Params, Progress, Result> task = new AsyncTask<Params, Progress, Result>() {
            @Override
            protected void onPreExecute() {
                executable.onPreExecute();
            }

            @Override
            protected Result doInBackground(Params... params) {
                return executable.doInBackground(
                    new ProgressTracker<Progress>() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public void publishProgress(Progress... progress) {
                            publishProgressToTask(progress);
                        }
                    },
                    params);
            }

            private void publishProgressToTask(Progress... progress) {
                publishProgress(progress);
            }


            @Override
            protected void onPostExecute(Result v) {
                executable.onPostExecute(v);
            }

            @Override
            protected void onCancelled() {
                executable.onCancelled();
            }

            @Override
            protected void onProgressUpdate(Progress... values) {
                executable.onProgressUpdate(values);
            }
        };

        task.execute(params);

        return new Future<Result>() {
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
            public Result get() throws InterruptedException, ExecutionException {
                return task.get();
            }

            @Override
            public Result get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
                return task.get(timeout, unit);
            }
        };
    }
}
