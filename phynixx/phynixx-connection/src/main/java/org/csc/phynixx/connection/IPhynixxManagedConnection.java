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


/**
 * a managed connection is managed by the phynixx system.
 * it takes care of integrating the connection into the phynixx XA Implementation
 * and/or provides persistent logger to store transaction data.
 * <p/>
 * It decorates the origin Connection with one or more aspects an. You can declare new decorators by {@link PhynixxManagedConnectionFactory}
 * <p/>
 * <p/>
 * this IF combines the role of a core connection and the role of a connection proxy.
 * <p/>
 * Impl. of this IF represents the access to the core connections in this FW
 *
 * h1. Thread Safeness
 *
 * When this connection is used in a pool ({@link org.csc.phynixx.connection.PooledPhynixxManagedConnectionFactory} it is strongly recommend to synchronized
 * the connection {@link #setSynchronized(boolean)}.
 *
 * When a connection is get from the pool or if it is released to it some functionality of the connection is used.
 * It is no obvious if the pools implementation of {@link org.apache.commons.pool2.impl.GenericObjectPool} is strictly thread safe.
 * @author christoph
 */
public interface IPhynixxManagedConnection<C extends IPhynixxConnection> extends IPhynixxConnection, ICloseable //, IPhynixxConnectionHandle<C>
{
    /**
     * set the thread safeness of the connection.
     * @param state
     */
    void setSynchronized(boolean state);

    /**
     *
     * @return shows if the connection is thread safe
     */
    boolean isSynchronized();

    /**
     * @return Id unique for the scope of the factory
     */
    long getManagedConnectionId();

    /**
     * @return the managed Core Connection
     */
    C getCoreConnection();

    /**
     *
     * @return shows if the connection has transactional data
     */
    boolean hasTransactionalData();

    /**
     * @return current connection interpreted as core connection, but still managed
     */
    C toConnection();


    /**
     * marks a connection a freed. This connection won't be used any more
     */
    void free();

    boolean hasCoreConnection();

    void recover();


    /**
     * opens a connection that may have been reset. After calling this method
     * {@link #isClosed()}==false and {@link IPhynixxConnection#reset()} is called on the physical connection.
     *
     * @throws java.lang.IllegalStateException connection has transactional data
     */
    void reopen();

    void addConnectionListener(IPhynixxManagedConnectionListener<C> listener);

    void removeConnectionListener(IPhynixxManagedConnectionListener<C> listener);

}
