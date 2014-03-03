package org.csc.phynixx.connection;

/**
 * Created by christoph on 03.03.14.
 */
public interface IManagedConnectionCommitEvent<C extends IPhynixxConnection> extends IManagedConnectionEvent<C> {

    boolean isOnePhaseCommit();
}
