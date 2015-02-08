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


public class ImplementorUtils {

    /**
     * 
     * Casts an object to the class or interface represented
     * by this <tt>Class</tt> object.
     *
     * @param obj the object to be cast
     * @param targetClass
     * @return the object after casting, or null if obj is null
     *
     * @throws IllegalArgumentException object oder Zielklasse sind undefiniert
     * @throws ClassCastException       cast konnte nicht durch gefuehrt werden
     * 
     * 
     * 
     */
    public static <T> T cast(final Object obj, final Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Zielklasse, in welche gecasted werden soll, ist anzugeben.");
        }
        if (obj == null) {
            return null;
        }
        return new ObjectImplementor<Object>(obj).cast(targetClass);
    }

    /**
     * prueft, ob eine Objekt in den Zieltyp <code>targetClass</code> zu casten
     * ist.
     * <p/>
     * Wird <code>null</code> uebergeben, so wird <code>false</code> geliefert.
     *
     * @param obj
     * @param targetClass
     * @return
     * @throws IllegalArgumentException object oder Zielklasse sind undefiniert
     * @throws ClassCastException       cast konnte nicht durch gefuehrt werden
     */
    public static <T> boolean isImplementationOf(final Object obj, final Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Zielklasse, in welche gecasted werden soll, ist anzugeben.");
        }
        if (obj == null) {
            return false;
        }
        return new ObjectImplementor<Object>(obj).isImplementationOf(targetClass);
    }

}
