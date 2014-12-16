package org.csc.phynixx.xa;

import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxConnectionFactory;

import javax.transaction.xa.Xid;

/**
 * Created by Christoph Schmidt-Casdorff on 09.07.14.
 */
public interface IPhynixxXAResourceFactory<T extends IPhynixxConnection> extends IPhynixxConnectionFactory<T>{

    IPhynixxXAResource<T> getXAResource();

    Xid[] recover();

    void close();
}
