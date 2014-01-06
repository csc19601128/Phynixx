package org.csc.phynixx.connection;

/*
 * #%L
 * phynixx-common
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


public class PooledConnectionFactory extends ConnectionFactory implements IPhynixxConnectionFactory, IPhynixxConnectionProxyListener {
    private IPhynixxLogger logger = PhynixxLogManager.getLogger(this.getClass());

    private GenericObjectPool genericObjectPool = null;

    /**
     * implementation of the contract of generic pools to manage pooled elements
     *
     * @author christoph
     */
    private class MyPoolableObjectFactory implements PoolableObjectFactory {


        public void activateObject(Object obj) throws Exception {

            if (logger.isDebugEnabled()) {
                logger.debug("Activated " + obj);
            }
        }

        public void destroyObject(Object obj) throws Exception {
            IPhynixxConnectionProxy ch = (IPhynixxConnectionProxy) obj;
            ch.getConnection().close();
        }

        public Object makeObject() throws Exception {
            IPhynixxConnection con = PooledConnectionFactory.this.instanciateConnection();
            return con;
        }

        public void passivateObject(Object obj) throws Exception {
            if (logger.isDebugEnabled()) {
                logger.debug("Passivate " + obj);
            }
        }

        public boolean validateObject(Object obj) {

            IPhynixxConnection con = ((IPhynixxConnectionProxy) obj).getConnection();
            return !(con.isClosed());
        }

    }

    public PooledConnectionFactory() {
        super();
    }


    public PooledConnectionFactory(IPhynixxConnectionFactory connectionFactory) {
        this(connectionFactory, null, null);
    }

    public PooledConnectionFactory(IPhynixxConnectionFactory connectionFactory,
                                   GenericObjectPool.Config genericPoolConfig) {
        this(connectionFactory, null, genericPoolConfig);
    }

    public PooledConnectionFactory(IPhynixxConnectionFactory connectionFactory,
                                   IPhynixxConnectionProxyFactory connectionProxyFactory,
                                   GenericObjectPool.Config genericPoolConfig) {
        super(connectionFactory, connectionProxyFactory);
        GenericObjectPool.Config cfg = genericPoolConfig;
        if (cfg == null) {
            cfg = new GenericObjectPool.Config();
        }
        this.genericObjectPool = new GenericObjectPool(new MyPoolableObjectFactory(), cfg);
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
        this.genericObjectPool = new GenericObjectPool(new MyPoolableObjectFactory(), cfg);
    }


    public IPhynixxConnection getConnection() {
        try {
            return (IPhynixxConnection) this.genericObjectPool.borrowObject();
        } catch (Throwable e) {
            throw new DelegatedRuntimeException("Instanciating new pooled Proxy", e);
        }
    }

    public void releaseConnection(IPhynixxConnection connection) {
        if (connection == null) {
            return;
        }
        try {
            this.genericObjectPool.returnObject(connection);
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Proxy " + connection + " returned to Pool");
        }
    }

    public void destroyConnection(IPhynixxConnection connection) {
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

    public Class connectionInterface() {
        return this.getConnectionFactory().connectionInterface();
    }

	/*
	public void recover() {
		
		// get all recoverable transaction data
		List messageLoggers= this.loggerSystemStrategy.readIncompleteTransactions();
		IPhynixxConnection con=null ;
		for(int i=0; i < messageLoggers.size();i++) {
			try {
				IRecordLogger msgLogger= (IRecordLogger) messageLoggers.get(i);			
				con= this.getConnection();
				if( (con instanceof IRecordLoggerAware)) {
					((IRecordLoggerAware)con).setRecordLogger(msgLogger);
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
    public void connectionClosed(IPhynixxConnectionProxyEvent event) {
        IPhynixxConnectionProxy proxy = event.getConnectionProxy();
        if (proxy.getConnection() == null) {
            return;
        }
        this.releaseConnection(proxy);
        if (logger.isDebugEnabled()) {
            logger.debug("Proxy " + proxy + " released");
        }

    }

    public void connectionDereferenced(IPhynixxConnectionProxyEvent event) {
        throw new IllegalStateException("Connection is bound to a proxy and cann't be released");
    }


}
