package uk.co.strangeskies.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class TypeLiteral<T> {
	private final Type type;
	private final Class<? super T> rawClass;

	@SuppressWarnings("unchecked")
	protected TypeLiteral() {
		type = ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		rawClass = (Class<? super T>) Types.getRawType(type);
	}

	public TypeLiteral(Class<T> exactClass) {
		type = rawClass = exactClass;
	}

	private TypeLiteral(Type type, Class<? super T> rawType) {
		this.type = type;
		rawClass = rawType;
	}

	public static TypeLiteral<?> of(Type type) {
		return new TypeLiteral<>(type, Types.getRawType(type));
	}

	public final Type getType() {
		return type;
	}

	public final Class<? super T> getRawType() {
		return rawClass;
	}

	public boolean isPrimitive() {
		return Types.isPrimitive(type);
	}

	public boolean isPrimitiveWrapper() {
		return Types.isPrimitiveWrapper(type);
	}

	@SuppressWarnings("unchecked")
	public TypeLiteral<T> wrap() {
		if (isPrimitive())
			return (TypeLiteral<T>) of(Types.wrap(rawClass));
		else
			return this;
	}

	@SuppressWarnings("unchecked")
	public TypeLiteral<T> unwrap() {
		if (isPrimitiveWrapper())
			return (TypeLiteral<T>) of(Types.unwrap(rawClass));
		else
			return this;
	}

	public Set<Invokable<T, ?>> getInvokables() {
		return Arrays.stream(getRawType().getMethods())
				.map(m -> new Invokable<>(this, of(Object.class), m))
				.collect(Collectors.toSet());
	}

	public Invokable<T, ?> resolveOverload(String name, Type... parameters) {
		Set<Invokable<T, ?>> namedCandidates = getInvokables().stream()
				.filter(i -> i.getExecutable().getName().equals(name))
				.collect(Collectors.toSet());

		Set<Invokable<T, ?>> applicableCandidates = namedCandidates.stream()
				.map(i -> i.withStrictApplicability(parameters)).filter(o -> o != null)
				.collect(Collectors.toSet());

		if (applicableCandidates.isEmpty())
			applicableCandidates = namedCandidates.stream()
					.map(i -> i.withLooseApplicability(parameters))
					.filter(o -> o != null).collect(Collectors.toSet());

		if (applicableCandidates.isEmpty())
			applicableCandidates = namedCandidates.stream()
					.map(i -> i.withVariableArityApplicability(parameters))
					.filter(o -> o != null).collect(Collectors.toSet());

		return null;
	}
}
