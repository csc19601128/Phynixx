package org.csc.phynixx.xa.deprecated;

import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A XAConnection creted by a XAresource can join different transactions and one transaction can
 * reference different XAConnection representing the same transactional context ( == same physical connection)
 * <p/>
 * A group of associations a XIDs with XAResources has its own transactional branch. The branch
 * represents the data, processed committing or rolling back the connection.
 * <p/>
 * Created by zf4iks2 on 10.02.14.
 */
class XAResourceAssociationGroup<X extends IPhynixxConnection> implements IXAResourceAssociationGroup<X> {

    Set<XAResourceAssociation<X>> associations = new HashSet<XAResourceAssociation<X>>();

    /**
     * parent owning the physical connection factory
     */
    XAManagedConnectionManager<X> xaManagedConnectionManager;

    /**
     * associated physical connection
     */
    private IPhynixxManagedConnection<X> connection;

    XAResourceAssociationGroup(XAManagedConnectionManager<X> xaManagedConnectionManager) {
        this.xaManagedConnectionManager = xaManagedConnectionManager;
    }

    public void associate(XAResourceAssociation<X> xaResourceAssociation) {
        if (!associations.contains(xaResourceAssociation)) {
            associations.add(xaResourceAssociation);
        }
    }

    public void disassociate(XAResourceAssociation<X> xaResourceAssociation) {
        if (!associations.contains(xaResourceAssociation)) {
            associations.remove(xaResourceAssociation);
        }
    }


    @Override
    public boolean isGroupMember(XAResourceAssociation<X> xaResourceAssociation) {
        return associations.contains(xaResourceAssociation);
    }

    public Set<XAResourceAssociation<X>> getAssociations() {
        return Collections.unmodifiableSet(associations);
    }

    public IPhynixxManagedConnection<X> getConnection() {
        return connection;
    }

    /**
     * sasserts that a physical connection is available. if no connection was created a new one is created
     *
     * @return pyhsical connection
     */
    public IPhynixxManagedConnection<X> assertConnection() {
        if (connection != null) {
            return connection;
        }
        this.connection = xaManagedConnectionManager.getManagedConnectionFactory().getManagedConnection();
        return this.getConnection();

    }

}
