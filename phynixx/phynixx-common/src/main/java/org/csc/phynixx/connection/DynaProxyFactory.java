package org.csc.phynixx.connection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.csc.phynixx.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.exceptions.ExceptionUtils;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;


public class DynaProxyFactory extends AbstractDynaProxyFactory implements IPhynixxConnectionProxyFactory {
	
	public DynaProxyFactory(Class[] supportedInterfaces, boolean synchronize) {
		super(supportedInterfaces, 
				   new Class[] {IPhynixxConnection.class, IPhynixxConnectionProxy.class, IPhynixxConnectionHandle.class}, 
				   new Class[] {IRecordLoggerAware.class},
				   synchronize);

	}

	public DynaProxyFactory(Class[] supportedInterfaces) 
	{
		this(supportedInterfaces, true);
	}

	public IPhynixxConnectionProxy getConnectionProxy() {
		ConnectionProxy proxy=new ConnectionProxy();
		IPhynixxConnectionProxy proxied= (IPhynixxConnectionProxy)
		         Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				    this.getImplementedInterfaces(),proxy );
		proxy.proxiedObject=proxied;
		return proxied;
	}
	
	
	class ConnectionProxy extends PhynixxConnectionProxyAdapter implements IPhynixxConnectionHandle,InvocationHandler 
	{


		private IPhynixxLogger log= PhynixxLogManager.getLogger(this.getClass());
		/**
		 * As the proxy contains more information than the current implentation we have to store
		 * the proxy an use it in all call backs
		 */
		private IPhynixxConnectionProxy proxiedObject= null; 
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable 
		{
			Object target= null; 
			
			/**
			 * methods of the IConnectionProxy are redirected to the current object
			 */

			// execute
			try {
				if( DynaProxyFactory.this.declaredBySystemInterface(method) ) 
				{
					target= this; 
					
					// System.out.println("Thread " + Thread.currentThread()+" Connection IF expected to " + this+" on "+method);
					Object obj= null;
					if( DynaProxyFactory.this.isSynchronize()) {
						synchronized (this) {
							obj=method.invoke(this,args);							
						}
					} else {
						obj=method.invoke(this,args);
					}
					return obj; 
				}  else if( DynaProxyFactory.this.declaredBySupportedInterface(method))
				{					

					target= this.getConnection(); 
					
					// System.out.println("Thread " + Thread.currentThread()+" Delegated to Connection " + target+" on "+method);
					
					Object obj= null;
					if( DynaProxyFactory.this.isSynchronize()) {
						synchronized (this) {
							// as we do not no better we assume that all methods needs transactions ...
						    this.fireConnectionRequiresTransaction();
							obj= method.invoke(target,args);							
						}
					} else {
						// as we do not no better we assume that all methods needs transactions ...
					    this.fireConnectionRequiresTransaction();
						obj= method.invoke(target,args);
					}
					return obj; 
				} else {
					Object obj= null;
					if( DynaProxyFactory.this.isSynchronize()) {
						synchronized (this) {
							obj=method.invoke(this,args);							
						}
					} else {
						obj=method.invoke(this,args);
					}
					return obj; 
				}
			} catch( InvocationTargetException targetEx) {
				targetEx.getTargetException().printStackTrace();
				log.fatal("Error calling method "+method +" on " +target +" :: "+targetEx.getMessage()+"\n"+ExceptionUtils.getStackTrace(targetEx.getTargetException()));
				throw new DelegatedRuntimeException("Invoking "+method,targetEx.getTargetException());
			}catch( Throwable ex) {
				log.fatal("Error calling method "+method +" on " +target +" :: "+ex+"\n"+ExceptionUtils.getStackTrace(ex));
				throw new DelegatedRuntimeException("Invoking "+method,ex);
			}
		}


		protected IPhynixxConnectionProxy getObservableProxy() {
			return (IPhynixxConnectionProxy)proxiedObject;
		}
	
	}

}
