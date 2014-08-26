package uk.co.strangeskies.gears.utilities;

public interface Property<T extends R, R> {
	public T set(R to);

	public T get();
}
