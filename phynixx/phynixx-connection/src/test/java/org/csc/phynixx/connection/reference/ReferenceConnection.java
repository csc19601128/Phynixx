package org.csc.phynixx.connection.reference;

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


import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.IXADataRecorderAware;
import org.csc.phynixx.connection.RequiresTransaction;
import org.csc.phynixx.connection.loggersystem.Dev0Strategy;
import org.csc.phynixx.loggersystem.logrecord.IDataRecord;
import org.csc.phynixx.loggersystem.logrecord.IDataRecordReplay;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * this reference implementation of a {@link org.csc.phynixx.connection.IPhynixxConnection } shows how to
 * implement your own recoverable connection
 * The business logic is kept quite simple. A counter is set to an initial value.
 * This value has to be restored when the connection is rollbacked or recovered.
 * <p/>
 * <p/>
 * You can increment this value, but the incrememts are not performed immediately but
 * are postponed to the commit phase.
 * Bevor the increment is perormed all incre,ments are stored as rollforward data.
 * If an exceptions is throws during the commit, these data are used to recover.
 *
 * @author christoph
 */

public class ReferenceConnection implements IReferenceConnection, IXADataRecorderAware {

    public static final int ERRONEOUS_INC = -1;

    private static final Integer ERRONEOUS_VALUE = new Integer(ERRONEOUS_INC);


    private Object id = null;

    private int counter = 0;

    private volatile boolean close = false;

    private boolean autoCommit;

    /**
     * store the increments as compensations for rollforward
     */
    private List<Integer> increments = new ArrayList<Integer>();


    private IPhynixxLogger logger = PhynixxLogManager.getLogger(this.getClass());


    private IXADataRecorder xaDataRecorder = Dev0Strategy.THE_DEV0_LOGGER;

    public IXADataRecorder getXADataRecorder() {
        return xaDataRecorder;
    }

    public void setXADataRecorder(IXADataRecorder dataRecorder) {
        this.xaDataRecorder = dataRecorder;
    }

    public ReferenceConnection(Object id) {
        this.id = id;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public int getCounter() {
        return counter;
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.ITestConnection#getId()
     */
    public Object getId() {
        return id;
    }


    /**
     * sets the initial value
     *
     * @param value
     */
    @RequiresTransaction
    public void setInitialCounter(int value) {
        this.counter = value;
        this.getXADataRecorder().writeRollbackData(Integer.toString(value).getBytes());
    }

    /**
     * Increments are stored and executed during commit
     * <p/>
     * For test test purpose only : if you increment {@link #ERRONEOUS_VALUE} the commit phase is interrupted.
     * This scenario shows the recovery from interrupted commits
     */
    public void incCounter(int inc) {
        this.increments.add(new Integer(inc));
    }

    public void reset() {
        increments.clear();
        counter = 0;
    }

    public void close() {
        if (!isClosed()) {
            this.close = true;
        }
    }

    public boolean isClosed() {
        return this.close;
    }

    /**
     * commit stores the increments as rollforward info.
     * If the incremets contain {@link #ERRONEOUS_VALUE}  the commit is interrupted after the rollforward data is written.
     * A recover completes the commit.
     *
     * the counter a executed twice
     */
    public void commit() {

        if (this.getXADataRecorder() == null) {
            return;
        }
        /**
         * All increments are stored as rollforward data to recover the commit
         */
        for (Iterator iterator = this.increments.iterator(); iterator.hasNext(); ) {
            Integer inc = (Integer) iterator.next();
            // the data is stored for recovering the committing phase
            this.getXADataRecorder().commitRollforwardData(inc.toString().getBytes());
            this.counter = this.counter + inc.intValue();
        }
        /**
         * execute the increment.
         * If an error occurs, you can rely on the recovering
         */
        for (Iterator iterator = this.increments.iterator(); iterator.hasNext(); ) {
            Integer inc = (Integer) iterator.next();
            if (inc.equals(ERRONEOUS_VALUE)) {
                throw new IllegalArgumentException("Erroneous increment");
            }
            this.counter = this.counter + inc.intValue();
        }
    }

    public void prepare() {
    }

    public void rollback() {
        if (this.getXADataRecorder() != null) {
            // use the recovery data to rollback the connection ....
        this.getXADataRecorder().replayRecords(new MessageReplay(this));
        }
    }

    public String toString() {
        return "ReferenceConnection " + id;
    }


    @Override
    public IDataRecordReplay recoverReplayListener() {
        return new MessageReplay(this);
    }

    static class MessageReplay implements IDataRecordReplay {

        private ReferenceConnection con;

        MessageReplay(ReferenceConnection con) {
            this.con = con;
        }

        public void replayRollback(IDataRecord message) {
            int initialCounter = Integer.parseInt(new String(message.getData()[0]));
            this.con.counter = initialCounter;
        }

        /**
         * recover the increments
         */
        public void replayRollforward(IDataRecord message) {
            int inc = Integer.parseInt(new String(message.getData()[0]));
            Integer incObj = new Integer(inc);
            if (!incObj.equals(ERRONEOUS_VALUE)) {
                this.con.increments.add(incObj);
            }
        }
    }

}
