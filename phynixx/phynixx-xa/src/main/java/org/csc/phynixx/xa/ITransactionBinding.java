package org.csc.phynixx.xa;

import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;

/**
 * Created by christoph on 16.02.14.
 */
public interface ITransactionBinding<C extends IPhynixxConnection> {

    TransactionBindingType getTransactionBindingType();

    void release();

    void close();

    IPhynixxManagedConnection<C> getConnection();


}
