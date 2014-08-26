package uk.co.strangeskies.gears.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Enumeration<S extends Enumeration<S>> implements Self<S> {
	private static class EnumerationType<T extends Enumeration<T>> {
		private final List<T> instances;
		private boolean initialised;

		public EnumerationType() {
			instances = new ArrayList<>();
			initialised = false;
		}

		public List<T> getInstances() {
			return Collections.unmodifiableList(instances);
		}

		public int addInstance(T instance) {
			if (initialised)
				throw new IllegalStateException();

			int ordinal = instances.size();

			instances.add(instance);

			return ordinal;
		}

		public void initialised() {
			initialised = true;
		}

		public boolean isInitialised() {
			return initialised;
		}
	}

	private static final Map<Class<?>, EnumerationType<?>> ENUM_TYPES = new HashMap<>();

	private final String name;
	private final int ordinal;

	protected Enumeration(String name) {
		this.name = name;
		ordinal = addInstance(getThis());
	}

	public String name() {
		return name;
	}

	@Override
	public final boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public final int hashCode() {
		return super.hashCode();
	}

	public int ordinal() {
		return ordinal;
	}

	@Override
	public String toString() {
		return name();
	};

	@Override
	public final S copy() {
		return getThis();
	}

	@SuppressWarnings("unchecked")
	private static <T extends Enumeration<T>> int addInstance(T instance) {
		EnumerationType<T> enumerationType = getEnumerationType(((Class<T>) instance
				.getClass()));
		List<T> enumerationConstants = enumerationType.getInstances();

		if (enumerationConstants.stream().anyMatch(
				e -> e.name().equals(instance.name())))
			throw new IllegalArgumentException();

		return enumerationType.addInstance(instance);
	}

	private static <T extends Enumeration<T>> EnumerationType<T> getEnumerationType(
			Class<T> type) {
		@SuppressWarnings("unchecked")
		EnumerationType<T> enumType = (EnumerationType<T>) ENUM_TYPES.get(type);
		if (enumType == null)
			ENUM_TYPES.put(type, enumType = new EnumerationType<>());

		if (!enumType.isInitialised() && !withinStaticInitialiser(type)) {
			forceInitialisation(type);
			enumType.initialised();
		}

		return enumType;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends Enumeration> List<T> getConstants(
			Class<T> enumerationClass) {
		return Collections.unmodifiableList(getEnumerationType(enumerationClass)
				.getInstances());
	}

	public static <T extends Enumeration<T>> T valueOf(Class<T> enumerationClass,
			String name) {
		return getConstants(enumerationClass).stream()
				.filter(e -> e.name().equals(name)).findAny().get();
	}

	private static void forceInitialisation(Class<?> initialiseClass) {
		try {
			Class.forName(initialiseClass.getName(), true,
					initialiseClass.getClassLoader());
		} catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}
	}

	private static boolean withinStaticInitialiser(Class<?> initialisingClass) {
		for (StackTraceElement element : Thread.currentThread().getStackTrace())
			if (element.getClassName().equals(initialisingClass.getCanonicalName())
					&& element.getMethodName().equals("<clinit>"))
				return true;

		return false;
	}
}
