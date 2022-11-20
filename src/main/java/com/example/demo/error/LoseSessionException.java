package com.example.demo.error;

public class LoseSessionException extends NullPointerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LoseSessionException() {
	}

	public LoseSessionException(String s) {
		super(s);
	}

}
