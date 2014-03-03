package org.csc.phynixx.connection;

/**
 * Created by christoph on 03.03.14.
 */
 class ManagedPhynixxConnectionCommitEvent<C extends IPhynixxConnection> extends ManagedPhynixxConnectionEvent<C> implements IManagedConnectionCommitEvent<C> {

    private boolean onePhaseCommit= false;
    ManagedPhynixxConnectionCommitEvent(IPhynixxManagedConnection<C> source, boolean onePhaseCommit) {
        super(source);
        this.onePhaseCommit=onePhaseCommit;
    }

    ManagedPhynixxConnectionCommitEvent(IPhynixxManagedConnection<C> source, Exception exception, boolean onePhaseCommit) {
        super(source, exception);

        this.onePhaseCommit=onePhaseCommit;
    }


    @Override
    public boolean isOnePhaseCommit() {
        return onePhaseCommit;
    }
}
