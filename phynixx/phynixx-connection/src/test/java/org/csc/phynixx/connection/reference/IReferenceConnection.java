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


import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IXADataRecorderAware;

/**
 * the current implementation manages a internal counter which can be incremented
 *
 * @author christoph
 */
public interface IReferenceConnection extends IPhynixxConnection, IXADataRecorderAware {

    /**
     * @return current ID of the connection
     */
    public Object getId();

    /**
     * sets the counter to the initial value
     * this value has to be restored if the connection is rollbacked
     */
    void setInitialCounter(int value);

    /**
     * incrememnts the current counter
     *
     * @param inc
     */
    public void incCounter(int inc);


    /**
     * @return current counter
     */
    public int getCounter();

}
