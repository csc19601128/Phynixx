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


import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxConnectionFactory;
import org.csc.phynixx.connection.IPhynixxConnectionProxyFactory;
import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.watchdog.IWatchdog;
import org.csc.phynixx.watchdog.IWatchedCondition;
import org.csc.phynixx.watchdog.WatchdogRegistry;

import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class PhynixxResourceFactory implements IPhynixxXAResourceListener {

    private static final long CHECK_INTERVAL = 100; // msecs

    private static IResourceIDGenerator idGenerator = new IDGenerator();
    private XAResourceTxStateManager xaresourceTxStateManager = new XAResourceTxStateManager();
    private ConnectionTray connectionTray = null;

    private Set xaresources = Collections.synchronizedSet(new HashSet());

    private Object resourceFactoryId = null;
    private IPhynixxConnectionProxyFactory connectionProxyFactory;
    private TransactionManager transactionManager = null;
    private IWatchdog xaresourrceWatchdog = null;

    public PhynixxResourceFactory(
            IPhynixxConnectionFactory connectionFactory,
            IPhynixxConnectionProxyFactory connectionProxyFactory,
            TransactionManager transactionManager) {
        this("RF", connectionFactory, connectionProxyFactory, transactionManager);
    }

    public PhynixxResourceFactory(
            Object id,
            IPhynixxConnectionFactory connectionFactory,
            IPhynixxConnectionProxyFactory connectionProxyFactory,
            TransactionManager transactionManager) {
        this.resourceFactoryId = id;
        this.connectionTray = new ConnectionTray(connectionFactory);

        this.connectionProxyFactory = connectionProxyFactory;
        this.transactionManager = transactionManager;
        this.xaresourrceWatchdog = WatchdogRegistry.getTheRegistry().createWatchdog(CHECK_INTERVAL);
    }


    public String getId() {
        return resourceFactoryId.toString();
    }

    public IPhynixxConnectionProxyFactory getConnectionProxyFactory() {
        return connectionProxyFactory;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public XAResourceTxStateManager getXAResourceTxStateManager() {
        return xaresourceTxStateManager;
    }

    public synchronized boolean isFreeConnection(IPhynixxConnection con) {
        return this.connectionTray.isFreeConnection(con);
    }


    protected IResourceIDGenerator getIdGenerator() {
        return idGenerator;
    }

    protected void setIdGenerator(IResourceIDGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    synchronized IPhynixxManagedConnection getConnection() {
        IPhynixxConnection con = this.connectionTray.getFreeConnenction();
        IPhynixxManagedConnection proxy = this.connectionProxyFactory.getConnectionProxy();

        proxy.addConnectionListener(this.connectionTray);
        proxy.setConnection(con);
        return proxy;
    }

    public final synchronized IPhynixxXAConnection getXAConnection() {
        IPhynixxManagedConnection proxy;
        proxy = this.getConnection();
        PhynixxXAResource xares = instanciateXAResource(proxy);
        return xares.getXAConnection();
    }

    private PhynixxXAResource instanciateXAResource(IPhynixxManagedConnection proxy) {
        PhynixxXAResource xares = new PhynixxXAResource(createXAResourceId(), this.transactionManager, this, proxy);
        xares.addXAResourceListener(this);
        this.xaresources.add(xares);
        return xares;
    }

    /**
     * Resource id helps debugging a xa resource.
     * It's unique for all xa resources of a resource factory
     *
     * @return
     */
    private String createXAResourceId() {
        return this.resourceFactoryId + "_" + this.idGenerator.generate();
    }

    public XAResource getXAResource() {
        PhynixxXAResource xaresource = instanciateXAResource(null);
        return xaresource;
    }

    public int freeConnectionSize() {
        return this.connectionTray.freeConnectionSize();
    }


    public void closed(IPhynixxXAResourceEvent event) {
        this.xaresources.remove(event.getXAResource());
    }

    /**
     * resource factory represents the persistence management system and is responsible
     * to implements system recovery
     * Subclasses have to implement die recovery
     *
     * @return recovered TX
     */
    public synchronized Xid[] recover() {
        return new Xid[]{};
    }

    /**
     * Closes all pending XAResources and all open but unused connections
     */
    public synchronized void close() {
        if (this.xaresources.size() > 0) {
            // copy all resources as the close of a resource modifies the xaresources ...
            Set tmpXAresources = new HashSet(this.xaresources);
            // close all open XAResources ....
            for (Iterator iterator = tmpXAresources.iterator(); iterator.hasNext(); ) {
                PhynixxXAResource xaresource = (PhynixxXAResource) iterator.next();
                xaresource.close();
            }
        }
        // Close all free connections
        this.connectionTray.close();
    }


    synchronized void registerWatchCondition(IWatchedCondition cond) {
        this.xaresourrceWatchdog.registerCondition(cond);
    }

    synchronized void unregisterWatchCondition(IWatchedCondition cond) {
        this.xaresourrceWatchdog.unregisterCondition(cond);
    }


}
