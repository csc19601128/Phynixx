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


import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.exceptions.ExceptionUtils;
import org.csc.phynixx.common.generator.IDGenerator;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * <p/>
 * provided generic proxies on base of java DynaProxies
 * <p/>
 * This proxy
 *
 * @param <C>
 */
class DynaPhynixxManagedConnectionFactory<C extends IPhynixxConnection> extends AbstractDynaProxyFactory {

    private static final IDGenerator idGenerator = new IDGenerator(1);

    private Class<C> connectionInterface;

    DynaPhynixxManagedConnectionFactory(Class<C> connectionInterface, boolean synchronize) {
        super(new Class<?>[]{connectionInterface},
                new Class[]{IPhynixxConnection.class, IPhynixxManagedConnection.class},
                new Class[]{IXADataRecorderAware.class, ICloseable.class},
                synchronize);
        this.connectionInterface = connectionInterface;


    }

    DynaPhynixxManagedConnectionFactory(Class<C> connectionInterface) {
        this(connectionInterface, true);
    }

    IPhynixxManagedConnection<C> getManagedConnection(C coreConnection) {

        Long connectionId = null;
        synchronized (idGenerator) {
            connectionId = idGenerator.generateLong();
        }
        ConnectionPhynixxGuard proxy = new ConnectionPhynixxGuard(connectionId, connectionInterface, coreConnection);

        IPhynixxManagedConnection<C> proxied = (IPhynixxManagedConnection<C>)
                Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                        this.getImplementedInterfaces(), proxy);
        proxy.proxiedObject = proxied;
        return proxied;
    }


    class ConnectionPhynixxGuard<C extends IPhynixxConnection> extends PhynixxManagedConnectionGuard<C> implements InvocationHandler {


        private final IPhynixxLogger log = PhynixxLogManager.getLogger(ConnectionPhynixxGuard.class);
        /**
         * As the proxy contains more information than the current implentation we have to store
         * the proxy an use it in all call backs
         */
        private IPhynixxManagedConnection<C> proxiedObject;

        ConnectionPhynixxGuard(long id, Class<C> connectionInterface, C connection) {
            super(id, connectionInterface, connection);
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

                    target = this.getCoreConnection();

                    // System.out.println("Thread " + Thread.currentThread()+" Delegated to Connection " + target+" on "+method);

                    Object obj = null;
                    boolean requireTransaction = this.requiresTransaction(method);
                    try {
                        if (DynaPhynixxManagedConnectionFactory.this.isSynchronize()) {
                            synchronized (this) {
                                if (requireTransaction) {
                                    this.fireConnectionRequiresTransaction();
                                }
                                obj = method.invoke(target, args);
                                if (requireTransaction) {
                                    fireConnectionRequiresTransactionExecuted();
                                }
                            }
                        } else {
                            if (requireTransaction) {
                                this.fireConnectionRequiresTransaction();
                            }
                            obj = method.invoke(target, args);
                            if (requireTransaction) {
                                fireConnectionRequiresTransactionExecuted();
                            }
                        }
                        return obj;
                    } catch (InvocationTargetException targetException) {
                        if (requireTransaction) {
                            Exception e = null;
                            if (!(targetException.getTargetException() instanceof Exception)) {
                                e = new Exception(targetException.getCause());
                            } else {
                                e = (Exception) (targetException.getTargetException());
                            }
                            fireConnectionRequiresTransactionExecuted(e);
                        }


                        // rethrow;
                        throw targetException;

                    }
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
