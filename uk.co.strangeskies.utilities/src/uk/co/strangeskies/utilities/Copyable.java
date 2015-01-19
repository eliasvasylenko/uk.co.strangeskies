package uk.co.strangeskies.utilities;

public interface Copyable<S extends Copyable<S>> {
	public S copy();
}
