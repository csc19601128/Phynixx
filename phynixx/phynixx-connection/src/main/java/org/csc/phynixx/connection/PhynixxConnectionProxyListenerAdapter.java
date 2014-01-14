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
 * Noop Implementation of the IPhynixxConnectionProxyListener.
 */
public class PhynixxConnectionProxyListenerAdapter<C extends IPhynixxConnection> implements IPhynixxConnectionProxyListener<C> {

    /**
     * NOOP
     */
    public void connectionClosed(IPhynixxConnectionProxyEvent<C> event) {
    }

    /**
     * NOOP
     */
    public void connectionErrorOccurred(IPhynixxConnectionProxyEvent<C> event) {
    }

    /**
     * NOOP
     */
    public void connectionRequiresTransaction(IPhynixxConnectionProxyEvent<C> event) {
    }


    public void connectionCommitting(IPhynixxConnectionProxyEvent<C> event) {

    }

    public void connectionPrepared(IPhynixxConnectionProxyEvent<C> event) {

    }

    public void connectionPreparing(IPhynixxConnectionProxyEvent<C> event) {

    }

    /**
     * NOOP
     */
    public void connectionCommitted(IPhynixxConnectionProxyEvent<C> event) {
    }

    @Override
    public void connectionRollingBack(IPhynixxConnectionProxyEvent<C> event) {

    }

    /**
     * NOOP
     */
    public void connectionRolledback(IPhynixxConnectionProxyEvent<C> event) {
    }


    /**
     * NOOP
     */
    public void connectionDereferenced(IPhynixxConnectionProxyEvent<C> event) {
    }

    /**
     * NOOP
     */
    public void connectionReferenced(IPhynixxConnectionProxyEvent<C> event) {

    }

    public void connectionRecovered(IPhynixxConnectionProxyEvent<C> event) {

    }

    public void connectionRecovering(IPhynixxConnectionProxyEvent<C> event) {

    }


}
