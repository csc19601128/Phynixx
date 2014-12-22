package org.csc.phynixx.tutorial;

/*
 * #%L
 * phynixx-tutorial
 * %%
 * Copyright (C) 2014 Christoph Schmidt-Casdorff
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Created by Christoph Schmidt-Casdorff on 03.02.14.
 */
public class TAEnabledUTFWriterTest {

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

    @Test
    public void testTAEnabledUTFWriter() throws Exception {

        File file = this.tmpDir.assertExitsFile("my_test.tmp");
        UTFWriter utfWriter= new UTFWriterImpl(file);

        TAEnabledUTFWriterImpl writer = new TAEnabledUTFWriterImpl("recover", utfWriter);
        writer.resetContent();

        try {
            writer.write("AA").write("BB");
        } finally {
            writer.close();
        }

        TAEnabledUTFWriterImpl recoverWriter = new TAEnabledUTFWriterImpl("recover", utfWriter);
        try {
            List<String> content = recoverWriter.readContent();
            Assert.assertEquals(2, content.size());
            Assert.assertEquals("AA", content.get(0));
            Assert.assertEquals("BB", content.get(1));

        } finally {
            writer.close();
        }


    }

}
