package org.csc.phynixx.xa;

import org.csc.phynixx.connection.IPhynixxConnection;

import javax.transaction.xa.Xid;

/**
 * Created by zf4iks2 on 10.02.14.
 */
public interface IXATransactionalBranchRepository<C extends IPhynixxConnection> extends IXATransactionalBranchDictionary<C> {


    void instanciateTransactionalBranch(Xid xid);

    void releaseTransactionalBranch(Xid xid);

    void close();
}
