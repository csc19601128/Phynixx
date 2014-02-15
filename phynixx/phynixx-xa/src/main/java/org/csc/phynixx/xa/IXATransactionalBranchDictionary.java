package org.csc.phynixx.xa;

import org.csc.phynixx.connection.IPhynixxConnection;

import javax.transaction.xa.Xid;

/**
 * Created by zf4iks2 on 10.02.14.
 */
public interface IXATransactionalBranchDictionary<C extends IPhynixxConnection> {

    XATransactionalBranch<C> findTransactionalBranch(Xid xid);
}
