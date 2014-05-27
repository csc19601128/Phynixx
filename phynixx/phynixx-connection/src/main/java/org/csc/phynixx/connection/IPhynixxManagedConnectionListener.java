package org.csc.phynixx.connection;

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


/**
 * lifecycle listener of an Connection
 *
 * @param <C> Typ of the connection
 */

public interface IPhynixxManagedConnectionListener<C extends IPhynixxConnection> {

    /**
     * called before the connection is opended
     *
     * @param event current connection
     */
    void connectionReset(IManagedConnectionEvent<C> event);

    /**
     * called after the connection is close.
     * A connection is released when is not any longer bound to a transactional context but should be reused
     *
     * {@link org.csc.phynixx.connection.IPhynixxManagedConnection#isClosed()} is true
     *
     * @param event current connection
     */
    void connectionReleased(IManagedConnectionEvent<C> event);

    /**
     * connection dereferenced
     */
    void connectionFreed(IManagedConnectionEvent<C> event);


    /**
     * connectionErrorOccurred is triggered when a fatal error,
     * such as the server crashing, causes the connection to be lost
     *
     * @param event current connection
     */
    void connectionErrorOccurred(IManagedConnectionEvent<C> event);

    /**
     * connectionRequiresTransaction - an action should be performed that
     * must be transactional
     *
     * @param event current connection
     */
    void connectionRequiresTransaction(IManagedConnectionEvent<C> event);

    /**
     * connectionRequiresTransaction - an action was performed that
     * must be transactional.
     * If the method fail the exception is attached to the event
     *
     * @param event current connection
     */
    void connectionRequiresTransactionExecuted(IManagedConnectionEvent<C> event);


    /**
     * connectionRolledback
     */
    void connectionRolledback(IManagedConnectionEvent<C> event);

    /**
     * connection enters state 'committing'
     *
     * @param event
     */
    void connectionCommitting(IManagedConnectionCommitEvent<C> event);

    /**
     * connectionCommitted
     * @param event
     */
    void connectionCommitted(IManagedConnectionCommitEvent<C> event);


    /**
     * connection enters state 'preparing'
     *
     * @param event
     */
    void connectionPreparing(IManagedConnectionEvent<C> event);


    /**
     * connection is prepared
     */
    void connectionPrepared(IManagedConnectionEvent<C> event);



    /**
     * connection enters state 'recovering'
     *
     * @param event
     */
    void connectionRecovering(IManagedConnectionEvent<C> event);

    /**
     * connection recovered
     */
    void connectionRecovered(IManagedConnectionEvent<C> event);

    /**
     * starts rolling back the connection
     *
     * @param event
     */
    void connectionRollingBack(IManagedConnectionEvent<C> event);


}
