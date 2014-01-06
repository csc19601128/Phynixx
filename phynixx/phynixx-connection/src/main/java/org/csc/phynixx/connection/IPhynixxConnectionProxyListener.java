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


public interface IPhynixxConnectionProxyListener {

    /**
     * called when the connection is closed
     * Called after closing the connection
     *
     * @param con current connection
     */
    void connectionClosed(IPhynixxConnectionProxyEvent event);

    /**
     * connectionErrorOccurred � triggered when a fatal error,
     * such as the server crashing, causes the connection to be lost
     *
     * @param con current connection
     */
    void connectionErrorOccurred(IPhynixxConnectionProxyEvent event);

    /**
     * connectionRequiresTransaction - an action should be performed that
     * must be transactional
     *
     * @param con current connection
     */
    void connectionRequiresTransaction(IPhynixxConnectionProxyEvent event);

    /**
     * connectionRolledback
     */
    void connectionRolledback(IPhynixxConnectionProxyEvent event);

    /**
     * connection enters state 'committing'
     *
     * @param event
     */
    void connectionCommitting(IPhynixxConnectionProxyEvent event);

    /**
     * connectionCommitted
     */
    void connectionCommitted(IPhynixxConnectionProxyEvent event);

    /**
     * connection enters state 'preparing'
     *
     * @param event
     */
    void connectionPreparing(IPhynixxConnectionProxyEvent event);


    /**
     * connection is prepared
     */
    void connectionPrepared(IPhynixxConnectionProxyEvent event);


    /**
     * connection dereferenced
     */
    void connectionDereferenced(IPhynixxConnectionProxyEvent event);

    /**
     * connection referenced
     */
    void connectionReferenced(IPhynixxConnectionProxyEvent event);


    /**
     * connection enters state 'recovering'
     *
     * @param event
     */
    void connectionRecovering(IPhynixxConnectionProxyEvent event);

    /**
     * connection recovered
     */
    void connectionRecovered(IPhynixxConnectionProxyEvent event);


}
