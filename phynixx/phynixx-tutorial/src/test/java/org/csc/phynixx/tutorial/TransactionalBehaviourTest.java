package org.csc.phynixx.tutorial;

import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.connection.PhynixxPhynixxManagedConnectionFactory;
import org.csc.phynixx.connection.loggersystem.IPhynixxLoggerSystemStrategy;
import org.csc.phynixx.connection.loggersystem.LoggerPerTransactionStrategy;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Created by zf4iks2 on 03.02.14.
 */
public class TransactionalBehaviourTest {

    private TmpDirectory tmpDir = null;

    private PhynixxPhynixxManagedConnectionFactory<TAEnabledUTFWriter> connectionFactory = null;

    private IPhynixxLoggerSystemStrategy strategy = null;

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        this.tmpDir = new TmpDirectory("test");

        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("ta_enabled", this.tmpDir.getDirectory());
        IPhynixxLoggerSystemStrategy strategy = new LoggerPerTransactionStrategy(loggerFactory);

        this.connectionFactory =
                new PhynixxPhynixxManagedConnectionFactory<TAEnabledUTFWriter>(new TAEnabledUTFWriterFactoryImpl());
        connectionFactory.setLoggerSystemStrategy(strategy);

    }

    @After
    public void tearDown() throws Exception {

        this.connectionFactory.close();

        // delete all tmp files ...
        this.tmpDir.clear();

    }

    @Test
    public void testCommit() throws Exception {

        File file = this.tmpDir.assertExitsFile("my_test.tmp");

        TAEnabledUTFWriter connection = this.connectionFactory.getConnection();
        connection.open(file);
        try {
            connection.write("AA").write("BB");
        } finally {
            connection.close();
        }

        TAEnabledUTFWriterImpl recoverWriter = new TAEnabledUTFWriterImpl();
        try {
            recoverWriter.open(file);
            List<String> content = recoverWriter.getContent();
            Assert.assertEquals(2, content.size());
            Assert.assertEquals("AA", content.get(0));
            Assert.assertEquals("BB", content.get(1));

        } finally {
            recoverWriter.close();
        }


    }


    @Test
    public void testRollback() throws Exception {

        File file = this.tmpDir.assertExitsFile("my_test.tmp");

        TAEnabledUTFWriter connection1 = this.connectionFactory.getConnection();
        try {
            connection1.open(file);
            connection1.write("AA").write("BB");
        } finally {
            connection1.close();
        }

        TAEnabledUTFWriter connection2 = this.connectionFactory.getConnection();
        try {
            connection2.open(file);
            connection2.write("CC").write("DD");
            connection2.rollback();
        } finally {
            connection2.close();
        }

        TAEnabledUTFWriter recoverWriter = this.connectionFactory.getConnection();
        recoverWriter.open(file);
        try {
            List<String> content = recoverWriter.getContent();
            Assert.assertEquals(2, content.size());
            Assert.assertEquals("AA", content.get(0));
            Assert.assertEquals("BB", content.get(1));

        } finally {
            recoverWriter.close();
        }


    }

}
