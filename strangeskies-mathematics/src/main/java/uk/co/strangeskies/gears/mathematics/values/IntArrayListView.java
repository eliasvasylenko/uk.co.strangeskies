package uk.co.strangeskies.gears.mathematics.values;

import java.util.AbstractList;

import uk.co.strangeskies.gears.utilities.factory.Factory;

public class IntArrayListView<V extends Value<V>> extends AbstractList<V> {
	private final int[] array;
	private final Factory<V> valueFactory;

	public IntArrayListView(int[] array, Factory<V> valueFactory) {
		if (array == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		this.array = array;
		this.valueFactory = valueFactory;
	}

	@Override
	public final V get(int index) {
		return valueFactory.create().setValue(array[index]);
	}

	public V set(int index, Value<?> element) {
		int previousValue = array[index];

		array[index] = element.intValue();

		return valueFactory.create().setValue(previousValue);
	}

	public Factory<V> getValueFactory() {
		return valueFactory;
	}

	@Override
	public final int size() {
		return array.length;
	}
}
