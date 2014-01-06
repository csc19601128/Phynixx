/**
 *
 */
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


class RecoveryListener extends PhynixxConnectionProxyListenerAdapter implements IPhynixxConnectionProxyDecorator {

    int recoveredConnections = 0;
    private int commmittedConnections = 0;
    private int rollbackedConnections = 0;

    public int getRecoveredConnections() {
        return recoveredConnections;
    }

    public int getCommmittedConnections() {
        return commmittedConnections;
    }

    public int getRollbackedConnections() {
        return rollbackedConnections;
    }

    public void connectionRecovered(IPhynixxConnectionProxyEvent event) {
        this.recoveredConnections++;
    }


    public void connectionCommitted(IPhynixxConnectionProxyEvent event) {
        this.commmittedConnections++;
    }

    public void connectionRollbacked(IPhynixxConnectionProxyEvent event) {
        this.rollbackedConnections++;
    }

    public IPhynixxConnectionProxy decorate(IPhynixxConnectionProxy connectionProxy) {
        connectionProxy.addConnectionListener(this);
        return connectionProxy;

    }


}
