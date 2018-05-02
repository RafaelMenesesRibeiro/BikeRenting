package org.binas.domain.exception;

public class UserAlreadyHasBinaException extends Exception {
	private static final long serialVersionUID = 1L;

	public UserAlreadyHasBinaException() {
	}

	public UserAlreadyHasBinaException(String message) {
		super(message);
	}
}
