package uk.co.strangeskies.gears.utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PropertySet<T> {
	private final Class<T> objectClass;
	private final T object;
	private final Set<Function<? super T, Object>> properties;

	public PropertySet(Class<T> objectClass, T object) {
		this.objectClass = objectClass;
		this.object = object;
		properties = new HashSet<>();
	}

	@SuppressWarnings("unchecked")
	public PropertySet(Class<T> objectClass, PropertySet<?> propertySet) {
		if (!propertySet.objectClass.isAssignableFrom(objectClass)
				|| !objectClass.isInstance(propertySet.object))
			throw new IllegalArgumentException();

		this.objectClass = objectClass;
		this.object = (T) propertySet.object;
		properties = new HashSet<>(
				((PropertySet<? super T>) propertySet).properties);
	}

	public PropertySet<T> add(Function<? super T, Object> property) {
		properties.add(property);
		return this;
	}

	public boolean testEquality(Object object) {
		if (this.object == object)
			return true;

		if (!objectClass.isInstance(object))
			return false;

		@SuppressWarnings("unchecked")
		T thatObject = (T) object;
		return properties.stream().allMatch(
				p -> Objects.equals(p.apply(this.object), p.apply(thatObject)));
	}

	public int generateHashCode() {
		return properties.stream().map(p -> Objects.hashCode(p.apply(object)))
				.reduce(0, (a, b) -> a ^ b);
	}

	public List<Object> values() {
		return properties.stream().map(p -> p.apply(object))
				.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		String lineSeparator = System.getProperty("line.separator");

		StringBuilder stringBuilder = new StringBuilder().append("{").append(
				lineSeparator);
		for (Function<? super T, Object> property : properties)
			stringBuilder.append("  : ").append(property.apply(object))
					.append(lineSeparator);
		stringBuilder.append("}");

		return stringBuilder.toString();
	}
}
