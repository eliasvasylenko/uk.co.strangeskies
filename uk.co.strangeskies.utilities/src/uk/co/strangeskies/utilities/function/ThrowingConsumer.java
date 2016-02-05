package uk.co.strangeskies.utilities.function;

public interface ThrowingConsumer<T, E extends Exception> {
	void accept(T value) throws E;
}
