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


import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;

/**
 * Factory pools the pure connection. before delivering the connection it ist decorate by the according to {@link org.csc.phynixx.connection.ManagedConnectionFactory}.
 *
 * @param <C> Typ of the pure connection
 */
public class PooledManagedConnectionFactory<C extends IPhynixxConnection> extends ManagedConnectionFactory<C> {
    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(PooledManagedConnectionFactory.class);

    private GenericObjectPool<C> genericObjectPool = null;

    /**
     * implementation of the contract of generic pools to manage pooled elements
     *
     * @author christoph
     */
    private static class MyPoolableObjectFactory<X extends IPhynixxConnection> implements PoolableObjectFactory<X> {

        PooledManagedConnectionFactory<X> managedConnectionFactory;

        private MyPoolableObjectFactory(PooledManagedConnectionFactory<X> managedConnectionFactory) {
            this.managedConnectionFactory = managedConnectionFactory;
        }

        public void activateObject(X obj) throws Exception {
            // opens the connection
            obj.open();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Activated " + obj);
            }
        }

        public void destroyObject(X obj) throws Exception {
            IManagedConnectionProxy ch = (IManagedConnectionProxy) obj;
            ch.getConnection().close();
        }

        public X makeObject() throws Exception {
            X con = this.managedConnectionFactory.instantiateConnection();
            return con;
        }

        public void passivateObject(X obj) throws Exception {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Passivated " + obj);
            }
        }

        public boolean validateObject(X obj) {
            X con = ((IManagedConnectionProxy<X>) obj).getConnection();
            return !(con.isClosed());
        }

    }

    public PooledManagedConnectionFactory() {
        super();
    }


    public PooledManagedConnectionFactory(IPhynixxConnectionFactory connectionFactory) {
        this(connectionFactory, null);
    }

    public PooledManagedConnectionFactory(IPhynixxConnectionFactory<C> connectionFactory,
                                          GenericObjectPool.Config genericPoolConfig) {
        super(connectionFactory);
        GenericObjectPool.Config cfg = genericPoolConfig;
        if (cfg == null) {
            cfg = new GenericObjectPool.Config();
        }
        this.genericObjectPool = new GenericObjectPool(new MyPoolableObjectFactory<C>(this), cfg);
    }

    /**
     * closes the current pool -if existing- and instanciates a new pool
     *
     * @param cfg
     * @throws Exception
     */
    public void setGenericPoolConfig(GenericObjectPool.Config cfg) throws Exception {
        if (this.genericObjectPool != null) {
            this.genericObjectPool.close();
        }
        if (cfg == null) {
            cfg = new GenericObjectPool.Config();
        }
        this.genericObjectPool = new GenericObjectPool(new MyPoolableObjectFactory<C>(this), cfg);
    }


    public C getConnection() {
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
    public void releaseConnection(C connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
            this.genericObjectPool.returnObject(connection);
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Proxy " + connection + " returned to Pool");
        }
    }

    public void destroyConnection(C connection) {
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

    public Class<C> connectionInterface() {
        return this.getConnectionFactory().connectionInterface();
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
        IManagedConnectionProxy<C> proxy = event.getConnectionProxy();
        if (proxy.getConnection() == null) {
            return;
        }
        this.releaseConnection(proxy.getConnection());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Proxy " + proxy + " released");
        }

    }

    public void connectionDereferenced(IManagedConnectionProxyEvent event) {
        throw new IllegalStateException("Connection is bound to a proxy and can't be released");
    }


}
