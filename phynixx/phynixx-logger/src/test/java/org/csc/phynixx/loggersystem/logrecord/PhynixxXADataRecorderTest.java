package org.csc.phynixx.loggersystem.logrecord;

/*
 * #%L
 * phynixx-logger
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
import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.io.LogRecordReader;
import org.csc.phynixx.common.io.LogRecordWriter;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;
import org.csc.phynixx.loggersystem.logger.channellogger.FileChannelDataLoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by christoph on 10.01.14.
 */
public class PhynixxXADataRecorderTest {

    private final String MESSAGE1 = "1234567qwertüäöß";
    private final String MESSAGE2 = "QWERT";


    private TmpDirectory tmpDir;

    private IDataLoggerFactory loggerFactory;

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("howllogger");
        this.tmpDir.clear();

        this.tmpDir = new TmpDirectory("howllogger");
        System.getProperties().setProperty("howl.log.logFileDir", this.tmpDir.getDirectory().getCanonicalPath());

        this.loggerFactory = new FileChannelDataLoggerFactory("test", tmpDir.getDirectory());

    }

    @After
    public void tearDown() throws Exception {
        // delete all tmp files ...
        this.tmpDir.clear();
    }

    @Test
    public void testWriteData() throws Exception {

        IDataLogger dataLogger1 = this.loggerFactory.instanciateLogger("log");
        PhynixxXADataRecorder dataRecorder = PhynixxXADataRecorder.openRecorderForWrite(1, new XADataLogger(dataLogger1), null);

        LogRecordWriter logRecordWriter2 = new LogRecordWriter();
        logRecordWriter2.writeUTF(MESSAGE2);
        byte[][] content2 = new byte[1][];
        content2[0] = logRecordWriter2.toByteArray();
        dataRecorder.writeRollbackData(content2);

        LogRecordWriter logRecordWriter1 = new LogRecordWriter();
        logRecordWriter1.writeUTF(MESSAGE1);
        byte[][] content1 = new byte[1][];
        content1[0] = logRecordWriter1.toByteArray();
        dataRecorder.writeRollforwardData(content1);

        dataRecorder.disqualify();

        IDataLogger dataLogger2 = this.loggerFactory.instanciateLogger("log");
        dataRecorder = PhynixxXADataRecorder.recoverDataRecorder(new XADataLogger(dataLogger2), null);
        dataRecorder.replayRecords(new IDataRecordReplay() {

            @Override
            public void notifyNoMoreData() {

            }

            @Override
            public void replayRollback(IDataRecord record) {
                LogRecordReader logReader = new LogRecordReader(record.getData()[0]);
                String data = null;
                try {
                    data = logReader.readUTF();
                } catch (IOException e) {
                    throw new DelegatedRuntimeException(e);
                }
                Assert.assertEquals(MESSAGE2, data);
                System.out.println(data);
            }

            @Override
            public void replayRollforward(IDataRecord record) {
                LogRecordReader logReader = new LogRecordReader(record.getData()[0]);
                String data = null;
                try {
                    data = logReader.readUTF();
                } catch (IOException e) {
                    throw new DelegatedRuntimeException(e);
                }
                Assert.assertEquals(MESSAGE1, data);
                System.out.println(data);
            }
        });

        dataRecorder.disqualify();

    }


}



