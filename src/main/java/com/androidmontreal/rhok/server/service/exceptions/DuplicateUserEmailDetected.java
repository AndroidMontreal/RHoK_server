package com.androidmontreal.rhok.server.service.exceptions;

public class DuplicateUserEmailDetected extends Exception {
	private static final long serialVersionUID = -8431522315089026199L;

	public DuplicateUserEmailDetected() {
		super();
	}

	public DuplicateUserEmailDetected(String arg0) {
		super(arg0);
	}
	
}
