package org.csc.phynixx.xa;

import org.csc.phynixx.connection.IPhynixxConnection;

import javax.transaction.xa.Xid;

/**
 * Created by zf4iks2 on 09.07.14.
 */
public interface IPhynixxXAResourceFactory<T extends IPhynixxConnection>{

    IPhynixxXAResource<T> getXAResource();

    Xid[] recover();

    void close();
}
