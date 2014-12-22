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
import org.csc.phynixx.connection.IPhynixxManagedConnectionFactory;
import org.csc.phynixx.watchdog.IWatchdog;
import org.csc.phynixx.watchdog.IWatchedCondition;
import org.csc.phynixx.watchdog.WatchdogRegistry;

import javax.transaction.TransactionManager;
import javax.transaction.xa.Xid;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @param <T>
 */
public class PhynixxXAResourceFactory<T extends IPhynixxConnection> implements IPhynixxXAResourceFactory<T>, IPhynixxXAResourceListener<T> {

    private static final long CHECK_INTERVAL = 100; // msecs

    private static IResourceIDGenerator ID_GENERATOR = new IDGenerator();


    /**
     * this XAResources represents the Resources of this factory
     */
    PhynixxXAResource<T> xaResource;


    private final IXATransactionalBranchRepository<T> xaTransactionalBranchRepository;


    //  private PhynixxConnectionTray<T> connectionTray = null;


    /**
     * set of active xaresources
     */
    private Set<PhynixxXAResource<T>> xaresources = Collections.synchronizedSet(new HashSet());

    private Object resourceFactoryId = null;

    /**
     * factory instaciating the underlying connections
     */
    private IPhynixxManagedConnectionFactory<T> managedConnectionFactory;

    private TransactionManager transactionManager = null;

    private boolean supportsTimeOut= false;

    public boolean isSupportsTimeOut() {
        return supportsTimeOut;
    }

    public void setSupportsTimeOut(boolean supportsTimeOut) {
        this.supportsTimeOut = supportsTimeOut;
    }

    /**
     * checks timeouts
     */
    private IWatchdog xaresourrceWatchdog = null;

    public PhynixxXAResourceFactory(
            IPhynixxManagedConnectionFactory<T> connectionFactory,
            TransactionManager transactionManager) {
        this("RF", connectionFactory, transactionManager);
    }

    public PhynixxXAResourceFactory(
            Object id,
            IPhynixxManagedConnectionFactory<T> connectionFactory,
            TransactionManager transactionManager) {
        this.resourceFactoryId = id;

        //  this.connectionTray = new PhynixxConnectionTray(connectionFactory);

        this.managedConnectionFactory = connectionFactory;

        xaTransactionalBranchRepository = new XATransactionalBranchRepository();

        this.transactionManager = transactionManager;
        //this.xaresourrceWatchdog = WatchdogRegistry.getTheRegistry().createWatchdog(CHECK_INTERVAL);


        this.xaResource= instanciateXAResource();
    }


    public String getId() {
        return resourceFactoryId.toString();
    }

    public IPhynixxManagedConnectionFactory<T> getManagedConnectionFactory() {

        return managedConnectionFactory;
    }

    IXATransactionalBranchRepository<T> getXATransactionalBranchRepository() {
        return xaTransactionalBranchRepository;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }


    protected IResourceIDGenerator getIdGenerator() {
        return ID_GENERATOR;
    }

    protected static void setIdGenerator(IResourceIDGenerator idGenerator) {
        ID_GENERATOR = idGenerator;
    }


    private PhynixxXAResource<T> instanciateXAResource() {
        PhynixxXAResource<T> xares = new PhynixxXAResource<T>(createXAResourceId(), this.transactionManager, this);
        xares.addXAResourceListener(this);
        this.xaresources.add(xares);
        return xares;
    }

    public IPhynixxXAConnection<T> getXAConnection() {
        return this.xaResource.getXAConnection();
    }


    /**
     * XAResourceProgressState id helps debugging a xa resource.
     * It's unique for all xa resources of a resource factory
     *
     * @return
     */
    private String createXAResourceId() {
        return this.resourceFactoryId + "_" + this.ID_GENERATOR.generate();
    }

    /**
     * erzeugt eine neue XAResource ...
     *
     * @return
     */
    @Override
    public IPhynixxXAResource<T> getXAResource() {
        return this.xaResource;
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
     *
     * TODO recover still to be implemented
     */
    @Override
    public synchronized Xid[] recover() {
        return new Xid[]{};
    }

    /**
     * Closes all pending XAResources and all reopen but unused connections
     */
    @Override
    public synchronized void close() {
        if (this.xaresources.size() > 0) {
            // copy all resources as the close of a resource modifies the xaresources ...
            Set<PhynixxXAResource<T>> tmpXAResources = new HashSet(this.xaresources);
            // close all reopen XAResources ....
            for (Iterator<PhynixxXAResource<T>> iterator = tmpXAResources.iterator(); iterator.hasNext(); ) {
                PhynixxXAResource<T> xaresource = iterator.next();
                xaresource.close();
            }
        }

        this.getXATransactionalBranchRepository().close();
    }


    synchronized void registerWatchCondition(IWatchedCondition cond) {
        this.xaresourrceWatchdog.registerCondition(cond);
    }

    synchronized void unregisterWatchCondition(IWatchedCondition cond) {
        this.xaresourrceWatchdog.unregisterCondition(cond);
    }

	@Override
	public T getConnection() {
		return this.getXAResource().getXAConnection().getConnection();
	}

	@Override
	public Class<T> getConnectionInterface() {
		return this.managedConnectionFactory.getConnectionInterface();
	}


}
