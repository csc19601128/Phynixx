package org.csc.phynixx.spring.jta;

import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.springframework.jdbc.datasource.ConnectionProxy;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by christoph on 14.12.2014.
 */
public class TransactionAwareXAResource<C> {

	ReflectionUtils reflectionUtils; 

    Object targetConnectionFactory =null;

    Class<C>  connectionType;

    /**
     * Create a new TransactionAwareDataSourceProxy.
     * @see #setTargetDataSource
     */
    public TransactionAwareXAResource() {
    }

    /**
     * Create a new TransactionAwareDataSourceProxy.
     * @param targetDataSource the target DataSource
     */
    public TransactionAwareXAResource(Object connectionFactory) {
      this.targetConnectionFactory=connectionFactory;
    }

    public Object getTargetConnectionFactory() {
        return targetConnectionFactory;
    }

    public void setTargetConnectionFactory(Object targetConnectionFactory, Class<C> connectionType) {
        this.targetConnectionFactory = targetConnectionFactory;
        this.connectionType=connectionType;
    }

    public Class<?> getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(Class<C> connectionType) {
        this.connectionType = connectionType;
    }


    /**
     * Delegates to DataSourceUtils for automatically participating in Spring-managed
     * transactions. Throws the original SQLException, if any.
     * <p>The returned Connection handle implements the ConnectionProxy interface,
     * allowing to retrieve the underlying target Connection.
     * @return a transactional Connection if any, a new one else
     * @see org.springframework.jdbc.datasource.DataSourceUtils#doGetConnection
     * @see org.springframework.jdbc.datasource.ConnectionProxy#getTargetConnection
     */
//    @Override
//    public C getConnection()  {
//        Object ds = getTargetConnectionFactory();
//        Assert.state(ds != null, "'targetDataSource' is required");
//        return getTransactionAwareConnectionProxy(ds);
//    }
//
//    /**
//     * Wraps the given Connection with a proxy that delegates every method call to it
//     * but delegates {@code close()} calls to DataSourceUtils.
//     * @param targetDataSource DataSource that the Connection came from
//     * @return the wrapped Connection
//     * @see java.sql.Connection#close()
//     * @see org.springframework.jdbc.datasource.DataSourceUtils#doReleaseConnection
//     */
//    protected C getTransactionAwareConnectionProxy(Object targetDataSource, Class<C> connectionType) {
//        return (Connection) Proxy.newProxyInstance(
//                ConnectionProxy.class.getClassLoader(),
//                new Class[]{ConnectionProxy.class, IObject.class, IConnectionDelegate.class},
//                new TransactionAwareInvocationHandler(targetDataSource,connectionType));
//    }
//
//    /**
//     * Determine whether to obtain a fixed target Connection for the proxy
//     * or to reobtain the target Connection for each operation.
//     * <p>The default implementation returns {@code true} for all
//     * standard cases. This can be overridden through the
//     * {@link #setReobtainTransactionalConnections "reobtainTransactionalConnections"}
//     * flag, which enforces a non-fixed target Connection within an active transaction.
//     * Note that non-transactional access will always use a fixed Connection.
//     * @param targetDataSource the target DataSource
//     */
//    protected boolean shouldObtainFixedConnection(DataSource targetDataSource) {
//        return (!TransactionSynchronizationManager.isSynchronizationActive() ||
//                !this.reobtainTransactionalConnections);
//    }
//
//
//    /**
//     * Invocation handler that delegates close calls on JDBC Connections
//     * to DataSourceUtils for being aware of thread-bound transactions.
//     */
//    private class TransactionAwareInvocationHandler<CC> implements InvocationHandler {
//
//
//        Object targetConnectionFactory =null;
//
//        Class<CC>  connectionType;
//
//
//        private CC target;
//
//        private boolean closed = false;
//
//        public TransactionAwareInvocationHandler(DataSource targetDataSource, Class<CC> connectionType) {
//            this.targetConnectionFactory = targetConnectionFactory;
//            this.connectionType=connectionType;
//        }
//
//        private boolean ismplementedBy(Method method, Class<?> iface) {
//            try {
//                return iface.getDeclaredMethod(method.getName(), method.getParameterTypes()) != null;
//            } catch (NoSuchMethodException e) {
//                return false;
//            }
//        }
//
//        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            // Invocation on ConnectionProxy interface coming in...
//
//            if (method.getName().equals("equals")) {
//                // Only considered as equal when proxies are identical.
//                return (proxy == args[0]);
//            }
//            else if (method.getName().equals("hashCode")) {
//                // Use hashCode of Connection proxy.
//                return System.identityHashCode(proxy);
//            }
//            else if (method.getName().equals("toString")) {
//                // Allow for differentiating between the proxy and the raw Connection.
//                StringBuilder sb = new StringBuilder("Transaction-aware proxy for target Connection ");
//                if (this.target != null) {
//                    sb.append("[").append(this.target.toString()).append("]");
//                }
//                else {
//                    sb.append(" from DataSource [").append(this.targetDataSource).append("]");
//                }
//                return sb.toString();
//            }
//           else if (method.getName().equals("close")) {
//                // Handle close method: only close if not within a transaction.
//                DataSourceUtils.doReleaseConnection(this.target, this.targetDataSource);
//                this.closed = true;
//                return null;
//            }
//            else if (method.getName().equals("isClosed")) {
//                return this.closed;
//            }
//
//            if (this.target == null) {
//                if (this.closed) {
//                    throw new DelegatedRuntimeException()n("Connection handle already closed");
//                }
//                this.target = DataSourceUtils.doGetConnection(this.targetDataSource);
//                            }
//            Connection actualTarget = this.target;
//            if (actualTarget == null) {
//                actualTarget = DataSourceUtils.doGetConnection(this.targetDataSource);
//            }
//
//            if (method.getName().equals("getTargetConnection")) {
//                // Handle getTargetConnection method: return underlying Connection.
//                return actualTarget;
//            }
//
//            // Invoke method on target Connection.
//            try {
//                Object retVal = method.invoke(actualTarget, args);
//
//                // If return value is a Statement, apply transaction timeout.
//                // Applies to createStatement, prepareStatement, prepareCall.
//                if (retVal instanceof Statement) {
//                    DataSourceUtils.applyTransactionTimeout((Statement) retVal, this.targetDataSource);
//                }
//
//                return retVal;
//            }
//            catch (InvocationTargetException ex) {
//                throw ex.getTargetException();
//            }
//            finally {
//                if (actualTarget != this.target) {
//                    DataSourceUtils.doReleaseConnection(actualTarget, this.targetDataSource);
//                }
//            }
//        }
//    }
}
