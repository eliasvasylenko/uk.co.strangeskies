package uk.co.strangeskies.mathematics.values;

import uk.co.strangeskies.utilities.factory.Factory;

public abstract class ValueFactory<V extends Value<V>> implements Factory<V> {
	public V get(int value) {
		return create().setValue(value);
	}

	public V get(long value) {
		return create().setValue(value);
	}

	public V get(double value) {
		return create().setValue(value);
	}

	public V get(float value) {
		return create().setValue(value);
	}
}