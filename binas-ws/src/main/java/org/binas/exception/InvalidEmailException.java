package org.binas.exception;

public class InvalidEmailException extends Exception {

	public InvalidEmailException() {
		super();
	}

	public InvalidEmailException(String message) {
		super(message);
	}

	public InvalidEmailException(Throwable cause) {
		super(cause);
	}

	public InvalidEmailException(String message, Throwable cause) {
		super(message, cause);
	}

}
