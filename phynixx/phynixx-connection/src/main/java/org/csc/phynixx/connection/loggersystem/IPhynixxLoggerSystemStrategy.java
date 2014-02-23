package org.csc.phynixx.connection.loggersystem;

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


import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxConnectionProxyDecorator;
import org.csc.phynixx.connection.IPhynixxManagedConnectionListener;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;

import java.util.List;

/**
 * this IF represents a strategy assigning loggers to connections.
 * <p/>
 * <pre>
 * Different strategies could be :
 *       no logger,
 *       one logger per transaction,
 *       one logger per connection,
 *       one logger per System
 *  </pre>
 */
public interface IPhynixxLoggerSystemStrategy<C extends IPhynixxConnection> extends IPhynixxManagedConnectionListener<C>, IPhynixxConnectionProxyDecorator<C> {

    /**
     * closes the strategy including all resources
     */
    void close();

    /**
     * recovers all Loggers of the system and returns a list of all reopen message sequences
     * Each message sequence represents an incomplete transaction.
     * To be able to recover the connection the message sequence is converted to a IMessageLogger
     *
     * @return list of Objects of type IMessageLogger
     */
    List<IXADataRecorder> readIncompleteTransactions();


}
