package org.binas.domain.exception;

public class UserHasNoBinaException extends Exception {
	private static final long serialVersionUID = 1L;

	public UserHasNoBinaException() {
	}

	public UserHasNoBinaException(String message) {
		super(message);
	}
}
