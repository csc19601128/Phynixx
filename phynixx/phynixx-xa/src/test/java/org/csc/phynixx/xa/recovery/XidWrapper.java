package org.csc.phynixx.xa.recovery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.transaction.xa.Xid;

public class XidWrapper implements Xid {
	
	private int formatId= 0;
	
	private byte[] gtrid= null; 
	
	private byte[] bqual= null; 

	public byte[] getBranchQualifier() {
		return bqual;
	}

	public int getFormatId() {
		return formatId;
	}

	public byte[] getGlobalTransactionId() {
		return gtrid;
	}
	
	
	public XidWrapper(Xid otherXid) {
		this.formatId= otherXid.getFormatId();
		this.bqual= otherXid.getBranchQualifier();
		this.gtrid= otherXid.getGlobalTransactionId();
	}
	
	public XidWrapper(byte[] xid) throws IOException 
	{
		this.internalize(xid);
	}
	
	
	private void internalize(byte[] xid) throws IOException
	{        
        DataInputStream inputIO= null;
        
        try {
	        ByteArrayInputStream byteIO= new ByteArrayInputStream(xid);
	        inputIO= new DataInputStream(byteIO);
	        this.formatId= inputIO.readInt();
	        
	        int gtrid_length= inputIO.readInt();        
	        this.gtrid= new byte[gtrid_length];
	        inputIO.read(this.gtrid);        
	
	        int bqual_length= inputIO.readInt();        
	        this.bqual= new byte[bqual_length];
	        inputIO.read(this.bqual);
		} finally {
			if( inputIO!=null) inputIO.close();
		}
        
        
	}
	
	public byte[] externalize() throws IOException {

	        
	        DataOutputStream outputIO= null;	        	

	        try {
				ByteArrayOutputStream byteIO= new ByteArrayOutputStream(); 
				outputIO = new DataOutputStream(byteIO);
				outputIO.writeInt(this.getFormatId());
				outputIO.writeInt(this.gtrid.length);
				outputIO.write(gtrid);
				outputIO.writeInt(bqual.length);
				outputIO.write(bqual);
				
				outputIO.flush();
				
				return byteIO.toByteArray();
			} finally {
				if( outputIO!=null) outputIO.close();
			}
	        
	        
	    }

}
