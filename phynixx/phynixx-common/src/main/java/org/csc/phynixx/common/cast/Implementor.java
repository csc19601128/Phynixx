package org.csc.phynixx.common.cast;

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


/**
 * Als Superklasse casted diese Klasse in die Subclass gegebene durch
 * <code>this.getClass()</code>
 *
 * @author Christoph Schmidt-Casdorff
 */
public class Implementor implements IImplementor {

    /**
     * prueft, ob uebergebene Klasse <code>assignable</code> von aktueller Klasse ist.
     */
    public <X> X cast(final Class<X> iface) {
        if (iface == null) {
            throw new IllegalArgumentException("Parameter 'cls' muss angegeben werden");
        }
        if (iface.isAssignableFrom(this.getClass())) {
            return iface.cast(this);
        }
        throw new ClassCastException("Klasse " + this.getClass() + " kann nicht nach " + iface + " gewandelt werden");
    }

    public <X> boolean isImplementationOf(final Class<X> cls) {
        return (cls != null) && cls.isAssignableFrom(this.getClass());
    }

}
