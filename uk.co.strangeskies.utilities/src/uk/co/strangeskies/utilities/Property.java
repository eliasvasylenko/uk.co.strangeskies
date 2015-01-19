package uk.co.strangeskies.utilities;

public interface Property<T extends R, R> {
	public T set(Property<T, R> this, R to);

	public T get();
}
