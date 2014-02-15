package org.csc.phynixx.common.utils;

import javax.transaction.xa.Xid;
import java.util.*;

/**
 * Associations betweeen Instances of X and Y can be grouped. To join a group  you have to give an assoc you want to share the group. You can assign a group Value to a group
 * Created by christoph on 09.02.14.
 */
public class GroupingAssociation<X, Y, A> {


    private static class Association<U, V> {
        private Xid xid;
        private }

    private static class Group {


    }

    Map<X, Set<Y>> mapA = new HashMap<X, Set<Y>>();

    Map<Y, Set<X>> mapB = new HashMap<Y, Set<X>>();


    public void associate(X x, Y y) {
        assertSet(mapA, x).add(y);
        assertSet(mapB, y).add(x);
    }


    public Set<Y> getX(X x) {
        return getEndpoints(x, mapA);
    }

    public Set<X> getY(Y y) {
        return getEndpoints(y, mapB);
    }


    public void removeX(X x) {
        removeDependents(x, mapA, mapB);
        mapA.remove(x);
    }


    public void removeY(Y y) {
        removeDependents(y, mapB, mapA);
        mapB.remove(y);
    }


    public void disassociate(X x, Y y) {
        removeDependent(x, y, mapA);
        removeDependent(y, x, mapB);
    }


    static <K, V> Set<V> getEndpoints(K k, Map<K, Set<V>> map) {
        Set<V> value = map.get(k);
        if (value == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(value);
    }


    static <K, V> void removeDependents(K key, Map<K, Set<V>> map, Map<V, Set<K>> inverseMap) {
        Set<V> value = map.get(key);
        if (value != null && !value.isEmpty()) {
            for (V v : value) {
                Set<K> keys = inverseMap.get(v);
                if (keys != null && !keys.isEmpty()) {
                    keys.remove(key);
                }
            }
        }
    }

    static <K, V> void removeDependent(K k, V v, Map<K, Set<V>> map) {
        Set<V> value = map.get(k);
        if (value != null && !value.isEmpty()) {
            value.remove(v);
        }
    }


    static <K, V> Set<V> assertSet(Map<K, Set<V>> map, K key) {
        Set<V> value = map.get(key);
        if (value == null) {
            value = new HashSet<V>();
            map.put(key, value);
        }
        return value;
    }


}
