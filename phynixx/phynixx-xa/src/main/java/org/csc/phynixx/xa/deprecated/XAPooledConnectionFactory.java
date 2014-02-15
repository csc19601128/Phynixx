package org.csc.phynixx.xa.deprecated;

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


import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxConnectionFactory;


public class XAPooledConnectionFactory<C extends IPhynixxConnection> implements IPhynixxConnectionFactory<C> {

    private class MyPoolableObjectFactory<X extends IPhynixxConnection> extends BasePooledObjectFactory<X> implements PooledObjectFactory<X> {

        private IPhynixxConnectionFactory<X> connectionFactory = null;

        MyPoolableObjectFactory(IPhynixxConnectionFactory<X> connectionFactory) {
            this.connectionFactory = connectionFactory;
        }

        @Override
        public X create() throws Exception {
            return connectionFactory.getConnection();
        }

        @Override
        public PooledObject<X> wrap(X obj) {
            return new DefaultPooledObject<X>(obj);
        }

        public void activateObject(PooledObject<X> obj) throws Exception {
        }

        public void destroyObject(PooledObject<X> obj) throws Exception {
            obj.getObject().close();
        }

        public void passivateObject(PooledObject<X> obj) throws Exception {
        }

        public boolean validateObject(PooledObject<X> obj) {
            return !(obj.getObject().isClosed());
        }

    }

    private IPhynixxConnectionFactory<C> connectionFactory = null;
    private GenericObjectPool<C> genericObjectPool = null;

    public XAPooledConnectionFactory(IPhynixxConnectionFactory<C> connectionFactory) {
        this(connectionFactory, null);
    }

    public XAPooledConnectionFactory(IPhynixxConnectionFactory<C> connectionFactory,
                                     GenericObjectPoolConfig genericPoolConfig) {
        this.connectionFactory = connectionFactory;
        GenericObjectPoolConfig cfg = genericPoolConfig;
        if (cfg == null) {
            cfg = new GenericObjectPoolConfig();
        }
        this.genericObjectPool = new GenericObjectPool(new MyPoolableObjectFactory(connectionFactory), cfg);
    }

    public IPhynixxConnectionFactory<C> getConnectionFactory() {
        return connectionFactory;
    }

    public int getMaxTotal() {
        return this.genericObjectPool.getMaxTotal();
    }

    public C getConnection() {
        try {
            return this.genericObjectPool.borrowObject();
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }

    public void releaseConnection(C connection) {
        if (connection == null) {
            return;
        }
        try {
            this.genericObjectPool.returnObject(connection);
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
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
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }

    public Class<C> getConnectionInterface() {
        return this.getConnectionFactory().getConnectionInterface();
    }


}
