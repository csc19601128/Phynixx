package org.csc.phynixx.tutorial;

import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.*;

/**
 * Created by christoph on 09.03.14.
 */
public class DumpManagedConnectionListener<C extends IPhynixxConnection> implements IPhynixxManagedConnectionListener<C>, IPhynixxConnectionProxyDecorator<C> {

    private static final IPhynixxLogger LOG= PhynixxLogManager.getLogger(DumpManagedConnectionListener.class);

    @Override
    public void connectionReset(IManagedConnectionEvent<C> event) {
        LOG.info("connectionReset "+ event.getManagedConnection());
    }

    @Override
    public void connectionReleased(IManagedConnectionEvent<C> event) {

        LOG.info("connectionReleased "+ event.getManagedConnection());

    }

    @Override
    public void connectionFreed(IManagedConnectionEvent<C> event) {

        LOG.info("connectionFreed "+ event.getManagedConnection());
    }

    @Override
    public void connectionErrorOccurred(IManagedConnectionEvent<C> event) {

        LOG.info("connectionErrorOccurred "+ event.getManagedConnection());

    }

    @Override
    public void connectionRequiresTransaction(IManagedConnectionEvent<C> event) {

        LOG.info("connectionRequiresTransaction "+ event.getManagedConnection());
    }

    @Override
    public void connectionRequiresTransactionExecuted(IManagedConnectionEvent<C> event) {
        LOG.info("connectionRequiresTransactionExecuted "+ event.getManagedConnection());

    }

    @Override
    public void connectionRolledback(IManagedConnectionEvent<C> event) {
        LOG.info("connectionRolledback "+ event.getManagedConnection());

    }

    @Override
    public void connectionCommitting(IManagedConnectionCommitEvent<C> event) {
        LOG.info("connectionCommitting "+ event.getManagedConnection());

    }

    @Override
    public void connectionCommitted(IManagedConnectionCommitEvent<C> event) {
        LOG.info(""+ event.getManagedConnection());

    }

    @Override
    public void connectionPreparing(IManagedConnectionEvent<C> event) {
        LOG.info("connectionPreparing "+ event.getManagedConnection());
    }

    @Override
    public void connectionPrepared(IManagedConnectionEvent<C> event) {
        LOG.info("connectionPrepared "+ event.getManagedConnection());
    }

    @Override
    public void connectionRecovering(IManagedConnectionEvent<C> event) {
        LOG.info("connectionRecovering "+ event.getManagedConnection());

    }

    @Override
    public void connectionRecovered(IManagedConnectionEvent<C> event) {
        LOG.info("connectionRecovered "+ event.getManagedConnection());
    }

    @Override
    public void connectionRollingBack(IManagedConnectionEvent<C> event) {
        LOG.info(""+ event.getManagedConnection());

    }

    @Override
    public IPhynixxManagedConnection<C> decorate(IPhynixxManagedConnection<C> managedConnection) {
        managedConnection.addConnectionListener(this);
        return managedConnection;
    }
}
