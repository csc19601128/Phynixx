package org.csc.phynixx.common.utils;

/*
 * #%L
 * phynixx-common
 * %%
 * Copyright (C) 2014 Christoph Schmidt-Casdorff
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.*;

/**
 * Created by christoph on 09.02.14.
 */
public class UnorderedMany2ManyAssociation<X, Y> {

    final Map<X, Set<Y>> mapA = new HashMap<X, Set<Y>>();

    final Map<Y, Set<X>> mapB = new HashMap<Y, Set<X>>();


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
