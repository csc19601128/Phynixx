package org.csc.phynixx.connection;

import java.lang.reflect.Method;

public class AbstractDynaProxyFactory {

	private Class[] supportedInterfaces = null;
	private Class[] requiredInterfaces = null;
	private Class[] optionalInterfaces= null;
	private Class[] implementedInterfaces= null; 
	
	private boolean synchronize= false; 

	public AbstractDynaProxyFactory(Class[] supportedInterfaces, Class[] requiredInterfaces, Class[] optionalInterfaces, boolean synchronize) 
	{
		this.synchronize= synchronize;
		if( supportedInterfaces==null || supportedInterfaces.length==0) {
			throw new IllegalArgumentException("supportedInterfaces are missing");
		}		
		this.supportedInterfaces= supportedInterfaces;
		this.requiredInterfaces= requiredInterfaces;
		this.implementedInterfaces= supportedInterfaces;
		this.optionalInterfaces= optionalInterfaces;
		if( requiredInterfaces==null) {
			return;
		}
		for( int i=0; i <requiredInterfaces.length;i++) { 
			this.implementedInterfaces= this.addRequiredInterface(this.implementedInterfaces, requiredInterfaces[i]);
		}
		
	}
	
	public boolean isSynchronize() {
		return synchronize;
	}

	private Class[] addRequiredInterface(Class[] implementedInterfaces, Class requiredInterface) 
	{
		for( int i=0; i <implementedInterfaces.length;i++) {
			if( implementedInterfaces[i].isInterface() && 
					requiredInterface.isAssignableFrom(implementedInterfaces[i])) 
			{
				return implementedInterfaces;
			}
		}
		
		// if not found extend the array of supported interfaces 
		Class[] xImplementedInterfaces= new Class[implementedInterfaces.length+1];
		xImplementedInterfaces[0]= requiredInterface;
		for( int i=0; i< implementedInterfaces.length;i++) {
			xImplementedInterfaces[i+1]= implementedInterfaces[i];
		}
		return xImplementedInterfaces;
		
	}
	

	protected Class[] getSupportedInterfaces() {
		return supportedInterfaces;
	}
	
	

	protected Class[] getImplementedInterfaces() {
		return implementedInterfaces;
	}
	
	protected Class[] getRequiredInterfaces () {
		return requiredInterfaces;
	}
	
	
	public Class[] getOptionalInterfaces() {
		return optionalInterfaces;
	}

	private boolean declaredBy(Method method, Class[] interfaces) 
	{
		Class declaringClass= method.getDeclaringClass();
		for( int i=0; i< interfaces.length;i++) {
			if( declaringClass.equals(interfaces[i])) {
				return true;
			}
		}
		return false;
	}
	protected boolean declaredBySupportedInterface(Method method) {
		return declaredBy(method, this.getSupportedInterfaces());		
	}
	
	protected boolean declaredBySystemInterface(Method method) {
		return declaredBy(method, this.getRequiredInterfaces()) || declaredBy(method, this.getOptionalInterfaces()) ;		
	}
	
}