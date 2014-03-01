package org.csc.phynixx.exceptions;

public class SampleTransactionalException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7037433162290063313L;

	public SampleTransactionalException(String string) {
		super(string);
	}

	public SampleTransactionalException() {
		super();
	}

	public SampleTransactionalException(String message, Throwable cause) {
		super(message, cause);
	}

	public SampleTransactionalException(Throwable cause) {
		super(cause);
	}

}
