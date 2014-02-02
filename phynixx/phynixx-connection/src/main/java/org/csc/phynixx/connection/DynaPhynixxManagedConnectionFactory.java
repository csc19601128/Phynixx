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


import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.exceptions.ExceptionUtils;
import org.csc.phynixx.generator.IDGenerator;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * provided generic proxies on base of java DynaProxies
 * <p/>
 * This proxy
 *
 * @param <C>
 */
class DynaPhynixxManagedConnectionFactory<C extends IPhynixxConnection> extends AbstractDynaProxyFactory {

    private static final IDGenerator idGenerator = new IDGenerator(1);

    public DynaPhynixxManagedConnectionFactory(Class[] supportedInterfaces, boolean synchronize) {
        super(supportedInterfaces,
                new Class[]{IPhynixxConnection.class, IPhynixxManagedConnection.class, IPhynixxConnectionHandle.class},
                new Class[]{IXADataRecorderAware.class},
                synchronize);

    }

    DynaPhynixxManagedConnectionFactory(Class[] supportedInterfaces) {
        this(supportedInterfaces, true);
    }

    IPhynixxManagedConnection<C> getConnectionProxy() {

        Long connectionId = null;
        synchronized (idGenerator) {
            connectionId = idGenerator.generateLong();
        }
        ConnectionPhynixxGuard proxy = new ConnectionPhynixxGuard(connectionId);

        IPhynixxManagedConnection<C> proxied = (IPhynixxManagedConnection<C>)
                Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                        this.getImplementedInterfaces(), proxy);
        proxy.proxiedObject = proxied;
        return proxied;
    }


    class ConnectionPhynixxGuard<C extends IPhynixxConnection> extends PhynixxManagedConnectionGuard<C> implements IPhynixxConnectionHandle<C>, InvocationHandler {


        private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());
        /**
         * As the proxy contains more information than the current implentation we have to store
         * the proxy an use it in all call backs
         */
        private IPhynixxManagedConnection<C> proxiedObject = null;

        ConnectionPhynixxGuard(long id) {
            super(id);
        }

        /**
         * if the interface method is decorated with {@link org.csc.phynixx.connection.RequiresTransaction}
         *
         * @param method
         * @return
         */
        private boolean requiresTransaction(Method method) {

            Annotation[] annotations = method.getDeclaredAnnotations();
            if (annotations == null || annotations.length == 0) {
                return false;
            }
            for (int i = 0; i < annotations.length; i++) {
                if (RequiresTransaction.class.isAssignableFrom(annotations[i].annotationType())) {
                    RequiresTransaction requiresTransactionAnnotation = (RequiresTransaction) annotations[i];
                    return requiresTransactionAnnotation.value();
                }
            }

            return false;

        }


        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object target = null;

            /**
             * methods of the IConnectionProxy are redirected to the current object
             */

            // execute
            try {
                if (DynaPhynixxManagedConnectionFactory.this.declaredBySystemInterface(method)) {
                    target = this;

                    // System.out.println("Thread " + Thread.currentThread()+" Connection IF expected to " + this+" on "+method);
                    Object obj = null;
                    if (DynaPhynixxManagedConnectionFactory.this.isSynchronize()) {
                        synchronized (this) {
                            obj = method.invoke(this, args);
                        }
                    } else {
                        obj = method.invoke(this, args);
                    }
                    return obj;
                } else if (DynaPhynixxManagedConnectionFactory.this.declaredBySupportedInterface(method)) {

                    target = this.getConnection();

                    // System.out.println("Thread " + Thread.currentThread()+" Delegated to Connection " + target+" on "+method);

                    Object obj = null;
                    if (DynaPhynixxManagedConnectionFactory.this.isSynchronize()) {
                        synchronized (this) {
                            if (this.requiresTransaction(method)) {
                                this.fireConnectionRequiresTransaction();
                            }
                            obj = method.invoke(target, args);
                        }
                    } else {
                        if (this.requiresTransaction(method)) {
                            this.fireConnectionRequiresTransaction();
                        }
                        obj = method.invoke(target, args);
                    }
                    return obj;
                } else {
                    Object obj = null;
                    if (DynaPhynixxManagedConnectionFactory.this.isSynchronize()) {
                        synchronized (this) {
                            obj = method.invoke(this, args);
                        }
                    } else {
                        obj = method.invoke(this, args);
                    }
                    return obj;
                }
            } catch (InvocationTargetException targetEx) {
                targetEx.getTargetException().printStackTrace();
                log.fatal("Error calling method " + method + " on " + target + " :: " + targetEx.getMessage() + "\n" + ExceptionUtils.getStackTrace(targetEx.getTargetException()));
                throw new DelegatedRuntimeException("Invoking " + method, targetEx.getTargetException());
            } catch (Throwable ex) {
                log.fatal("Error calling method " + method + " on " + target + " :: " + ex + "\n" + ExceptionUtils.getStackTrace(ex));
                throw new DelegatedRuntimeException("Invoking " + method, ex);
            }
        }

        protected IPhynixxManagedConnection<C> getObservableProxy() {
            return (IPhynixxManagedConnection) proxiedObject;
        }

    }
}
