package uk.co.strangeskies.gears.mathematics;

public interface Incrementor<T> {
	public T increment(T value);

	public T decrement(T value);

	public T getIncremented(T value);

	public T getDecremented(T value);

	public default T increment(T value, int amount) {
		for (int i = 0; i < amount; i++)
			increment(value);
		for (int i = 0; i > amount; i--)
			decrement(value);

		return value;
	}

	public default T decrement(T value, int amount) {
		for (int i = 0; i < amount; i++)
			decrement(value);
		for (int i = 0; i > amount; i--)
			increment(value);

		return value;
	}

	public default T getIncremented(T value, int amount) {
		if (amount < 0)
			value = getDecremented(value, -amount);
		else {
			if (amount > 0)
				value = getIncremented(value);
			for (int i = 1; i < amount; i++)
				increment(value);
		}

		return value;
	}

	public default T getDecremented(T value, int amount) {
		if (amount < 0)
			value = getIncremented(value, -amount);
		else {
			if (amount > 0)
				value = getDecremented(value);
			for (int i = 1; i < amount; i++)
				decrement(value);
		}

		return value;
	}

	public default Incrementor<T> reversed() {
		Incrementor<T> incrementor = this;

		return new Incrementor<T>() {
			@Override
			public T increment(T value) {
				return incrementor.decrement(value);
			}

			@Override
			public T decrement(T value) {
				return incrementor.increment(value);
			}

			@Override
			public T getIncremented(T value) {
				return incrementor.getDecremented(value);
			}

			@Override
			public T getDecremented(T value) {
				return incrementor.getIncremented(value);
			}

			@Override
			public T increment(T value, int amount) {
				return incrementor.decrement(value, amount);
			}

			@Override
			public T decrement(T value, int amount) {
				return incrementor.increment(value, amount);
			}

			@Override
			public T getIncremented(T value, int amount) {
				return incrementor.getDecremented(value, amount);
			}

			@Override
			public T getDecremented(T value, int amount) {
				return incrementor.getIncremented(value, amount);
			}
		};
	}
}
