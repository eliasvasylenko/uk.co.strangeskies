package uk.co.strangeskies.gears.utilities.factory;

public class InvalidBuildStateException extends BuilderStateException {
	private static final long serialVersionUID = -84782003263925409L;

	public InvalidBuildStateException(Factory<?> configurator) {
		super(configurator, "Build state is invalid.");
	}

	public InvalidBuildStateException(Factory<?> configurator, Throwable cause) {
		super(configurator, "Build state is invalid.", cause);
	}
}
