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

public interface IManagedConnectionListener<C extends IPhynixxConnection> {

    /**
     * called before the connection is opended
     *
     * @param event current connection
     */
    void connectionOpen(IManagedConnectionProxyEvent<C> event);

    /**
     * called after the connection is closed
     *
     * @param event current connection
     */
    void connectionClosed(IManagedConnectionProxyEvent<C> event);

    /**
     * connectionErrorOccurred is triggered when a fatal error,
     * such as the server crashing, causes the connection to be lost
     *
     * @param event current connection
     */
    void connectionErrorOccurred(IManagedConnectionProxyEvent<C> event);

    /**
     * connectionRequiresTransaction - an action should be performed that
     * must be transactional
     *
     * @param event current connection
     */
    void connectionRequiresTransaction(IManagedConnectionProxyEvent<C> event);

    /**
     * connectionRolledback
     */
    void connectionRolledback(IManagedConnectionProxyEvent<C> event);

    /**
     * connection enters state 'committing'
     *
     * @param event
     */
    void connectionCommitting(IManagedConnectionProxyEvent<C> event);

    /**
     * connectionCommitted
     */
    void connectionCommitted(IManagedConnectionProxyEvent<C> event);


    /**
     * connection enters state 'preparing'
     *
     * @param event
     */
    void connectionPreparing(IManagedConnectionProxyEvent<C> event);


    /**
     * connection is prepared
     */
    void connectionPrepared(IManagedConnectionProxyEvent<C> event);

    /**
     * connection dereferenced
     */
    void connectionDereferenced(IManagedConnectionProxyEvent<C> event);


    /**
     * connection referenced
     */
    void connectionReferenced(IManagedConnectionProxyEvent<C> event);

    /**
     * connection enters state 'recovering'
     *
     * @param event
     */
    void connectionRecovering(IManagedConnectionProxyEvent<C> event);

    /**
     * connection recovered
     */
    void connectionRecovered(IManagedConnectionProxyEvent<C> event);

    /**
     * starts rolling back the connection
     *
     * @param event
     */
    void connectionRollingBack(IManagedConnectionProxyEvent<C> event);
}
