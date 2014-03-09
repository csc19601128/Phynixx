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
import org.csc.phynixx.connection.IPhynixxManagedConnectionFactory;
import org.csc.phynixx.connection.IPhynixxRecovery;
import org.csc.phynixx.connection.PhynixxManagedConnectionFactory;
import org.csc.phynixx.connection.PhynixxRecovery;
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

    private PhynixxManagedConnectionFactory<TAEnabledUTFWriter> connectionFactory = null;

    private IPhynixxLoggerSystemStrategy strategy = null;

    @Before
    public void setUp() throws Exception {


        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging("INFO");

        this.tmpDir = new TmpDirectory("test");

        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory("ta_enabled", this.tmpDir.getDirectory());
        IPhynixxLoggerSystemStrategy<TAEnabledUTFWriter> strategy = new LoggerPerTransactionStrategy<TAEnabledUTFWriter>(loggerFactory);

        this.connectionFactory =
                new PhynixxManagedConnectionFactory<TAEnabledUTFWriter>(new TAEnabledUTFWriterFactoryImpl());
        connectionFactory.setLoggerSystemStrategy(strategy);
        connectionFactory.addConnectionProxyDecorator(new DumpManagedConnectionListener<TAEnabledUTFWriter>());

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
        try {
            connection.open(file);
            connection.resetContent();
            connection.write("AA").write("BB");
            connection.commit();
        } finally {
            connection.close();
        }

        TAEnabledUTFWriterImpl recoverWriter = new TAEnabledUTFWriterImpl("recover");
        try {
            recoverWriter.open(file);
            List<String> content = recoverWriter.readContent();
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

        TAEnabledUTFWriter connection = this.connectionFactory.getConnection();
        try {
            connection.open(file);
            connection.resetContent();
            connection.write("AA").write("BB");
            connection.commit();
        } finally {
            connection.close();
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
            List<String> content = recoverWriter.readContent();
            Assert.assertEquals(2, content.size());
            Assert.assertEquals("AA", content.get(0));
            Assert.assertEquals("BB", content.get(1));

        } finally {
            recoverWriter.close();
        }


    }



    @Test
    public void testRecovery() throws Exception {

        File file = this.tmpDir.assertExitsFile("my_test.tmp1");

        TAEnabledUTFWriter connection1 = this.connectionFactory.getConnection();
        connection1.open(file);
        connection1.write("AA").write("BB");
        connection1.commit();
        connection1.close();


        TAEnabledUTFWriter connection2 = this.connectionFactory.getConnection();
        connection2.open(file);
        connection2.write("XX").write("YY");


        // connection1 keeps uncommitted
        this.connectionFactory.close();

        IPhynixxLoggerSystemStrategy<TAEnabledUTFWriter> recoveredRecorderSystem=
                this.connectionFactory.getLoggerSystemStrategy();

        // close the LoggerStrategy
        PhynixxRecovery<TAEnabledUTFWriter> recovery= new PhynixxRecovery<TAEnabledUTFWriter>(new TAEnabledUTFWriterFactoryImpl());
         recovery.setLoggerSystemStrategy(recoveredRecorderSystem);

        recovery.recover(new IPhynixxRecovery.IRecoveredManagedConnection<TAEnabledUTFWriter>() {

            @Override
            public void managedConnectionRecovered(TAEnabledUTFWriter con) {

                try {
                    System.out.println(con.readContent());
                } catch (Exception e) {}
            }
        });

    }

}
