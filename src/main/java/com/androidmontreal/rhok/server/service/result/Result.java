package com.androidmontreal.rhok.server.service.result;

/**
 * <p>Attempt at a super basic result class. 
 * <p>TODO: Make sure it's used or clear it out.
 */
public class Result {
	public enum State {
		SUCCESS, FAILURE
	};

	private State state;
	private String message;
	
	public Result(State state) {
		this.state = state;
	}

	public Result(State state, String message) {
		this.state = state;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public State getState() {
		return state;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setState(State state) {
		this.state = state;
	}

}
