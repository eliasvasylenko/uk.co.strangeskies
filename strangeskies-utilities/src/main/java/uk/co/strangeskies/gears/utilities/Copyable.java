package uk.co.strangeskies.gears.utilities;

public interface Copyable<S extends Copyable<S>> {
	public/* Mutable */S copy();
}
