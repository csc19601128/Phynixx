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


import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;

/**
 * Factory pools the pure connection. before delivering the connection it ist decorate by the according to {@link PhynixxManagedConnectionFactory}.
 *
 * @param <C> Typ of the pure connection
 */
public class PooledPhynixxManagedConnectionFactory<C extends IPhynixxConnection> extends PhynixxManagedConnectionFactory<C> {
    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(PooledPhynixxManagedConnectionFactory.class);

    private GenericObjectPool<IPhynixxManagedConnection<C>> genericObjectPool = null;

    /**
     * implementation of the contract of generic pools to manage pooled elements
     *
     * @author christoph
     */
    private static class MyPoolableObjectFactory<X extends IPhynixxConnection> extends BasePooledObjectFactory<IPhynixxManagedConnection<X>> implements PooledObjectFactory<IPhynixxManagedConnection<X>> {

        PooledPhynixxManagedConnectionFactory<X> managedConnectionFactory;

        private MyPoolableObjectFactory(PooledPhynixxManagedConnectionFactory<X> managedConnectionFactory) {
            this.managedConnectionFactory = managedConnectionFactory;
        }

        public void activateObject(PooledObject<IPhynixxManagedConnection<X>> obj) throws Exception {
            // opens the connection
            obj.getObject().open();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Activated " + obj);
            }
        }

        public void destroyObject(PooledObject<IPhynixxManagedConnection<X>> obj) throws Exception {
            IPhynixxManagedConnection ch = (IPhynixxManagedConnection) obj.getObject();
            ch.getConnection().close();
        }

        @Override
        public IPhynixxManagedConnection<X> create() throws Exception {
            return this.managedConnectionFactory.instantiateConnection();
        }

        @Override
        public PooledObject<IPhynixxManagedConnection<X>> wrap(IPhynixxManagedConnection<X> obj) {
            return new DefaultPooledObject<IPhynixxManagedConnection<X>>(obj);
        }

        public void passivateObject(PooledObject<IPhynixxManagedConnection<X>> obj) throws Exception {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Passivated " + obj.getObject());
            }
        }

        public boolean validateObject(PooledObject<IPhynixxManagedConnection<X>> obj) {
            X con = obj.getObject().getConnection();
            return !(con.isClosed());
        }

    }

    public PooledPhynixxManagedConnectionFactory() {
        super();
    }


    public PooledPhynixxManagedConnectionFactory(IPhynixxConnectionFactory connectionFactory) {
        this(connectionFactory, null);
    }

    public PooledPhynixxManagedConnectionFactory(IPhynixxConnectionFactory<C> connectionFactory,
                                                 GenericObjectPoolConfig genericPoolConfig) {
        super(connectionFactory);
        GenericObjectPoolConfig cfg = genericPoolConfig;
        if (cfg == null) {
            cfg = new GenericObjectPoolConfig();
        }
        this.genericObjectPool = new GenericObjectPool(new MyPoolableObjectFactory<C>(this), cfg);
    }

    /**
     * closes the current pool -if existing- and instanciates a new pool
     *
     *
     * @param cfg
     * @throws Exception
     */
    public void setGenericPoolConfig(GenericObjectPoolConfig cfg) throws Exception {
        if (this.genericObjectPool != null) {
            this.genericObjectPool.close();
        }
        if (cfg == null) {
            cfg = new GenericObjectPoolConfig();
        }
        this.genericObjectPool = new GenericObjectPool(new MyPoolableObjectFactory<C>(this), cfg);
    }

    @Override
    public IPhynixxManagedConnection<C> getManagedConnection() {
        try {
            return this.genericObjectPool.borrowObject();
        } catch (Throwable e) {
            throw new DelegatedRuntimeException("Instantiating new pooled Proxy", e);
        }
    }

    /**
     * closes the connection an releases it to the pool
     *
     * @param connection
     */
    public void releaseConnection(IPhynixxManagedConnection<C> connection) {
        if (connection == null) {
            return;
        }
        try {
            this.genericObjectPool.returnObject(connection);
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Proxy " + connection + " returned to Pool");
        }
    }

    public void destroyConnection(IPhynixxManagedConnection<C> connection) {
        if (connection == null) {
            return;
        }
        try {
            this.genericObjectPool.invalidateObject(connection);
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }

    public void close() {
        try {
            this.genericObjectPool.close();
            super.close();
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

    }

    public Class<C> getConnectionInterface() {
        return this.getConnectionFactory().getConnectionInterface();
    }

	/*
    public void recover() {
		
		// get all recoverable transaction data
		List messageLoggers= this.loggerSystemStrategy.readIncompleteTransactions();
		IPhynixxConnection con=null ;
		for(int i=0; i < messageLoggers.size();i++) {
			try {
				IXADataRecorder msgLogger= (IXADataRecorder) messageLoggers.get(i);
				con= this.getConnection();
				if( (con instanceof IXADataRecorderAware)) {
					((IXADataRecorderAware)con).setRecordLogger(msgLogger);
				}
				con.recover();
			} finally {
				if( con!=null) {
					con.close(); 
				}
			}
		}
		
	}
*/

    /**
     * the connection is released to the pool
     */
    public void connectionClosed(IManagedConnectionProxyEvent<C> event) {
        IPhynixxManagedConnection<C> proxy = event.getConnectionProxy();
        if (proxy.getConnection() == null) {
            return;
        }
        this.releaseConnection(proxy);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Proxy " + proxy + " released");
        }

    }

    public void connectionDereferenced(IManagedConnectionProxyEvent<C> event) {
        throw new IllegalStateException("Connection is bound to a proxy and can't be released");
    }


}
