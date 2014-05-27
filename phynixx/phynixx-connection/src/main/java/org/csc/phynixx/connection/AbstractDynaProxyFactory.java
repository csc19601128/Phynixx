package org.csc.phynixx.connection;

/*
 * #%L
 * phynixx-connection
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


import java.lang.reflect.Method;

class AbstractDynaProxyFactory {

    private Class[] supportedInterfaces = null;
    private Class[] requiredInterfaces = null;
    private Class[] optionalInterfaces = null;
    private Class[] implementedInterfaces = null;


    /**
     * @param supportedInterfaces
     * @param requiredInterfaces
     * @param optionalInterfaces
     * @param synchronize
     */
    protected AbstractDynaProxyFactory(Class[] supportedInterfaces, Class[] requiredInterfaces, Class[] optionalInterfaces, boolean synchronize) {
        if (supportedInterfaces == null || supportedInterfaces.length == 0) {
            throw new IllegalArgumentException("supportedInterfaces are missing");
        }
        this.supportedInterfaces = supportedInterfaces;
        this.requiredInterfaces = requiredInterfaces;
        this.implementedInterfaces = supportedInterfaces;
        this.optionalInterfaces = optionalInterfaces;
        if (requiredInterfaces == null) {
            return;
        }
        for (int i = 0; i < requiredInterfaces.length; i++) {
            this.implementedInterfaces = this.addRequiredInterface(this.implementedInterfaces, requiredInterfaces[i]);
        }

    }


    private Class[] addRequiredInterface(Class[] implementedInterfaces, Class requiredInterface) {
        for (int i = 0; i < implementedInterfaces.length; i++) {
            if (implementedInterfaces[i].isInterface() &&
                    requiredInterface.isAssignableFrom(implementedInterfaces[i])) {
                return implementedInterfaces;
            }
        }

        // if not found extend the array of supported interfaces
        Class[] xImplementedInterfaces = new Class[implementedInterfaces.length + 1];
        xImplementedInterfaces[0] = requiredInterface;
        for (int i = 0; i < implementedInterfaces.length; i++) {
            xImplementedInterfaces[i + 1] = implementedInterfaces[i];
        }
        return xImplementedInterfaces;

    }


    protected Class[] getSupportedInterfaces() {
        return supportedInterfaces;
    }


    protected Class[] getImplementedInterfaces() {
        return implementedInterfaces;
    }

    protected Class[] getRequiredInterfaces() {
        return requiredInterfaces;
    }


    public Class[] getOptionalInterfaces() {
        return optionalInterfaces;
    }

    private boolean declaredBy(Method method, Class[] interfaces) {
        Class declaringClass = method.getDeclaringClass();
        for (int i = 0; i < interfaces.length; i++) {
            if (declaringClass.equals(interfaces[i])) {
                return true;
            }
        }
        return false;
    }

    protected boolean declaredBySupportedInterface(Method method) {
        return declaredBy(method, this.getSupportedInterfaces());
    }

    protected boolean declaredBySystemInterface(Method method) {
        return declaredBy(method, this.getRequiredInterfaces()) || declaredBy(method, this.getOptionalInterfaces());
    }

}
