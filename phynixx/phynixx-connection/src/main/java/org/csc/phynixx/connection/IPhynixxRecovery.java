package org.csc.phynixx.connection;

/**
 * Created by zf4iks2 on 26.02.14.
 */
public interface IPhynixxRecovery<C extends IPhynixxConnection> {

    /**
     * Created by christoph on 02.02.14.
     */
    public static interface IRecoveredManagedConnection<C> {
        public void managedConnectionRecovered(C con);
    }


    /**
     * recovers all connection that have not completed transactions.
     * The recovered connections are handed to the callback after recovering.
     * All connections are closed after returning from this method
     *
     * @param recoveredManagedConnectionCallback callback accepting the recovered connections
     */
    void recover(IPhynixxRecovery.IRecoveredManagedConnection<C> recoveredManagedConnectionCallback);

}
