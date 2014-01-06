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
import java.util.Iterator;
import java.util.Map;


public class ConnectionTray extends PhynixxConnectionProxyListenerAdapter implements IPhynixxConnectionProxyListener {

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

    private Map referencedConnections = new HashMap();

    private XAPooledConnectionFactory connectionFactory = null;


    ConnectionTray(IPhynixxConnectionFactory connectionFactory) {
        this.connectionFactory = new XAPooledConnectionFactory(connectionFactory);
    }

    private IPhynixxConnectionFactory getCoreConnectionFactory() {
        return this.connectionFactory;
    }

    synchronized IPhynixxConnection getFreeConnenction() {
        IPhynixxConnection con = this.connectionFactory.getConnection();
        return con;

    }

    synchronized int freeConnectionSize() {
        int maxCon = this.connectionFactory.getMaxActive();
        return maxCon - this.referencedConnections.size();

    }

    synchronized boolean isFreeConnection(IPhynixxConnection con) {
        return !this.referencedConnections.containsKey(con);
    }

    synchronized void close() {
        // release the referenced connections
        for (Iterator iterator = this.referencedConnections.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iterator.next();
            IPhynixxConnection con = (IPhynixxConnection) entry.getKey();
            this.connectionFactory.releaseConnection(con);
        }
        this.referencedConnections.clear();

        // ...and close the pooled factory
        this.connectionFactory.close();

    }

    public synchronized void connectionDereferenced(IPhynixxConnectionProxyEvent event) {
        IPhynixxConnection connection = event.getConnectionProxy().getConnection();
        if (connection != null) {
            RefCounter refCounter = (RefCounter) this.referencedConnections.get(connection);
            if (refCounter == null) {
                throw new IllegalStateException("Connection " + connection + " is not registerd");
            }
            if (refCounter.getRefCount() <= 1) {
                if (!connection.isClosed()) {
                    this.referencedConnections.remove(connection);
                    log.debug("Connection " + connection + " freed");
                    this.connectionFactory.releaseConnection(connection);
                }
            } else {
                refCounter.decreaseRefCount();
                log.debug("Connection " + connection + " dereferenced (refCount=" + refCounter.getRefCount() + ")");
            }
        }
    }

    public synchronized void connectionReferenced(IPhynixxConnectionProxyEvent event) {

        IPhynixxConnection connection = event.getConnectionProxy().getConnection();
        if (connection != null) {
            RefCounter refCounter = (RefCounter) this.referencedConnections.get(connection);
            if (refCounter == null) {
                refCounter = new RefCounter();
                this.referencedConnections.put(connection, refCounter);
            }
            refCounter.increaseRefCount();
            log.debug("Connection " + connection + " referenced (refCount=" + refCounter.getRefCount() + ")");

        }
    }

    public synchronized void connectionClosed(IPhynixxConnectionProxyEvent event) {

        // dereference the connection
        event.getConnectionProxy().setConnection(null);

        // Check, if it is really closed ...
        IPhynixxConnection con = event.getConnectionProxy().getConnection();
        this.connectionFactory.releaseConnection(con);

        event.getConnectionProxy().removeConnectionListener(this);

    }

}
