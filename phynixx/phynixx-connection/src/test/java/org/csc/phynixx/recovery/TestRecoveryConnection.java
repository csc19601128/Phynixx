package org.csc.phynixx.recovery;

/*
 * #%L
 * phynixx-connection
 * %%
 * Copyright (C) 2014 csc
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


import org.csc.phynixx.connection.loggersystem.Dev0Strategy;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logrecord.IDataRecord;
import org.csc.phynixx.loggersystem.logrecord.IDataRecordReplay;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;
import org.csc.phynixx.test_connection.ActionInterruptedException;
import org.csc.phynixx.test_connection.ITestConnection;
import org.csc.phynixx.test_connection.TestConnectionStatus;
import org.csc.phynixx.test_connection.TestConnectionStatusManager;


public class TestRecoveryConnection implements ITestConnection {

    public static final int RF_INCREMENT = 17;

    private IPhynixxLogger log = PhynixxLogManager.getLogger("test");

    private Object id = null;
    private boolean closed = false;

    private int currentCounter = 0;

    private int interruptCounter = -1;

    private IXADataRecorder messageLogger = Dev0Strategy.THE_DEV0_LOGGER;


    public IXADataRecorder getXADataRecorder() {
        return messageLogger;
    }

    public void setXADataRecorder(IXADataRecorder messageLogger) {
        this.messageLogger = messageLogger;
    }

    public TestRecoveryConnection(Object id) {
        super();
        this.id = id;
        TestConnectionStatusManager.addStatusStack(this.getId());
    }


    public boolean isInterruptFlag() {
        return interruptCounter > 0;
    }

    public void setInterruptFlag(boolean interruptFlag) {
        this.interruptCounter = 1;
    }

    public void setInterruptFlag(int interruptOffset) {
        this.interruptCounter = interruptOffset;
    }

    public int getCurrentCounter() {
        return currentCounter;
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ITestConnection#getId()
     */
    public Object getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ITestConnection#act()
     */
    public void act(int inc) {
        this.getXADataRecorder().writeRollbackData(Integer.toString(inc).getBytes());
        interrupt();
        this.currentCounter = this.currentCounter + inc;
        log.info("TestConnection " + id + " counter incremented to " + inc + " counter=" + this.getCurrentCounter());

        TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.ACT);
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.CLOSED);
        log.info("TestConnection " + id + " closed");
        this.closed = true;
    }

    public void commit() {
        this.getXADataRecorder().commitRollforwardData(Integer.toString(RF_INCREMENT).getBytes());
        interrupt();
        this.currentCounter = this.currentCounter + RF_INCREMENT;
        TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.COMMITTED);
        log.info("TestConnection " + id + " is committed");

    }

    public void prepare() {
        interrupt();
        TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.PREPARED);
        log.info("TestConnection " + id + " is prepared");
    }

    public void rollback() {
        interrupt();
        this.currentCounter = 0;
        TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.ROLLBACKED);
        log.info("TestConnection " + id + " rollbacked");
    }

    public String toString() {
        return "TestConnection " + id;
    }


    private void interrupt() {
        this.interruptCounter--;

        if (isInterruptFlag()) {
            throw new ActionInterruptedException();
        }
    }

    public void recover() {
        this.getXADataRecorder().replayRecords(new MessageReplay());
    }


    private class MessageReplay implements IDataRecordReplay {

        public void replayRollback(IDataRecord message) {
            int inc = Integer.parseInt(new String(message.getData()[0]));
            TestRecoveryConnection.this.currentCounter =
                    TestRecoveryConnection.this.currentCounter + inc;
        }

        public void replayRollforward(IDataRecord message) {
            int inc = Integer.parseInt(new String(message.getData()[0]));
            TestRecoveryConnection.this.currentCounter =
                    TestRecoveryConnection.this.currentCounter + inc;
        }

    }

}
