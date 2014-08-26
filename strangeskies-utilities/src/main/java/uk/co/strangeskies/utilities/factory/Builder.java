package uk.co.strangeskies.utilities.factory;

public interface Builder<C extends Factory<T>, T> {
	C configure();
}
