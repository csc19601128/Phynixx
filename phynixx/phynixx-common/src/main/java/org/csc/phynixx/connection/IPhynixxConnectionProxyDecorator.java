package org.csc.phynixx.connection;



public interface IPhynixxConnectionProxyDecorator {

	/**
	 * installs this strategy to the given connection
	 * 
	 * @param connectionProxy
	 * @return TODO
	 */
	public IPhynixxConnectionProxy decorate(IPhynixxConnectionProxy connectionProxy);

}