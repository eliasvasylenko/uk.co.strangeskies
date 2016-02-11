package uk.co.strangeskies.utilities.classpath;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class PropertyType<T> {
	private static final List<PropertyType<?>> DEFAULT_TYPES = new ArrayList<>();

	public static final PropertyType<String> STRING = reg(new PropertyType<>(String.class, Function.identity()));

	public static final PropertyType<String[]> STRINGS = reg(STRING.asListType());

	public static final PropertyType<Long> LONG = reg(new PropertyType<>(Long.class, Long::parseLong));

	public static final PropertyType<Long[]> LONGS = reg(LONG.asListType());

	public static final PropertyType<Double> DOUBLE = reg(new PropertyType<>(Double.class, Double::parseDouble));

	public static final PropertyType<Double[]> DOUBLES = reg(DOUBLE.asListType());

	private final String name;
	private final Class<T> type;

	private final Function<? super String, ? extends T> parser;
	private final Function<? super T, ? extends String> composer;

	public PropertyType(Class<T> type, Function<? super String, ? extends T> parser) {
		this(type.getSimpleName(), type, parser, Object::toString);
	}

	private static <T> PropertyType<T> reg(PropertyType<T> propertyType) {
		DEFAULT_TYPES.add(propertyType);
		return propertyType;
	}

	public PropertyType(String name, Class<T> type, Function<? super String, ? extends T> parser,
			Function<? super T, ? extends String> composer) {
		this.name = name;
		this.type = type;

		this.parser = parser;
		this.composer = composer;
	}

	public PropertyType<T[]> asListType() {
		@SuppressWarnings("unchecked")
		Class<T[]> arrayType = (Class<T[]>) Array.newInstance(type(), 0).getClass();

		return new PropertyType<>("List<" + name() + ">", arrayType, null, null);
	}

	public String name() {
		return name;
	}

	public Class<T> type() {
		return type;
	}

	public String composeString(T value) {
		return composer.apply(value);
	}

	public T parseString(String string) {
		return parser.apply(string);
	}

	public static PropertyType<?> fromName(String name) {
		return fromName(name, Collections.emptySet());
	}

	public static PropertyType<?> fromName(String name, PropertyType<?>... knownTypes) {
		return fromName(name, Arrays.asList(knownTypes));
	}

	public static PropertyType<?> fromName(String name, Collection<? extends PropertyType<?>> knownTypes) {
		Set<PropertyType<?>> allKnownTypes = new HashSet<>(knownTypes);
		allKnownTypes.addAll(DEFAULT_TYPES);

		return allKnownTypes.stream().filter(p -> p.name().equals(name)).findAny()
				.orElseThrow(() -> new RuntimeException("Cannot find property type " + name));
	}
}
