package org.csc.phynixx.tutorial;

/*
 * #%L
 * Tutorial (how to use Phynixx)
 * %%
 * Copyright (C) 2014 - 2015 Christoph Schmidt-Casdorff
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.tutorial.UTFWriter;
import org.csc.phynixx.tutorial.UTFWriterImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Collections;
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

    static DecimalFormat format = new DecimalFormat();

    static {
        format.applyPattern("000");
    }

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

    /**
     * messages ' My message {0}' are written concurrently. To check, that the message are written once and haven't been disrupted , read all messages,
     * order them and check, that each mwssage apperras once.
     *
     * @throws Exception
     */
    @Test
    public void testLocking() throws Exception {

        final File file = this.tmpDir.assertExitsFile("testOut.txt");
        UTFWriter writer = new UTFWriterImpl(file);
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        Set<TestCallable> callables = new HashSet<TestCallable>();
        for (int i = 0; i < 30; i++) {
            callables.add(new TestCallable(i, writer));
        }
        final List<Future<String>> futures = executorService.invokeAll(callables);

        final List<String> content = writer.readContent();
        Collections.sort(content);
        for (int i = 0; i < content.size(); i++) {
            String line = content.get(i);
            Assert.assertEquals("My message " + format.format(i), line);
        }
    }

    private static class TestCallable implements Callable<String> {

        private final UTFWriter writer;

        private final int id;

        private TestCallable(int id, UTFWriter writer) {
            this.writer = writer;
            this.id = id;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public String call() throws Exception {
            String lockToken = writer.lock();
            try {
                writer.write("My message " + format.format(id));
                System.out.println("My message " + format.format(id));
                //Thread.sleep(1000);
                return lockToken;
            } finally {
                writer.unlock(lockToken);
            }
        }
    }

}
