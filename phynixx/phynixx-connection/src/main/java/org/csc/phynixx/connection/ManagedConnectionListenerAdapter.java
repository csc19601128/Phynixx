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
 * Noop Implementation of the IManagedConnectionListener.
 */
public class ManagedConnectionListenerAdapter<C extends IPhynixxConnection> implements IManagedConnectionListener<C> {


    /**
     * Noop
     *
     * @param event current connection
     */
    @Override
    public void connectionOpen(IManagedConnectionProxyEvent<C> event) {
    }

    /**
     * NOOP
     */
    public void connectionClosed(IManagedConnectionProxyEvent<C> event) {
    }

    /**
     * NOOP
     */
    public void connectionErrorOccurred(IManagedConnectionProxyEvent<C> event) {
    }

    /**
     * NOOP
     */
    public void connectionRequiresTransaction(IManagedConnectionProxyEvent<C> event) {
    }


    public void connectionCommitting(IManagedConnectionProxyEvent<C> event) {

    }

    public void connectionPrepared(IManagedConnectionProxyEvent<C> event) {

    }

    public void connectionPreparing(IManagedConnectionProxyEvent<C> event) {

    }

    /**
     * NOOP
     */
    public void connectionCommitted(IManagedConnectionProxyEvent<C> event) {
    }

    @Override
    public void connectionRollingBack(IManagedConnectionProxyEvent<C> event) {

    }

    /**
     * NOOP
     */
    public void connectionRolledback(IManagedConnectionProxyEvent<C> event) {
    }


    /**
     * NOOP
     */
    public void connectionDereferenced(IManagedConnectionProxyEvent<C> event) {
    }

    /**
     * NOOP
     */
    public void connectionReferenced(IManagedConnectionProxyEvent<C> event) {

    }

    public void connectionRecovered(IManagedConnectionProxyEvent<C> event) {

    }

    public void connectionRecovering(IManagedConnectionProxyEvent<C> event) {

    }


}
