package org.csc.phynixx.connection.reference;

/*
 * #%L
 * phynixx-common
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
import org.csc.phynixx.loggersystem.Dev0Strategy;
import org.csc.phynixx.loggersystem.messages.ILogRecord;
import org.csc.phynixx.loggersystem.messages.ILogRecordReplay;
import org.csc.phynixx.loggersystem.messages.IRecordLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * this reference implementation of a {@link IPhynixxConnection } shows how to
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

public class ReferenceConnection implements IReferenceConnection {

    public static final int ERRONEOUS_INC = Integer.MAX_VALUE;
    private static final Integer ERRONEOUS_VALUE = new Integer(ERRONEOUS_INC);
    private Object id = null;
    private int counter = 0;
    private volatile boolean close = false;
    private List increments = new ArrayList();


    private IPhynixxLogger logger = PhynixxLogManager.getLogger(this.getClass());

    private IRecordLogger recordLogger = Dev0Strategy.THE_DEV0_LOGGER;

    public IRecordLogger getRecordLogger() {
        return recordLogger;
    }

    public void setRecordLogger(IRecordLogger messageLogger) {
        this.recordLogger = messageLogger;
    }

    public ReferenceConnection(Object id) {
        this.id = id;
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


    public void setInitialCounter(int value) {
        this.counter = value;
        this.getRecordLogger().writeRollbackData(Integer.toString(value).getBytes());
    }

    /**
     * Increments are stored an executed during commit
     */
    public void incCounter(int inc) {
        this.increments.add(new Integer(inc));
    }

    public void close() {
        if (!isClosed()) {
            this.close = true;
        }
    }

    public boolean isClosed() {
        return this.close;
    }

    public void commit() {
        /**
         * All increments are stored as rollforward data to recover the commit
         */
        for (Iterator iterator = this.increments.iterator(); iterator.hasNext(); ) {
            Integer inc = (Integer) iterator.next();
            // the data is stored for recoverinmg the commiting phase
            this.getRecordLogger().commitRollforwardData(inc.toString().getBytes());
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
        // use the recovery data to rollback the connection ....
        this.getRecordLogger().replayRecords(new MessageReplay());
    }

    public String toString() {
        return "ReferenceConnection " + id;
    }

    public void recover() {
        this.getRecordLogger().replayRecords(new MessageReplay());
        logger.error(this.getId() + " recovered with initial value=" + this.getCounter());
    }

    private class MessageReplay implements ILogRecordReplay {

        public void replayRollback(ILogRecord message) {
            int initialCounter = Integer.parseInt(new String(message.getData()[0]));
            ReferenceConnection.this.counter = initialCounter;
        }

        /**
         * recover the increments
         */
        public void replayRollforward(ILogRecord message) {
            int inc = Integer.parseInt(new String(message.getData()[0]));
            Integer incObj = new Integer(inc);
            if (!incObj.equals(ERRONEOUS_VALUE)) {
                ReferenceConnection.this.increments.add(incObj);
            }
        }
    }

}
