package uk.co.strangeskies.utilities.factory;

public class IncompleteBuildStateException extends BuilderStateException {
	private static final long serialVersionUID = -84782003263925409L;

	public IncompleteBuildStateException(Factory<?> configurator) {
		super(configurator, "Build state is incomplete.");
	}
}
