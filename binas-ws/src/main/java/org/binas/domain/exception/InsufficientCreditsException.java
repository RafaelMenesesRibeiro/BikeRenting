package org.binas.domain.exception;

public class InsufficientCreditsException extends Exception {
	private static final long serialVersionUID = 1L;

	public InsufficientCreditsException() {
	}

	public InsufficientCreditsException(String message) {
		super(message);
	}
}
