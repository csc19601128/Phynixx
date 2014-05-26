package org.csc.phynixx.connection;

public interface IPooledConnection extends IPhynixxConnection{

	public PooledConnectionFactory getPooledConnectionFactory();

	public void setPooledConnectionFactory(
			PooledConnectionFactory pooledConnectionFactory);

}