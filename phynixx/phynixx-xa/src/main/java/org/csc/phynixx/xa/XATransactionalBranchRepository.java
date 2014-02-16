package org.csc.phynixx.xa;

import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;

import javax.transaction.xa.Xid;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zf4iks2 on 10.02.14.
 */
class XATransactionalBranchRepository<C extends IPhynixxConnection> implements IXATransactionalBranchRepository<C> {

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(XATransactionalBranchRepository.class);


    private Map<Xid, XATransactionalBranch<C>> branches = new HashMap<Xid, XATransactionalBranch<C>>();

    XATransactionalBranchRepository() {
    }

    @Override
    public XATransactionalBranch<C> instanciateTransactionalBranch(Xid xid, IPhynixxManagedConnection<C> physicalConnection) {
        XATransactionalBranch<C> branch = branches.get(xid);
        if (branch == null) {
            branch = new XATransactionalBranch<C>(xid, physicalConnection);
            branches.put(xid, branch);
        }
        return branch;


    }

    @Override
    public void releaseTransactionalBranch(Xid xid) {
        if (branches.containsKey(xid)) {
            branches.remove(xid);
        }
    }

    @Override
    public XATransactionalBranch<C> findTransactionalBranch(Xid xid) {
        return branches.get(xid);
    }


    @Override
    public void close() {
        for (XATransactionalBranch<C> branch : branches.values()) {
            branch.close();
        }
    }

}