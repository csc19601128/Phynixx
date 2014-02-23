package org.csc.phynixx.connection;

/**
 *
 * maps the call of {@link IPhynixxManagedConnection#close()} to an implementation
 *
 * Its no interface to keep this strategy hidden
 * Created by christoph on 22.02.14.
 */
abstract class CloseStrategy<C extends IPhynixxConnection> {

    /**
     * marks a connection a close from a transactional context. The implematations decides if it can be re-used or if it is destroyes
     * @param managedConnection
     */
    abstract void close(PhynixxManagedConnectionGuard<C> managedConnection);


}
