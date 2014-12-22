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
 * @param <O>
 * @author Christoph Schmidt-Casdorff
 */
public class ObjectImplementor<O> implements IImplementor {

    private final O bean;

    public ObjectImplementor(final O bean) {
        if (bean == null) {
            throw new IllegalArgumentException("Bean muss definiert sein");
        }
        this.bean = bean;
    }

    public O getBean() {
        return this.bean;
    }

    /**
     * prueft, ob uebergebene Klasse <code>assignable</code> von aktueller
     * Klasse ist.
     */
    public <X> X cast(final Class<X> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("Parameter 'cls' muss angegeben werden");
        }

        final Class<?> beanClass = bean.getClass();
        if (cls.isAssignableFrom(beanClass)) {
            return cls.cast(bean);
        }
        throw new ClassCastException("Klasse " + beanClass + " kann nicht nach " + cls + " gewandelt werden");
    }

    public <X> boolean isImplementationOf(final Class<X> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("Parameter 'cls' muss angegeben werden");
        }
        final Class<?> beanClass = bean.getClass();
        return cls.isAssignableFrom(beanClass);

    }

}
