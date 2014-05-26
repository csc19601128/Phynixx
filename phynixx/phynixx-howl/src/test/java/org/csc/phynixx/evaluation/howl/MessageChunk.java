package org.csc.phynixx.evaluation.howl;


public class MessageChunk {
	
	private long backwardReference= -1L;
	private Long messageId= null;
	private byte[] data= null;
	

	
	public MessageChunk(long backwardReference, long messageId, byte[] data) {
		super();
		this.backwardReference = backwardReference;
		this.messageId = new Long(messageId);
		this.data =data;
	}

	public long getBackwardReference() {
		return backwardReference;
	}

	public Long getMessageId() {
		return messageId;
	}

	public byte[] getData() {
		return data;
	} 
	
	
	
	
	
	
	
}
