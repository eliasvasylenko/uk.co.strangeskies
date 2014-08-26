package uk.co.strangeskies.gears.utilities.factory;

public interface Builder<C extends Factory<T>, T> {
	C configure();
}
