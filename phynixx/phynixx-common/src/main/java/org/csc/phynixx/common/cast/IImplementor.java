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
 * Instanzen, welche mehere Interface implementieren, welche nicht ale an der Instanz sichtbar sind,
 * bieten mit Hilfe dieses IF die Moeglichkeit, zu einen dieser 'versteckten' Interfaces zu wechselen
 *
 * @author Christoph Schmidt-Casdorff
 */
public interface IImplementor {

    /**
     * Prueft, ob cast in die gewuenschte Zielklasse moeglich ist.
     * <p/>
     * Ist <code>cls==null</code>, so wird <code>false</code> geliefert.
     *
     * @param <X> expected (Super)Type of the checked object
     * @param cls target class / interface
     * @return true if the current class can be safely casted to X
     */
    <X> boolean isImplementationOf(Class<X> cls);

    /**
     * Fuehrt den cast nach <code>Class<X></code>
     *
     * @param <X>
     * @param cls Zielklasse
     * @return Objekt, welche das gewuenschte Klasse implementiert. Es wird kein Zusammenhang zwischen Ausgangsobjekt und Resultat vorausgesetzt (wie .z.B. sub/super, implements, instanceof etc)
     * @throws ClassCastException
     */
    <X> X cast(Class<X> cls);
}
