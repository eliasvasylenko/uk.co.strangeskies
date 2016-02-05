package uk.co.strangeskies.utilities.function;

public interface ThrowingRunnable<E extends Exception> {
	void run() throws E;
}
