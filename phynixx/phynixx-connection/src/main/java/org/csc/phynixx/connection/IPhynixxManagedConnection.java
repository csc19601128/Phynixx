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
 * @author christoph
 */
public interface IPhynixxManagedConnection<C extends IPhynixxConnection> extends IPhynixxConnection //, IPhynixxConnectionHandle<C>
{


    /**
     * @return Id unique for the scope of the factory
     */
    long getManagedConnectionId();

    /**
     * @return the managed Core Connection
     */
    C getCoreConnection();

    /**
     * @return current connection interpreted as core connection, but still managed
     */
    C toConnection();

    void recover();

    void addConnectionListener(IPhynixxManagedConnectionListener<C> listener);

    void removeConnectionListener(IPhynixxManagedConnectionListener<C> listener);

    void fireConnectionErrorOccurred(Exception exception);

    void fireConnectionDereferenced();

    void fireConnectionReferenced();
}
