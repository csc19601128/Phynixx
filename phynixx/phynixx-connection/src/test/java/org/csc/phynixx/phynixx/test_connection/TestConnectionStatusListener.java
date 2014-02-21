/**
 *
 */
package org.csc.phynixx.phynixx.test_connection;

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


import org.csc.phynixx.connection.IManagedConnectionProxyEvent;
import org.csc.phynixx.connection.IPhynixxConnectionProxyDecorator;
import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.connection.PhynixxManagedConnectionListenerAdapter;

public class TestConnectionStatusListener extends PhynixxManagedConnectionListenerAdapter<ITestConnection> implements IPhynixxConnectionProxyDecorator<ITestConnection> {


    @Override
    public void connectionCommitted(IManagedConnectionProxyEvent<ITestConnection> event) {
        TestConnectionStatusManager.registerStatus(event.getManagedConnection(), TestConnectionStatus.COMMITTED);
    }

    @Override
    public void connectionReset(IManagedConnectionProxyEvent<ITestConnection> event) {
        TestConnectionStatusManager.registerStatus(event.getManagedConnection(), TestConnectionStatus.RESET);
    }

    @Override
    public void connectionClosed(IManagedConnectionProxyEvent<ITestConnection> event) {
        try {
            TestConnectionStatusManager.registerStatus(event.getManagedConnection(), TestConnectionStatus.CLOSED);
        } finally {
            event.getManagedConnection().removeConnectionListener(this);
        }
    }

    @Override
    public void connectionErrorOccurred(IManagedConnectionProxyEvent<ITestConnection> event) {
        TestConnectionStatusManager.registerStatus(event.getManagedConnection(), TestConnectionStatus.ERROR_OCCURRED);
    }

    @Override
    public void connectionRequiresTransaction(IManagedConnectionProxyEvent<ITestConnection> event) {
        TestConnectionStatusManager.registerStatus(event.getManagedConnection(), TestConnectionStatus.REQUIRES_TRANSACTION);
    }

    @Override
    public void connectionPrepared(IManagedConnectionProxyEvent<ITestConnection> event) {
        TestConnectionStatusManager.registerStatus(event.getManagedConnection(), TestConnectionStatus.PREPARED);
    }

    @Override
    public void connectionRolledback(IManagedConnectionProxyEvent<ITestConnection> event) {
        TestConnectionStatusManager.registerStatus(event.getManagedConnection(), TestConnectionStatus.ROLLEDBACK);
    }

    @Override
    public void connectionRecovered(IManagedConnectionProxyEvent<ITestConnection> event) {
        TestConnectionStatusManager.registerStatus(event.getManagedConnection(), TestConnectionStatus.RECOVERED);
    }

    public IPhynixxManagedConnection decorate(IPhynixxManagedConnection<ITestConnection> connectionProxy) {
        connectionProxy.addConnectionListener(this);
        return connectionProxy;

    }

}
