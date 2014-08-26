package uk.co.strangeskies.gears.mathematics;

public class NaturalIncrementor<T extends Incrementable<? extends T>>
		implements Incrementor<T> {
	@Override
	public final T increment(T value) {
		return value.increment();
	}

	@Override
	public final T decrement(T value) {
		return value.decrement();
	}

	@Override
	public final T getIncremented(T value) {
		return value.getIncremented();
	}

	@Override
	public final T getDecremented(T value) {
		return value.getDecremented();
	}
}
