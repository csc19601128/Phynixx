package org.csc.phynixx.xa;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.csc.phynixx.connection.IRecordLoggerAware;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxConnectionFactory;
import org.csc.phynixx.exceptions.DelegatedRuntimeException;


public class XAPooledConnectionFactory implements IPhynixxConnectionFactory {

	private IPhynixxConnectionFactory connectionFactory= null; 
	private GenericObjectPool genericObjectPool= null; 
	
	private class MyPoolableObjectFactory implements PoolableObjectFactory {

		public void activateObject(Object obj) throws Exception {	
		}

		public void destroyObject(Object obj) throws Exception 
		{
			((IPhynixxConnection)obj).close();	
		}

		public Object makeObject() throws Exception {
			return XAPooledConnectionFactory.this.getConnectionFactory().getConnection();
		}

		public void passivateObject(Object obj) throws Exception {
		}

		public boolean validateObject(Object obj) {
			IPhynixxConnection con= (IPhynixxConnection)obj;	
			return !( con.isClosed()); 
		}
		
	}

	public XAPooledConnectionFactory(IPhynixxConnectionFactory connectionFactory) 
	{
		this(connectionFactory,null);
	}

	public XAPooledConnectionFactory(IPhynixxConnectionFactory connectionFactory,
			                       GenericObjectPool.Config genericPoolConfig) 
	{
		this.connectionFactory = connectionFactory;
		GenericObjectPool.Config cfg= genericPoolConfig;
		if( cfg==null) {
			cfg=new GenericObjectPool.Config();
		}
		this.genericObjectPool= new GenericObjectPool(new MyPoolableObjectFactory(), cfg);
	}

	public IPhynixxConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public int getMaxActive() {
		return this.genericObjectPool.getMaxActive();
	}

	public IPhynixxConnection getConnection() {
		try {
			return (IPhynixxConnection)this.genericObjectPool.borrowObject();
		} catch (Exception e) {
			throw new DelegatedRuntimeException(e);
		}
	}

	public void releaseConnection(IPhynixxConnection connection) 
	{
		if( connection==null) {
			return;
		}
		try {
			this.genericObjectPool.returnObject(connection);
		} catch( Exception e) {
			throw new DelegatedRuntimeException(e);
		}		
	}
	
	public void destroyConnection(IPhynixxConnection connection) 
	{
		if( connection==null) {
			return;
		}
		try {
			this.genericObjectPool.invalidateObject(connection);
		} catch( Exception e) {
			throw new DelegatedRuntimeException(e);
		}		
	}
	
	public void close() {
		try {
			this.genericObjectPool.close();
		} catch (Exception e) {
			throw new DelegatedRuntimeException(e);
		}
	}

	public Class connectionInterface() {
		return this.getConnectionFactory().connectionInterface();
	}

	

}
