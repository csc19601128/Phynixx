package org.csc.phynixx.xa.recovery;

import javax.transaction.xa.XAResource;

import org.objectweb.jotm.TransactionResourceManager;

/**
 * 
 * Dummy implememntation of JOTM's TransactionresourceManager
 * 
 * It's used by JOTM to record the Mapping of Resource's name to XAResource
 * @author christoph
 *
 */

public class   SampleTransactionResourceManager implements TransactionResourceManager 
{
		
	public void returnXAResource(String rmName, XAResource rmXares) 
	{
		return ;

	}

}
