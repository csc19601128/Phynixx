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


import org.csc.phynixx.cast.ImplementorUtils;
import org.csc.phynixx.connection.loggersystem.Dev0Strategy;
import org.csc.phynixx.connection.loggersystem.IPhynixxLoggerSystemStrategy;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;

import java.util.List;

/**
 * managedConnection are proxies for connections created by a {@link IPhynixxConnectionFactory}. The proxy adds serveral capabilities to the (core-)connection
 * <pre>
 *   1.)
 *
 *
 * </pre>
 *
 * @param <C>
 */

public class PhynixxPhynixxManagedConnectionFactory<C extends IPhynixxConnection> extends PhynixxManagedConnectionListenerAdapter<C> implements IPhynixxConnectionFactory<C>, IPhynixxManagedConnectionFactory<C>, IPhynixxManagedConnectionListener<C> {

    private IPhynixxLogger logger = PhynixxLogManager.getLogger(this.getClass());

    private IPhynixxConnectionFactory<C> connectionFactory = null;

    private DynaPhynixxManagedConnectionFactory<C> connectionProxyFactory = null;
    private IPhynixxLoggerSystemStrategy<C> loggerSystemStrategy = new Dev0Strategy();
    private IPhynixxConnectionProxyDecorator<C> connectionProxyDecorator = null;


    public PhynixxPhynixxManagedConnectionFactory() {
    }

    public PhynixxPhynixxManagedConnectionFactory(IPhynixxConnectionFactory<C> connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.connectionProxyFactory =
                new DynaPhynixxManagedConnectionFactory<C>(new Class[]{connectionFactory.getConnectionInterface()});

    }


    public void setConnectionFactory(IPhynixxConnectionFactory<C> connectionFactory) {

        this.connectionFactory = connectionFactory;
        this.connectionProxyFactory =
                new DynaPhynixxManagedConnectionFactory<C>(new Class[]{connectionFactory.getConnectionInterface()});
    }


    public IPhynixxConnectionFactory<C> getConnectionFactory() {
        return connectionFactory;
    }

    public IPhynixxConnectionProxyDecorator<C> getConnectionProxyDecorator() {
        return connectionProxyDecorator;
    }

    public void setConnectionProxyDecorator(
            IPhynixxConnectionProxyDecorator<C> connectionProxyDecorator) {
        this.connectionProxyDecorator = connectionProxyDecorator;
    }

    public IPhynixxLoggerSystemStrategy<C> getLoggerSystemStrategy() {
        return loggerSystemStrategy;
    }

    public void setLoggerSystemStrategy(IPhynixxLoggerSystemStrategy<C> loggerSystemStrategy) {
        this.loggerSystemStrategy = loggerSystemStrategy;
    }

    @Override
    public C getConnection() {
        return ImplementorUtils.cast(getManagedConnection(), getConnectionInterface());
    }

    @Override
    public IPhynixxManagedConnection<C> getManagedConnection() {
        return this.instantiateConnection();
    }


    protected IPhynixxManagedConnection<C> instantiateConnection() {
        try {
            IPhynixxManagedConnection<C> proxy;
            try {

                // instanciate a fresh core connection
                C connection = PhynixxPhynixxManagedConnectionFactory.this.getConnectionFactory().getConnection();

                /**
                 * returns empty Proxy
                 */
                proxy = PhynixxPhynixxManagedConnectionFactory.this.connectionProxyFactory.getConnectionProxy();

                /**
                 * sets the decorated connection
                 */
                proxy.setConnection(connection);
                proxy.addConnectionListener(PhynixxPhynixxManagedConnectionFactory.this);

                if (PhynixxPhynixxManagedConnectionFactory.this.loggerSystemStrategy != null) {
                    proxy = PhynixxPhynixxManagedConnectionFactory.this.loggerSystemStrategy.decorate(proxy);
                }

                if (PhynixxPhynixxManagedConnectionFactory.this.connectionProxyDecorator != null) {
                    proxy = PhynixxPhynixxManagedConnectionFactory.this.connectionProxyDecorator.decorate(proxy);
                }

                // Instantiate the connection
                proxy.open();
            } catch (ClassCastException e) {
                e.printStackTrace();
                throw new DelegatedRuntimeException(e);
            }

            return proxy;

        } catch (Throwable e) {
            throw new DelegatedRuntimeException("Instantiating new pooled Proxy", e);
        }
    }


    public Class<C> getConnectionInterface() {
        return this.getConnectionFactory().getConnectionInterface();
    }

    @Override
    public void recover(IRecoveredManagedConnection<C> recoveredManagedConnectionCallback) {

        // get all recoverable transaction data
        List<IXADataRecorder> messageLoggers = this.loggerSystemStrategy.readIncompleteTransactions();
        IPhynixxManagedConnection<C> con = null;
        for (int i = 0; i < messageLoggers.size(); i++) {
            try {
                IXADataRecorder msgLogger = messageLoggers.get(i);
                con = this.getManagedConnection();
                if (!ImplementorUtils.isImplementationOf(con, IXADataRecorderAware.class)) {
                    throw new IllegalStateException("Connection does not support " + IXADataRecorderAware.class + " and can't be recovered");
                } else {
                    (ImplementorUtils.cast(con, IXADataRecorderAware.class)).setXADataRecorder(msgLogger);
                }

                con.recover();

                if (recoveredManagedConnectionCallback != null) {
                    recoveredManagedConnectionCallback.managedConnectionRecovered(con.getConnection());
                }
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        }

    }

    @Override
    public void close() {
        try {
            if (this.loggerSystemStrategy != null) {
                this.loggerSystemStrategy.close();
            }
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

    }

    /**
     * the connection is released to the pool
     */
    public void connectionClosed(IManagedConnectionProxyEvent<C> event) {
        IPhynixxManagedConnection proxy = event.getConnectionProxy();
        if (proxy.getConnection() == null) {
            return;
        }
        if (proxy.getConnection() != null) {
            proxy.getConnection().close();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Connection " + proxy + " closed");
        }
    }

    public void connectionDereferenced(IManagedConnectionProxyEvent<C> event) {
        throw new IllegalStateException("Connection is bound to a proxy and can't be released");
    }


}