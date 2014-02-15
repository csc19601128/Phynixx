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
 * Implementierung des von {@link IImplementor} fuer eine Delegatinon.
 *
 * @param <Y>
 * @author zf4iks2
 */
public class ImplementorWrapper<Y extends IImplementor> implements IImplementor {

    private Y editable = null;

    protected ImplementorWrapper(final Y editable) {
        super();
        this.editable = editable;
    }

    protected Y getWrapped() {
        return this.editable;
    }

    /**
     * prueft, ob uebergebene Klasse <code>assignable</code> von aktueller
     * Klasse ist.
     */
    public <X> X cast(final Class<X> iface) {
        if (iface == null) {
            throw new IllegalArgumentException("Parameter 'cls' muss angegeben werden");
        }

        final Class<?> clazz = this.getClass();
        if (iface.isAssignableFrom(clazz)) {
            return iface.cast(this);
        } else {
            if (this.editable == null) {
                throw new IllegalStateException("Delegate ist nicht definiert");
            }
            return this.editable.cast(iface);
        }
    }

    public <X> boolean isImplementationOf(final Class<X> cls) {
        if (cls == null) {
            throw new IllegalArgumentException(
                    "Parameter 'cls' muss angegeben werden");
        }
        if (cls.isAssignableFrom(this.getClass())) {
            return true;
        } else {
            if (this.editable == null) {
                throw new IllegalStateException("Delegate ist nicht definiert");
            }
            return this.editable.isImplementationOf(cls);
        }
    }

}
