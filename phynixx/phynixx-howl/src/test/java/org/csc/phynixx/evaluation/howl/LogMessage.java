package org.csc.phynixx.evaluation.howl;

import java.util.ArrayList;
import java.util.List;

public class LogMessage {
	
	private Long messageId= null;
	
	private List messageChunks= new ArrayList(); 
	
	private transient byte[] data= null; //tmp array for data

	public LogMessage(long messageId) {
		this.messageId= new Long(messageId);
	}
	
	public LogMessage(Long messageId) {
		this(messageId.longValue());
	}
	public void addMessageChunk(MessageChunk chunk) {
		this.messageChunks.add(chunk);
	}
	
	public List getMessageChunks() {
		return messageChunks;
	}
	
	public byte[] [] getData() {
		
		byte[] [] data= new byte[ messageChunks.size()] [];
		for(int i=0; i< this.messageChunks.size();i++) {
			data[i]= ((MessageChunk)this.messageChunks.get(i)).getData();
		}		
		return data;
	}

	public boolean equals(Object obj) {
		if( obj==null || !(obj instanceof LogMessage)) {
			return false;
		}
		return this.messageId.longValue()== ((LogMessage)obj).getMessageId().longValue();
	}

	public Long getMessageId() {
		return this.messageId;
	}

	public int hashCode() {
		return this.messageId.hashCode();
	}
	
	
	
	
	
	
	

}
