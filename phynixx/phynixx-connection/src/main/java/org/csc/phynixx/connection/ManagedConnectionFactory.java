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
import org.csc.phynixx.connection.loggersystem.ILoggerSystemStrategy;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;

import java.util.List;

/**
 * @param <C>
 */

public class ManagedConnectionFactory<C extends IPhynixxConnection> extends ManagedConnectionListenerAdapter<C> implements IPhynixxConnectionFactory<C>, IManagedConnectionListener<C> {

    private IPhynixxLogger logger = PhynixxLogManager.getLogger(this.getClass());

    private IPhynixxConnectionFactory<C> connectionFactory = null;

    private DynaManagedConnectionProxyFactory<C> connectionProxyFactory = null;
    private ILoggerSystemStrategy<C> loggerSystemStrategy = new Dev0Strategy();
    private IPhynixxConnectionProxyDecorator<C> connectionProxyDecorator = null;


    public ManagedConnectionFactory() {
    }

    public ManagedConnectionFactory(IPhynixxConnectionFactory<C> connectionFactory) {
        this.connectionFactory = connectionFactory;
            this.connectionProxyFactory =
                    new DynaManagedConnectionProxyFactory<C>(new Class[]{connectionFactory.connectionInterface()});

    }


    public void setConnectionFactory(IPhynixxConnectionFactory<C> connectionFactory) {
        this.connectionFactory = connectionFactory;
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

    public ILoggerSystemStrategy<C> getLoggerSystemStrategy() {
        return loggerSystemStrategy;
    }

    public void setLoggerSystemStrategy(ILoggerSystemStrategy<C> loggerSystemStrategy) {
        this.loggerSystemStrategy = loggerSystemStrategy;
    }

    public C getConnection() {
        return this.instantiateConnection();
    }


    protected C instantiateConnection() {
        try {
            IManagedConnectionProxy<C> proxy;
            try {
                C connection = ManagedConnectionFactory.this.getConnectionFactory().getConnection();

                /**
                 * returns empty Proxy
                 */
                proxy = ManagedConnectionFactory.this.connectionProxyFactory.getConnectionProxy();

                /**
                 * sets the decorated connection
                 */
                proxy.setConnection(connection);
                proxy.addConnectionListener(ManagedConnectionFactory.this);

                if (ManagedConnectionFactory.this.loggerSystemStrategy != null) {
                    proxy = ManagedConnectionFactory.this.loggerSystemStrategy.decorate(proxy);
                }

                if (ManagedConnectionFactory.this.connectionProxyDecorator != null) {
                    proxy = ManagedConnectionFactory.this.connectionProxyDecorator.decorate(proxy);
                }

                // Instantiate the connection
                proxy.open();
            } catch (ClassCastException e) {
                e.printStackTrace();
                throw new DelegatedRuntimeException(e);
            }

            return ImplementorUtils.cast(proxy, connectionFactory.connectionInterface());

        } catch (Throwable e) {
            throw new DelegatedRuntimeException("Instantiating new pooled Proxy", e);
        }
    }


    public Class<C> connectionInterface() {
        return this.getConnectionFactory().connectionInterface();
    }

    public static interface IRecoveredManagedConnection<C> {
        public void managedConnectionRecovered(C con);
    }

    public void recover(IRecoveredManagedConnection<C> recoveredManagedConnectionCallback) {

        // get all recoverable transaction data
        List<IXADataRecorder> messageLoggers = this.loggerSystemStrategy.readIncompleteTransactions();
        C con = null;
        for (int i = 0; i < messageLoggers.size(); i++) {
            try {
                IXADataRecorder msgLogger = messageLoggers.get(i);
                con = this.getConnection();
                con.setXADataRecorder(msgLogger);
                ((IManagedConnectionProxy<C>) con).recover();

                if (recoveredManagedConnectionCallback != null) {
                    recoveredManagedConnectionCallback.managedConnectionRecovered(con);
                }
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        }

    }

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
        IManagedConnectionProxy proxy = event.getConnectionProxy();
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
