package org.csc.phynixx.xa;

import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnectionFactory;

import javax.transaction.xa.Xid;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zf4iks2 on 10.02.14.
 */
class XATransactionalBranchRepository<C extends IPhynixxConnection> implements IXATransactionalBranchRepository<C> {

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(XATransactionalBranchRepository.class);

    private IPhynixxManagedConnectionFactory<C> managedConnectionFactory;

    private Map<Xid, XATransactionalBranch<C>> branches = new HashMap<Xid, XATransactionalBranch<C>>();

    XATransactionalBranchRepository(IPhynixxManagedConnectionFactory<C> managedConnectionFactory) {
        this.managedConnectionFactory = managedConnectionFactory;
    }

    @Override
    public void instanciateTransactionalBranch(Xid xid) {
        if (!branches.containsKey(xid)) {
            IPhynixxManagedConnection<C> connection = this.managedConnectionFactory.getManagedConnection();
            XATransactionalBranch<C> branch = new XATransactionalBranch<C>(xid, connection);
            branches.put(xid, branch);
        }
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