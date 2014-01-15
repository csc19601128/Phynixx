package org.csc.phynixx.test_connection;

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


import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logrecord.IDataRecord;
import org.csc.phynixx.loggersystem.logrecord.IDataRecordReplay;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;

import java.util.HashMap;
import java.util.Map;


/**
 * TestConnection provides a mechanism to activate predetermined points of interruption.
 * These points leads the current work to interrupt.
 * You can simulate abnormal situation like system crashes.
 * <p/>
 *
 *
 * The points of interruption are defined by the call of {@link #interrupt(org.csc.phynixx.test_connection.TestInterruptionPoint)}.
 * You can define a gate value that define how often the interruption point is reached  till the exception is thrown.
 *
 * Feassable interruption pints are
 *
 * ACT       - after the rollback data is written and before the counter is incremented
 * COMMIT    - after the rollforward dat is written
 * ROLLBACK  -
 * PREPARE
 * CLOSE
 *
 * <p/>
 *
 *
 *
 * @author christoph
 */

public class TestConnection implements ITestConnection {

    private Map<TestInterruptionPoint, Integer> interruptionFlags = new HashMap<TestInterruptionPoint, Integer>();

    private void resetInterruptionFlags() {
        TestInterruptionPoint[] values = TestInterruptionPoint.values();
        for (int i = 0; i < values.length; i++) {
            interruptionFlags.put(values[i], 0);
        }
    }


    private IPhynixxLogger log = PhynixxLogManager.getLogger("test");

    private Object id = null;
    private boolean closed = false;

    private int currentCounter = 0;

    private int initialValue = 0;

    private IXADataRecorder messageLogger = null;


    public IXADataRecorder getXADataRecorder() {
        return messageLogger;
    }

    public void setXADataRecorder(IXADataRecorder messageLogger) {
        this.messageLogger = messageLogger;
    }

    public TestConnection(Object id) {
        resetInterruptionFlags();
        this.id = id;
        TestConnectionStatusManager.addStatusStack(this.getId());
    }

    /**
     * sets the counter to the initial value
     * this value has to be restored if the connection is rollbacked
     */
    public void setInitialCounter(int value) {
        this.currentCounter = this.currentCounter + value;
        this.getXADataRecorder().writeRollbackData(Integer.toString(-1 * this.currentCounter).getBytes());
        this.getXADataRecorder().writeRollbackData(Integer.toString(value).getBytes());
        this.initialValue = value;
    }


    public boolean isInterruptFlag(TestInterruptionPoint interruptionPoint) {
        return this.interruptionFlags.get(interruptionPoint) <= 0;
    }

    public void setInterruptFlag(TestInterruptionPoint interruptionPoint, int gate) {
        interruptionFlags.put(interruptionPoint, gate);
    }

    public void setInterruptFlag(TestInterruptionPoint interruptionPoint) {
        this.setInterruptFlag(interruptionPoint, 1);
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
        interrupt(TestInterruptionPoint.ACT);
        this.currentCounter = this.currentCounter + inc;
        log.info("TestConnection " + id + " counter incremented to " + inc + " counter=" + this.getCurrentCounter());

        TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.ACT);
    }

    public boolean isClosed() {
        return closed;
    }


    @Override
    public void open() {
        resetInterruptionFlags();
        currentCounter = 0;
    }

    public void close() {
        TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.CLOSED);
        this.interrupt(TestInterruptionPoint.CLOSE);
        log.info("TestConnection " + id + " closed");
        this.closed = true;
    }

    public void commit() {
        this.getXADataRecorder().commitRollforwardData(Integer.toString(this.currentCounter + RF_INCREMENT).getBytes());
        interrupt(TestInterruptionPoint.COMMIT);
        this.currentCounter = this.currentCounter + RF_INCREMENT;
        TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.COMMITTED);
        log.info("TestConnection " + id + " is committed");

    }

    public void prepare() {
        interrupt(TestInterruptionPoint.PREPARE);
        TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.PREPARED);
        log.info("TestConnection " + id + " is prepared");
    }

    public void rollback() {
        interrupt(TestInterruptionPoint.ROLLBACK);
        this.currentCounter = this.initialValue;
        TestConnectionStatusManager.getStatusStack(this.getId()).addStatus(TestConnectionStatus.ROLLBACKED);
        log.info("TestConnection " + id + " rollbacked");
    }

    public String toString() {
        return "TestConnection " + id;
    }


    private void interrupt(TestInterruptionPoint interruptionPoint) {
        Integer gate = this.interruptionFlags.get(interruptionPoint) - 1;
        if (gate == 0) {
            throw new ActionInterruptedException();
        }
        // refresh gate
        this.interruptionFlags.put(interruptionPoint, gate);
    }

    public void recover() {
        this.getXADataRecorder().replayRecords(new MessageReplay(this));
    }

    @Override
    public IDataRecordReplay recoverReplayListener() {
        return new MessageReplay(this);
    }

    /**
     * for debugging purpose
     */
    static class MessageReplay implements IDataRecordReplay {

        private TestConnection con;

        MessageReplay(TestConnection con) {
            this.con = con;
        }

        public void replayRollback(IDataRecord message) {
            int inc = Integer.parseInt(new String(message.getData()[0]));
            this.con.currentCounter = this.con.currentCounter + inc;
        }

        public void replayRollforward(IDataRecord message) {
            int inc = Integer.parseInt(new String(message.getData()[0]));
            this.con.currentCounter = this.con.currentCounter + inc;
        }

    }
}
