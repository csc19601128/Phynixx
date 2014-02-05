package org.csc.phynixx.xa;

/*
 * #%L
 * phynixx-xa
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

import org.csc.phynixx.connection.*;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;

import java.util.HashMap;
import java.util.Map;


/**
 *
 */
public class PhynixxConnectionTray<C extends IPhynixxConnection> extends PhynixxManagedConnectionListenerAdapter<C> implements IPhynixxManagedConnectionListener<C> {

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private static class RefCounter {
        private int refCount = 0;

        public int getRefCount() {
            return refCount;
        }

        public void increaseRefCount() {
            this.refCount++;
        }

        public void decreaseRefCount() {
            this.refCount--;
        }

    }

    private Map<IPhynixxManagedConnection<C>, RefCounter> referencedConnections = new HashMap<IPhynixxManagedConnection<C>, RefCounter>();

    private IPhynixxManagedConnectionFactory<C> connectionFactory;

    public PhynixxConnectionTray(IPhynixxManagedConnectionFactory<C> connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private IPhynixxManagedConnectionFactory<C> getCoreConnectionFactory() {
        return this.connectionFactory;
    }

    @Deprecated
    IPhynixxManagedConnection<C> getFreeConnenction() {
        IPhynixxManagedConnection<C> con = this.connectionFactory.getManagedConnection();
        return con;
    }

    int freeConnectionSize() {
        return 1 - this.referencedConnections.size();

    }

    boolean isFreeConnection(IPhynixxManagedConnection<C> con) {
        return !this.referencedConnections.containsKey(con);
    }

    void close() {
        // release the referenced connections
        for (IPhynixxManagedConnection<C> connection : this.referencedConnections.keySet()) {
            connection.close();
        }
        this.referencedConnections.clear();

        // ...and close the pooled factory
        this.connectionFactory.close();

    }

    public void connectionDereferenced(IManagedConnectionProxyEvent<C> event) {
        IPhynixxManagedConnection<C> connection = event.getManagedConnection();
        if (connection != null) {
            RefCounter refCounter = this.referencedConnections.get(connection);
            if (refCounter == null) {
                throw new IllegalStateException("Connection " + connection + " is not registerd");
            }
            if (refCounter.getRefCount() <= 1) {
                if (!connection.isClosed()) {
                    this.referencedConnections.remove(connection);
                    log.debug("Connection " + connection + " freed");
                    connection.close();
                }
            } else {
                refCounter.decreaseRefCount();
                log.debug("Connection " + connection + " dereferenced (refCount=" + refCounter.getRefCount() + ")");
            }
        }
    }

    public void connectionReferenced(IManagedConnectionProxyEvent event) {

        IPhynixxManagedConnection<C> connection = event.getManagedConnection();
        if (connection != null) {
            RefCounter refCounter = this.referencedConnections.get(connection);
            if (refCounter == null) {
                refCounter = new RefCounter();
                this.referencedConnections.put(connection, refCounter);
            }
            refCounter.increaseRefCount();
            log.debug("Connection " + connection + " referenced (refCount=" + refCounter.getRefCount() + ")");

        }
    }

    public void connectionClosed(IManagedConnectionProxyEvent event) {

        IPhynixxManagedConnection managedConnection = event.getManagedConnection();
        this.referencedConnections.remove(managedConnection);
        event.getManagedConnection().removeConnectionListener(this);

    }

}
