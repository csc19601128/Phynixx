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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.csc.phynixx.common.cast.ImplementorUtils;
import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.loggersystem.Dev0Strategy;
import org.csc.phynixx.connection.loggersystem.IPhynixxLoggerSystemStrategy;

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

public class PhynixxManagedConnectionFactory<C extends IPhynixxConnection> extends PhynixxManagedConnectionListenerAdapter<C> implements IPhynixxManagedConnectionFactory<C>, IPhynixxManagedConnectionListener<C> {

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(PhynixxManagedConnectionFactory.class);

    private IPhynixxConnectionFactory<C> connectionFactory = null;

    private DynaPhynixxManagedConnectionFactory<C> managedConnectionFactory = null;
    private IPhynixxLoggerSystemStrategy<C> loggerSystemStrategy = new Dev0Strategy<C>();

    private List<IPhynixxConnectionProxyDecorator<C>> managedConnectionDecorators = new ArrayList<IPhynixxConnectionProxyDecorator<C>>();


    private boolean autocommitAware= true;

    private boolean synchronizeConnection= true;

    private final AutoCommitDecorator<C> autcommitDecorator=new AutoCommitDecorator<C>();

    private CloseStrategy<C> closeStrategy;

    public PhynixxManagedConnectionFactory() {
    }

    public PhynixxManagedConnectionFactory(IPhynixxConnectionFactory<C> connectionFactory) {
        this.setConnectionFactory(connectionFactory);
        this.setCloseStrategy(new UnpooledConnectionCloseStrategy<C>());
    }

    @Override
    public boolean isAutocommitAware() {
        return autocommitAware;
    }

    @Override
    public void setAutocommitAware(boolean autocommitAware) {
        this.autocommitAware = autocommitAware;
    }

    @Override
    public boolean isSynchronizeConnection() {
        return synchronizeConnection;
    }

    @Override
    public void setSynchronizeConnection(boolean synchronizeConnection) {
        this.synchronizeConnection = synchronizeConnection;
    }


    void setCloseStrategy(CloseStrategy<C> closeStrategy) {
        this.closeStrategy= closeStrategy;
    }

    public void setConnectionFactory(IPhynixxConnectionFactory<C> connectionFactory) {

        if(this.connectionFactory!=null) {
            this.connectionFactory.close();
            this.connectionFactory=null;
        }

        /// factory for physical connection
        this.connectionFactory = connectionFactory;
    }

    private void createManagedConnectionFactory() {

        if(this.getConnectionFactory()==null) {
            throw new IllegalStateException("Connection Factory has to be defined");
        }

        if(this.closeStrategy==null) {
            throw new IllegalStateException("CloseStrategy has to be defined");
        }

        this.managedConnectionFactory =
                new DynaPhynixxManagedConnectionFactory<C>(this.connectionFactory.getConnectionInterface(), this.closeStrategy, this.isSynchronizeConnection());
    }


    public IPhynixxConnectionFactory<C> getConnectionFactory() {
        return connectionFactory;
    }

    public List<IPhynixxConnectionProxyDecorator<C>> getManagedConnectionDecorators() {
        return Collections.unmodifiableList(managedConnectionDecorators);
    }

    public void addManagedConnectionDecorator(
            IPhynixxConnectionProxyDecorator<C> connectionProxyDecorator) {
        this.managedConnectionDecorators.add(connectionProxyDecorator);
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
            IPhynixxManagedConnection<C> managedConnection;
            try {

                // instanciate a fresh core connection
                C connection = PhynixxManagedConnectionFactory.this.getConnectionFactory().getConnection();

                /**
                 * returns empty Proxy
                 */
                managedConnection = getManagedConnectionFactory().getManagedConnection(connection);

                /**
                 * sets the decorated connection
                 */
                managedConnection.addConnectionListener(PhynixxManagedConnectionFactory.this);

                managedConnection.setSynchronized(this.isSynchronizeConnection());

                if (PhynixxManagedConnectionFactory.this.loggerSystemStrategy != null) {
                    managedConnection = PhynixxManagedConnectionFactory.this.loggerSystemStrategy.decorate(managedConnection);
                }

                for (IPhynixxConnectionProxyDecorator<C> decorators : this.managedConnectionDecorators) {
                    managedConnection = decorators.decorate(managedConnection);
                }


                if(this.isAutocommitAware()) {
                    this.autcommitDecorator.decorate(managedConnection);
                }

                // Instantiate the connection
                managedConnection.reset();
            } catch (ClassCastException e) {
                e.printStackTrace();
                throw new DelegatedRuntimeException(e);
            }

            return managedConnection;

        } catch (Throwable e) {
            throw new DelegatedRuntimeException("Instantiating new pooled Proxy", e);
        }
    }

    private DynaPhynixxManagedConnectionFactory<C> getManagedConnectionFactory() {

        if(this.managedConnectionFactory==null) {
            this.createManagedConnectionFactory();
        }
        return PhynixxManagedConnectionFactory.this.managedConnectionFactory;
    }


    @Override
    public Class<C> getConnectionInterface() {
        return this.getConnectionFactory().getConnectionInterface();
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
    @Override
    public void connectionReleased(IManagedConnectionEvent<C> event) {
        IPhynixxManagedConnection<C> proxy = event.getManagedConnection();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Proxy " + proxy + " released");
        }

    }

    @Override
    public void connectionFreed(IManagedConnectionEvent<C> event) {
        IPhynixxManagedConnection<C> proxy = event.getManagedConnection();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Proxy " + proxy + " set free");
        }
    }
}
