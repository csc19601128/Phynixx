package org.csc.phynixx.tutorial;

import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Created by zf4iks2 on 03.02.14.
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

        TAEnabledUTFWriterImpl writer = new TAEnabledUTFWriterImpl();
        writer.open(file);
        writer.resetContent();

        try {
            writer.write("AA").write("BB");
        } finally {
            writer.close();
        }

        TAEnabledUTFWriterImpl recoverWriter = new TAEnabledUTFWriterImpl();
        recoverWriter.open(file);
        try {
            List<String> content = writer.getContent();
            Assert.assertEquals(2, content.size());
            Assert.assertEquals("AA", content.get(0));
            Assert.assertEquals("BB", content.get(1));

        } finally {
            writer.close();
        }


    }

}
