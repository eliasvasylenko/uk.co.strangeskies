package uk.co.strangeskies.utilities.function;

public interface ThrowingSupplier<T, E extends Exception> {
	T get() throws E;
}
