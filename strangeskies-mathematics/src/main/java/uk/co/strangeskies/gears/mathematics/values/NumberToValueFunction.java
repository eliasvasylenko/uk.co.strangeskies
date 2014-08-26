package uk.co.strangeskies.gears.mathematics.values;

import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.factory.Factory;

public class NumberToValueFunction<V extends Value<V>> implements
		Function<Number, V> {
	private final Factory<V> valueFactory;

	public <X extends V> NumberToValueFunction(Factory<V> valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	public V apply(Number input) {
		return valueFactory.create().setValue(input);
	}
}
