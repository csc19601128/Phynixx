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


import org.csc.phynixx.connection.IAutoCommitAware;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IXADataRecorderAware;
import org.csc.phynixx.connection.RequiresTransaction;

/**
 * the current implementation manages a internal counter which can be incremented
 *
 * @author christoph
 */
public interface ITestConnection extends IPhynixxConnection, IXADataRecorderAware, IAutoCommitAware {


    /**
     * sets the counter to the initial value
     * this value has to be restored if the connection is rolled back
     */

    @RequiresTransaction
    void setInitialCounter(int value);


    /**
     * @return current ID of the connection
     */
    public Object getConnectionId();

    /**
     * increments the current counter. In value of the counter is rolled back
     *
     * @param inc
     */
    @RequiresTransaction
    public void act(int inc);


    boolean isInterruptFlag(TestInterruptionPoint interruptionPoint);

    void setInterruptFlag(TestInterruptionPoint interruptionPoint, int gate);

    void setInterruptFlag(TestInterruptionPoint interruptionPoint);

    /**
     * @return current counter
     */
    public int getCounter();

}
