package org.binas.domain.exception;

public class StationNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public StationNotFoundException() {
	}

	public StationNotFoundException(String message) {
		super(message);
	}
}
