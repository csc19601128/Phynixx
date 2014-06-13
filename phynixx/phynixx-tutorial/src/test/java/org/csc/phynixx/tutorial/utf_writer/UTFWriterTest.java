package org.csc.phynixx.tutorial.utf_writer;

import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.tutorial.UTFWriter;
import org.csc.phynixx.tutorial.UTFWriterImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by christoph on 09.06.2014.
 */
public class UTFWriterTest {

    private TmpDirectory tmpDir = null;

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        this.tmpDir = new TmpDirectory("test");
    }

    @After
    public void tearDown() throws Exception {

        // delete all tmp files ...
        this.tmpDir.clear();

    }

    private static class TestCallable implements Callable<String> {

        private final UTFWriter writer;

        private TestCallable(UTFWriter writer) {
            this.writer = writer;
        }

        /**

         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public String call() throws Exception {
            String lockToken=writer.lock();
            try {
                writer.write("My message");
                Thread.sleep(1000);
                return lockToken;
            }finally {
                writer.unlock(lockToken);
            }
        }
    }

    @Test
    public void testLocking() throws InterruptedException {

        UTFWriter writer= new UTFWriterImpl();
        final ExecutorService executorService = Executors.newFixedThreadPool(3);
        Set<TestCallable> callables= new HashSet<TestCallable>();
       for (int i = 0; i < 10; i++) {
            callables.add(new TestCallable(writer));
        }
        final List<Future<String>> futures = executorService.invokeAll(callables);
    }

}
