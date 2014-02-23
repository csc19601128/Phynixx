package org.csc.phynixx.connection;

/**
 * Created by christoph on 22.02.14.
 */
public class UnpooledConnectionStrategy<C extends IPhynixxConnection> extends CloseStrategy<C> {
    @Override
    public void close(PhynixxManagedConnectionGuard<C> managedConnection) {
        managedConnection.doClose();
    }


    @Override
    void free(PhynixxManagedConnectionGuard<C> managedConnection) {
        this.close(managedConnection);
    }
}
