package org.csc.phynixx.connection;

/**
 * Created by christoph on 15.02.14.
 */
public class AutoCommitDecorator<C extends IPhynixxConnection> implements IPhynixxConnectionProxyDecorator<C> {

    @Override
    public IPhynixxManagedConnection<C> decorate(IPhynixxManagedConnection<C> connectionProxy) {
        connectionProxy.addConnectionListener(new AutoCommitListener<C>());
        return connectionProxy;
    }
}
