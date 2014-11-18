package uk.co.strangeskies.utilities;

public interface Copyable<S extends Copyable<S>> {
	public/* Mutable */S copy();
}
