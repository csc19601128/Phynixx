package org.csc.phynixx.connection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;



public class PooledDynaProxyFactory extends AbstractDynaProxyFactory implements IPhynixxConnectionProxyFactory {
	
	private static Method closeMethod= null;
	
	static {
		try {
			closeMethod=IPhynixxConnection.class.getDeclaredMethod("close", new Class[] {});
		} catch (NoSuchMethodException e) {
			throw new DelegatedRuntimeException("Looking for IConnection.close()",e);
		}
	}
	
	
	public PooledDynaProxyFactory(Class[] supportedInterfaces) 
	{
		super(supportedInterfaces, 
			   new Class[] {IPhynixxConnection.class, IPhynixxConnectionProxy.class, IPhynixxConnectionHandle.class}, 
			   new Class[] {IRecordLoggerAware.class},
			   true);
	}

	public IPhynixxConnectionProxy getConnectionProxy() {
		return (IPhynixxConnectionProxy)
		         Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				    this.getSupportedInterfaces(), new ConnectionProxy() );
	}
	

	private  class ConnectionProxy extends PooledConnectionProxyAdapter implements IPhynixxConnectionHandle, IPooledConnection,InvocationHandler 
	{

		protected IPhynixxConnectionProxy getObservableProxy() {
			// TODO Auto-generated method stub
			return null;
		}

		private IPhynixxLogger logger= PhynixxLogManager.getLogger(this.getClass());
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable 
		{			
			/**
			 * methods of the IConnectionProxy are redirected to the current object
			 */

			if(logger.isDebugEnabled() ) {
				logger.debug("Calling "+method.getDeclaringClass()+"."+method.getName()+" on Object "+ this);
			}
			
			// execute
			try {
				if( method.getDeclaringClass().equals(IPhynixxConnection.class)	&&
					method.equals(PooledDynaProxyFactory.closeMethod)) 
				{
					if (this.getPooledConnectionFactory()==null) {
						throw new IllegalStateException("No PooledConnectionFactoryx assigned and the current proxy could not be released");
					}
					this.close();
					return null;
				}  else if ( method.getDeclaringClass().equals(IPhynixxConnectionProxy.class)|| 
				             method.getDeclaringClass().equals(IPhynixxConnection.class)|| 
				             method.getDeclaringClass().equals(IPhynixxConnectionHandle.class) || 
					          method.getDeclaringClass().equals(IPooledConnection.class)	    ) 
				{
					return method.invoke(this,args);
				}  else {	
					Object target= this.getConnection();
					// all methods of the interfaces joins the TX 
					this.fireConnectionRequiresTransaction();
					
					Object obj= method.invoke(target,args);					
					return obj; 
				} 
			} catch( InvocationTargetException targetEx) {
				throw new DelegatedRuntimeException("Invoke "+method, targetEx.getTargetException());
			} catch( Throwable ex) {
				throw new DelegatedRuntimeException("Invoke "+method, ex);
			}
		}

	
	}
	}
