package uk.co.strangeskies.gears.utilities.factory;

public class BuilderStateException extends RuntimeException {
	private static final long serialVersionUID = -7915027424255184075L;

	public BuilderStateException(Factory<?> configurator, String message) {
		super(message(configurator, message));
	}

	public BuilderStateException(Factory<?> configurator, String message,
			Throwable cause) {
		super(message(configurator, message), cause);
	}

	private static String message(Factory<?> configurator, String message) {
		return "Builder '" + configurator + "' excepetion: " + message;
	}
}
