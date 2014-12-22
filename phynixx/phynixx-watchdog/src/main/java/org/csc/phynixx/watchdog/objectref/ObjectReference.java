package org.csc.phynixx.watchdog.objectref;

/*
 * #%L
 * phynixx-watchdog
 * %%
 * Copyright (C) 2014 csc
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


/**
 *
 * Created by christoph on 09.06.2012.
 */
public class ObjectReference<T> implements IObjectReference<T> {

    private T objectRef = null;


    public ObjectReference(T objectRef) {
        super();
        this.objectRef = objectRef;
    }


    public String getObjectDescription() {

        return objectRef == null ? "NULL" : objectRef.toString();
    }


    public boolean isWeakReference() {
        return false;
    }


    public T get() {
        return objectRef;
    }

    public boolean equals(Object obj) {
        T objRef = this.get();
        if (objRef == null) {
            return obj == null;
        }
        return objRef.equals(obj);
    }

    public int hashCode() {
        Object objRef = this.get();
        if (objRef == null) {
            return "".hashCode();
        }
        return objRef.hashCode();
    }

    public String toString() {
        Object objRef = this.get();
        if (objRef == null) {
            return "";
        }
        return objRef.toString();
    }


    public boolean isStale() {
        return false;
    }

}
