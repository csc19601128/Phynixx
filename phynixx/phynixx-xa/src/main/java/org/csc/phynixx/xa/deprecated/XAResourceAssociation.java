package org.csc.phynixx.xa.deprecated;

import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.xa.PhynixxXAResource;

import javax.transaction.xa.Xid;

/**
 * Created by zf4iks2 on 10.02.14.
 */
final class XAResourceAssociation<X extends IPhynixxConnection> {
    private final Xid xid;
    private final PhynixxXAResource<X> xaResource;

    XAResourceAssociation(Xid xid, PhynixxXAResource<X> xaResource) {
        this.xid = xid;
        this.xaResource = xaResource;
    }

    final Xid getXid() {
        return xid;
    }

    final PhynixxXAResource<X> getXaResource() {
        return xaResource;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XAResourceAssociation that = (XAResourceAssociation) o;

        if (!xaResource.equals(that.xaResource)) return false;
        if (!xid.equals(that.xid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = xid.hashCode();
        result = 31 * result + xaResource.hashCode();
        return result;
    }
}
