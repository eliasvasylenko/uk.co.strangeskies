package uk.co.strangeskies.reflection.impl;

public class TypeInferenceException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TypeInferenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public TypeInferenceException(String message) {
		super(message);
	}
}
