package org.csc.phynixx.phynixx.testconnection;

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


import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.io.LogRecordReader;
import org.csc.phynixx.common.io.LogRecordWriter;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logrecord.IDataRecord;
import org.csc.phynixx.loggersystem.logrecord.IDataRecordReplay;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;

import java.util.HashMap;
import java.util.Map;


/**
 * the initial value of the counter is the defines the rollback state. Every increrment of the counter via {@link #act} increases the counter.
 * The counter at commit are commitRollforward data.

 * TestConnection provides a mechanism to activate predetermined points of interruption.
 * These points leads the current work to interrupt.
 * You can simulate abnormal situation like system crashes.



 * The points of interruption are defined by the call of {@link #interrupt(org.csc.phynixx.phynixx.testconnection.TestInterruptionPoint)}.
 * You can define a gate value that define how often the interruption point is reached  till the exception is thrown.

 * Feassable interruption pints are

 * REQUIRES_TRANSACTION       - after the rollback data is written and before the counter is incremented
 * COMMIT    - after the rollforward dat is written
 * ROLLBACK  -
 * PREPARE
 * CLOSE


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

    private Object connectionId = null;

    private int increment = 0;
    private int initialValue = 0;

    private IXADataRecorder messageLogger = null;

    private boolean autoCommit=false;

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public IXADataRecorder getXADataRecorder() {
        return messageLogger;
    }

    public void setXADataRecorder(IXADataRecorder messageLogger) {
        this.messageLogger = messageLogger;
    }

    public TestConnection(Object id) {
        resetInterruptionFlags();
        this.connectionId = id;
    }

    /**
     * sets the counter to the initial value
     * this value has to be restored if the connection is rollbacked
     */
    public void setInitialCounter(int value) {
        this.increment = 0;
        this.initialValue = value;
        this.getXADataRecorder().writeRollbackData(Integer.toString(value).getBytes());
    }


    @Override
    public boolean isInterruptFlag(TestInterruptionPoint interruptionPoint) {
        return this.interruptionFlags.get(interruptionPoint) <= 0;
    }

    @Override
    public void setInterruptFlag(TestInterruptionPoint interruptionPoint, int gate) {
        interruptionFlags.put(interruptionPoint, gate);
    }

    @Override
    public void setInterruptFlag(TestInterruptionPoint interruptionPoint) {
        this.setInterruptFlag(interruptionPoint, 1);
    }


    public int getCounter() {
        return this.initialValue + this.increment;
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ITestConnection#getConnectionId()
     */
    public Object getConnectionId() {
        return connectionId;
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ITestConnection#act()
     */
    public void act(int inc) {

        interrupt(TestInterruptionPoint.ACT);
        this.increment = this.increment + inc;
        log.info("TestConnection " + connectionId + " counter incremented to " + inc + " counter=" + this.getCounter());

    }


    @Override
    public void reset() {
        privReset();

    }

    /**
     * reset without being tracked by the Listeners
     */
    private void privReset() {
        resetInterruptionFlags();
        this.increment = 0;
        this.initialValue=0;
    }

    public void close() {
        this.interrupt(TestInterruptionPoint.CLOSE);
        this.privReset();
        log.info("TestConnection " + connectionId + " closed");

    }

    public void commit() {
        try {
            byte[] bytes = new LogRecordWriter().writeInt(this.initialValue).writeInt(this.increment).toByteArray();
            if (this.getXADataRecorder() != null) {
                this.getXADataRecorder().writeRollforwardData(bytes);
            }
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
        interrupt(TestInterruptionPoint.COMMIT);
        this.initialValue = this.initialValue+ increment;
        this.increment =0;
        log.info("TestConnection " + connectionId + " is committed");

    }

    public void prepare() {
        interrupt(TestInterruptionPoint.PREPARE);
        log.info("TestConnection " + connectionId + " is prepared");
    }

    public void rollback() {
        interrupt(TestInterruptionPoint.ROLLBACK);
        this.increment = 0;
        log.info("TestConnection " + connectionId + " rollbacked");
    }

    public String toString() {
        return "TestConnection " + connectionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestConnection that = (TestConnection) o;

        if (connectionId != null ? !connectionId.equals(that.connectionId) : that.connectionId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return connectionId != null ? connectionId.hashCode() : 0;
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

        @Override
        public void notifyNoMoreData() {

        }

        public void replayRollback(IDataRecord message) {
            int initialValue = Integer.parseInt(new String(message.getData()[0]));
            this.con.increment = initialValue;
        }

        public void replayRollforward(IDataRecord message) {

            try {
                LogRecordReader logRecordReader = new LogRecordReader(message.getData()[0]);
                this.con.initialValue = logRecordReader.readInt();
                this.con.increment = logRecordReader.readInt();
            } catch (Exception e) {
                throw new DelegatedRuntimeException(e);
            }
        }

    }
}
