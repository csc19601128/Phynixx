package org.csc.phynixx.xa;

import org.csc.phynixx.connection.IPhynixxConnection;

import javax.transaction.xa.XAResource;

/**
 * Created by zf4iks2 on 10.02.14.
 */
public interface IPhynixxXAResource<C extends IPhynixxConnection> extends XAResource {

    IPhynixxXAConnection<C> getXAConnection();
}
