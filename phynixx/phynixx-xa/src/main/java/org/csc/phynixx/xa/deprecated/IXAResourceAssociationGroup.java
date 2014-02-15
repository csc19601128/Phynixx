package org.csc.phynixx.xa.deprecated;

/**
 * Created by zf4iks2 on 10.02.14.
 */
public interface IXAResourceAssociationGroup<X> {
    boolean isGroupMember(XAResourceAssociation<X> xaResourceAssociation);
}
