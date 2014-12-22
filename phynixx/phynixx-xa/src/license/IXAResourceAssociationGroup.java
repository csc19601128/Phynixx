package org.csc.phynixx.xa;

import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;

import java.util.Set;

/**
 * Created by zf4iks2 on 10.02.14.
 */
public interface IXAResourceAssociationGroup<C extends IPhynixxConnection> {

    IPhynixxManagedConnection<C> assertManagedConnection();

    IPhynixxManagedConnection<C> getManagedConnection();

    Set<XAResourceAssociation<C>> getXAResourceAssociations();

}
