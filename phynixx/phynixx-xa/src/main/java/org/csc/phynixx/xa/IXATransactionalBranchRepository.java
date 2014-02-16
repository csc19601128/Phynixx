package org.csc.phynixx.xa;

import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;

import javax.transaction.xa.Xid;

/**
 * Created by zf4iks2 on 10.02.14.
 */
public interface IXATransactionalBranchRepository<C extends IPhynixxConnection> extends IXATransactionalBranchDictionary<C> {


    XATransactionalBranch<C> instanciateTransactionalBranch(Xid xid, IPhynixxManagedConnection<C> physicalConnection);

    void releaseTransactionalBranch(Xid xid);

    void close();
}
