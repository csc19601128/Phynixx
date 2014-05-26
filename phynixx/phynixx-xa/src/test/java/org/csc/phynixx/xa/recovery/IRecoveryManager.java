package org.csc.phynixx.xa.recovery;

import java.io.IOException;

import javax.transaction.xa.Xid;

public interface IRecoveryManager {	
	
	byte[] serializeXid(Xid xid) throws IOException;
	
	Xid deserialize(byte[] xid) throws IOException; 

}
