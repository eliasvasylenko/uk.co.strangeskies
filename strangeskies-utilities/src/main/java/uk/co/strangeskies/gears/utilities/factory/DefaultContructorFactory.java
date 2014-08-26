package uk.co.strangeskies.gears.utilities.factory;

import java.lang.reflect.Constructor;

public class DefaultContructorFactory<T> implements Factory<T> {
	private final Class<T> clazz;

	public DefaultContructorFactory(Class<T> clazz) {
		boolean valid = false;
		for (Constructor<?> c : clazz.getConstructors()) {
			if (c.getParameterTypes().length == 0) {
				valid = true;
				break;
			}
		}
		if (!valid) {
			throw new IllegalArgumentException();
		}
		this.clazz = clazz;
	}

	@Override
	public T create() {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new AssertionError();
		}
	}
}
