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


import org.csc.phynixx.connection.IManagedConnectionEvent;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnectionListener;
import org.csc.phynixx.connection.PhynixxManagedConnectionListenerAdapter;


/**
 * @author Christoph Schmidt-Casdorff
 */
class LocalTransactionProxy<C extends IPhynixxConnection> extends PhynixxManagedConnectionListenerAdapter<C> implements IPhynixxManagedConnectionListener<C> {

    // private TransactionManager tmMgr = null;

    private IPhynixxManagedConnection<C> connection;
    

    @Override
    public String toString() {
        return "LocalTransactionProxy [connection=" + this.connection + "]";
    }
    LocalTransactionProxy(IPhynixxManagedConnection<C> connection) {
        this.connection = connection;
        this.connection.addConnectionListener(this);
    }
	/**
     * @return indicates if the transaction has uncomnmitted transactional data
     */
    boolean hasTransactionalData() {
        return this.connection!=null && connection.hasTransactionalData();
    }

    boolean isClosed() {
        return this.getConnection() == null || this.getConnection().isClosed();
    }


    /**
     * releases the accociated connection. It can be reuse.
     * It ist not closed
     */
    public void release() 
    {
    	IPhynixxManagedConnection<C> con= this.connection;
        if (connection != null) {
            this.connection.removeConnectionListener(this);
        }
        this.connection = null;
        
        // return con; 
    }

    public void close() {
        if (connection != null) {
            IPhynixxManagedConnection<C> con=this.connection;
            this.release();
            con.close();
        }
    }

    public IPhynixxManagedConnection<C> getConnection() {
        return connection;
    }

    @Override
    public void connectionReleased(IManagedConnectionEvent<C> event) {
        event.getManagedConnection().removeConnectionListener(this);
    }

    @Override
    public void connectionFreed(IManagedConnectionEvent<C> event) {
        event.getManagedConnection().removeConnectionListener(this);
    }
}