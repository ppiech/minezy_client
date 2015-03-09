package org.minezy.android.utlis;

import android.test.AndroidTestCase;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TaskChainTests extends AndroidTestCase {

    private static Runnable EMPTY_RUNNABLE = new Runnable() {
        public void run() {
        }
    };

    private static Runnable ERROR_RUNNABLE = new Runnable() {
        public void run() {
            throw new RuntimeException();
        }
    };


    private static Object CONSTANT = new Object();

    private static Callable<Object> CONSTANT_CALLABLE = new Callable<Object>() {
        @Override
        public Object call() throws Exception {
            return CONSTANT;
        }
    };

    private static Callable<Object> ERROR_CALLABLE = new Callable<Object>() {
        @Override
        public Object call() throws Exception {
            throw new RuntimeException();
        }
    };

    private static Parametrized<String, Integer> STRING_TO_INT_PARAMETRIZED = new Parametrized<String, Integer>() {
        @Override
        public Integer perform(String param) throws Exception {
            return Integer.parseInt(param);
        }
    };


    private TaskChainFactory mFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFactory = new TaskChainFactory(ImmediateExecutor.INSTANCE, ImmediateExecutor.INSTANCE);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreate_get() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create();
        assertEquals(null, chain.get());
    }

    public void testRunnable_getBeforeExecute() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().main(EMPTY_RUNNABLE);
        try {
            chain.get(10, TimeUnit.MILLISECONDS);
            fail("Expected timeout");
        } catch (TimeoutException e) {
        }
    }

    public void testRunnable_getAfterExecute() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().background(EMPTY_RUNNABLE).execute();
        assertEquals(null, chain.get());
    }

    public void testRunnable_cancelBeforeExecute() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().main(EMPTY_RUNNABLE);
        assertTrue(chain.cancel(false));
        try {
            chain.get();
            fail("Expected cancellation");
        } catch (CancellationException e) {
        }
    }

    public void testRunnable_cancelAfterExecute() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().background(EMPTY_RUNNABLE);
        chain.execute();
        assertFalse(chain.cancel(false));
        assertEquals(null, chain.get());
    }

    public void testRunnable_error() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().main(ERROR_RUNNABLE);
        chain.execute();
        try {
            chain.get();
            fail("Expected error");
        } catch (ExecutionException e) {
        }
    }

    public void testCallable_getBeforeExecute() throws ExecutionException, InterruptedException {
        TaskChain<Object> chain = mFactory.create().background(CONSTANT_CALLABLE);
        try {
            chain.get(10, TimeUnit.MILLISECONDS);
            fail("Expected timeout");
        } catch (TimeoutException e) {
        }
    }

    public void testCallable_getAfterExecute() throws ExecutionException, InterruptedException {
        TaskChain<Object> chain = mFactory.create().main(CONSTANT_CALLABLE);
        chain.execute();
        assertEquals(CONSTANT, chain.get());
    }

    public void testCallable_cancelBeforeExecute() throws ExecutionException, InterruptedException {
        TaskChain<Object> chain = mFactory.create().background(CONSTANT_CALLABLE);
        assertTrue(chain.cancel(false));
        try {
            chain.get();
            fail("Expected cancellation");
        } catch (CancellationException e) {
        }
    }

    public void testCallable_cancelAfterExecute() throws ExecutionException, InterruptedException {
        TaskChain<Object> chain = mFactory.create().main(CONSTANT_CALLABLE);
        chain.execute();
        assertFalse(chain.cancel(false));
        assertEquals(CONSTANT, chain.get());
    }


    public void testCallable_error() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().background(ERROR_CALLABLE);
        chain.execute();
        try {
            chain.get();
            fail("Expected error");
        } catch (ExecutionException e) {
        }
    }

    public void testRunnableRunnable_getBeforeExecute() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().main(EMPTY_RUNNABLE).main(EMPTY_RUNNABLE);
        ;
        try {
            chain.get(10, TimeUnit.MILLISECONDS);
            fail("Expected timeout");
        } catch (TimeoutException e) {
        }
    }

    public void testRunnableRunnable_getAfterExecute() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().background(EMPTY_RUNNABLE).main(EMPTY_RUNNABLE);
        chain.execute();
        assertEquals(null, chain.get());
    }

    public void testRunnableRunnable_cancelBeforeExecute() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().main(EMPTY_RUNNABLE).main(EMPTY_RUNNABLE);
        assertTrue(chain.cancel(false));
        try {
            chain.get();
            fail("Expected cancellation");
        } catch (CancellationException e) {
        }
    }

    public void testRunnableRunnable_cancelAfterExecute() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().background(EMPTY_RUNNABLE).main(EMPTY_RUNNABLE);
        chain.execute();
        assertFalse(chain.cancel(false));
        assertEquals(null, chain.get());
    }

    public void testRunnableRunnable_errorInSecondRunnable() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().main(EMPTY_RUNNABLE).main(ERROR_RUNNABLE);
        chain.execute();
        try {
            chain.get();
            fail("Expected error");
        } catch (ExecutionException e) {
        }
    }

    public void testRunnableRunnable_errorInFirstRunnable() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().background(ERROR_RUNNABLE).main(EMPTY_RUNNABLE);
        chain.execute();
        try {
            chain.get();
            fail("Expected error");
        } catch (ExecutionException e) {
        }
    }

    public void testRunnableCallable_getAfterExecute() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().main(EMPTY_RUNNABLE).main(CONSTANT_CALLABLE);
        chain.execute();
        assertEquals(CONSTANT, chain.get());
    }

    public void testCallableRunnable_getAfterExecute() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create().background(CONSTANT_CALLABLE).main(EMPTY_RUNNABLE);
        chain.execute();
        assertEquals(CONSTANT, chain.get());
    }

    public void testCallableRunnable_getWithTimeoutAfterExecute()
        throws ExecutionException, InterruptedException, TimeoutException {
        TaskChain<Void> chain = mFactory.create().main(CONSTANT_CALLABLE).main(EMPTY_RUNNABLE);
        chain.execute();
        assertEquals(CONSTANT, chain.get(1, TimeUnit.MILLISECONDS));
    }

    public void testParam_getBeforeExecute() throws ExecutionException, InterruptedException {
        TaskChain<Void> chain = mFactory.create();
        chain.param("test");
        assertEquals("test", chain.get());
    }


    public void testParam_getWithTimeoutBeforeExecute()
        throws ExecutionException, InterruptedException, TimeoutException {
        TaskChain<Void> chain = mFactory.create();
        chain.param("test");
        assertEquals("test", chain.get(1, TimeUnit.MILLISECONDS));
    }

    public void testParamParametrized_getAfterExecute() throws ExecutionException, InterruptedException {
        TaskChain<String> chain = mFactory.create().param("656").main(STRING_TO_INT_PARAMETRIZED);
        chain.execute();
        assertEquals(656, chain.get());
    }

    public void testParamParametrized_getWithTimeoutAfterExecute()
        throws ExecutionException, InterruptedException, TimeoutException {
        TaskChain<String> chain = mFactory.create().param("656").main(STRING_TO_INT_PARAMETRIZED);
        chain.execute();
        assertEquals(656, chain.get(1, TimeUnit.MILLISECONDS));
    }

    public void testParamParametrized_error() throws ExecutionException, InterruptedException {
        TaskChain<String> chain = mFactory.create().param("not a number").background(STRING_TO_INT_PARAMETRIZED);
        chain.execute();
        try {
            chain.get();
            fail("Expected error");
        } catch (ExecutionException e) {
        }
    }

}