package uk.co.strangeskies.gears.utilities.factory;

public class StaleBuilderStateException extends BuilderStateException {
	private static final long serialVersionUID = 2380924166435297716L;

	public StaleBuilderStateException(Factory<?> configurator) {
		super(configurator, "Builder state is stale.");
	}
}
