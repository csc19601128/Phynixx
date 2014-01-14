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
 *
 * @param <C> Typ of the connection
 *
 */

public interface IPhynixxConnectionProxyListener<C extends IPhynixxConnection> {

    /**
     * called when the connection is closed
     * Called after closing the connection
     *
     * @param event current connection
     */
    void connectionClosed(IPhynixxConnectionProxyEvent<C> event);

    /**
     * connectionErrorOccurred � triggered when a fatal error,
     * such as the server crashing, causes the connection to be lost
     *
     * @param event current connection
     */
    void connectionErrorOccurred(IPhynixxConnectionProxyEvent<C> event);

    /**
     * connectionRequiresTransaction - an action should be performed that
     * must be transactional
     *
     * @param event current connection
     */
    void connectionRequiresTransaction(IPhynixxConnectionProxyEvent<C> event);

    /**
     * connectionRolledback
     */
    void connectionRolledback(IPhynixxConnectionProxyEvent<C> event);

    /**
     * connection enters state 'committing'
     *
     * @param event
     */
    void connectionCommitting(IPhynixxConnectionProxyEvent<C> event);

    /**
     * connectionCommitted
     */
    void connectionCommitted(IPhynixxConnectionProxyEvent<C> event);

    /**
     * connection enters state 'preparing'
     *
     * @param event
     */
    void connectionPreparing(IPhynixxConnectionProxyEvent<C> event);


    /**
     * connection is prepared
     */
    void connectionPrepared(IPhynixxConnectionProxyEvent<C> event);


    /**
     * connection dereferenced
     */
    void connectionDereferenced(IPhynixxConnectionProxyEvent<C> event);

    /**
     * connection referenced
     */
    void connectionReferenced(IPhynixxConnectionProxyEvent<C> event);


    /**
     * connection enters state 'recovering'
     *
     * @param event
     */
    void connectionRecovering(IPhynixxConnectionProxyEvent<C> event);

    /**
     * connection recovered
     */
    void connectionRecovered(IPhynixxConnectionProxyEvent<C> event);

    /**
     * starts rolling back the connection
     *
     * @param event
     */
    void connectionRollingBack(IPhynixxConnectionProxyEvent<C> event);

}
