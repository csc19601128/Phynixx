package org.csc.phynixx.connection;




public abstract class PooledConnectionProxyAdapter extends PhynixxConnectionProxyAdapter implements  IPhynixxConnectionProxy , IPooledConnection 
{

	
	private PooledConnectionFactory pooledConnectionFactory= null; 
	
	
	/* (non-Javadoc)
	 * @see de.csc.xaresource.sample.connection.IPooledConnection#getPooledConnectionFactory()
	 */
	public PooledConnectionFactory getPooledConnectionFactory() {
		return pooledConnectionFactory;
	}


	/* (non-Javadoc)
	 * @see de.csc.xaresource.sample.connection.IPooledConnection#setPooledConnectionFactory(de.csc.xaresource.sample.connection.PooledConnectionFactory)
	 */
	public void setPooledConnectionFactory(
			PooledConnectionFactory pooledConnectionFactory) {
		this.pooledConnectionFactory = pooledConnectionFactory;
	}


	public synchronized void close() {
		this.pooledConnectionFactory.releaseConnection(this.getConnection());
		super.close();
	}
	
	
	
	
}
