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
     * marks a connection a release from a transactional context
     * @param managedConnection
     */
    abstract void close(PhynixxManagedConnectionGuard<C> managedConnection);


    /**
     * marks a connection a freed. This connection won't be used any more
     * @param managedConnection
     */
    abstract void free(PhynixxManagedConnectionGuard<C> managedConnection);

}
