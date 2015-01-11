package uk.co.strangeskies.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TypeLiteral<T> implements GenericTypeContainer<Class<? super T>> {
	private final Type type;
	private final Class<? super T> rawType;

	private final Resolver resolver;

	@SuppressWarnings("unchecked")
	protected TypeLiteral() {
		if (getClass().getSuperclass().equals(TypeLiteral.class))
			type = ((ParameterizedType) getClass().getGenericSuperclass())
					.getActualTypeArguments()[0];
		else {
			Resolver resolver = new Resolver();
			resolver.incorporateTypes(getClass().getGenericSuperclass());

			type = ((ParameterizedType) resolver
					.resolveTypeParameters(TypeLiteral.class)).getActualTypeArguments()[0];
		}

		rawType = (Class<? super T>) Types.getRawType(type);

		resolver = new Resolver();
	}

	public TypeLiteral(Class<T> exactClass) {
		this(exactClass, exactClass);
	}

	private TypeLiteral(Type type, Class<? super T> rawType) {
		this.type = type;
		this.rawType = rawType;

		resolver = new Resolver();
	}

	public static TypeLiteral<?> from(Type type) {
		return new TypeLiteral<>(type, Types.getRawType(type));
	}

	public static <T> TypeLiteral<T> from(Class<T> type) {
		return new TypeLiteral<>(type);
	}

	public static <T> TypeLiteral<T> from(String typeString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return type.toString();
	}

	public Type getType() {
		return type;
	}

	public Class<? super T> getRawType() {
		return rawType;
	}

	@Override
	public Class<? super T> getGenericDeclaration() {
		return getRawType();
	}

	@Override
	public Type getDeclaringType() {
		return getType();
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
			return (TypeLiteral<T>) from(Types.wrap(rawType));
		else
			return this;
	}

	@SuppressWarnings("unchecked")
	public TypeLiteral<T> unwrap() {
		if (isPrimitiveWrapper())
			return (TypeLiteral<T>) from(Types.unwrap(rawType));
		else
			return this;
	}

	public boolean isAssignableFrom(Type type) {
		return Types.isAssignable(type, this.type);
	}

	public Type resolveType(Type type) {
		return resolveType(from(type)).getType();
	}

	public <U> TypeLiteral<? extends U> resolveType(TypeLiteral<U> type) {
		return null;
	}

	public <U> TypeLiteral<? extends U> resolveTypeParameters(Class<U> type2) {
		return null;
	}

	public TypeLiteral<? extends T> withTypeArguments(
			Map<TypeVariable<? extends Class<?>>, Type> instantiations) {
		return null;
	}

	public TypeLiteral<? extends T> withTypeArgument(
			TypeVariable<? extends Class<?>> typeVariable, Type instantiation) {
		return null;
	}

	public <V> TypeLiteral<? extends T> withTypeArgument(
			TypeParameter<V> variable, TypeLiteral<V> instantiation) {
		return withTypeArgument(variable.getType(), instantiation.getType());
	}

	public <V> TypeLiteral<? extends T> withTypeArgument(
			TypeParameter<V> variable, Class<V> instantiation) {
		return withTypeArgument(variable.getType(), instantiation);
	}

	public Set<Invokable<T, T>> getConstructors() {
		return Arrays.stream(getRawType().getConstructors())
				.map(m -> new Invokable<>(this, this, m)).collect(Collectors.toSet());
	}

	public Set<Invokable<T, ?>> getMethods() {
		return Arrays.stream(getRawType().getMethods())
				.map(m -> new Invokable<>(this, from(Object.class), m))
				.collect(Collectors.toSet());
	}

	public Set<Invokable<T, ?>> getInvokables() {
		Set<Invokable<T, ?>> invokables = new HashSet<>();
		invokables.addAll(getConstructors());
		invokables.addAll(getMethods());
		return invokables;
	}

	public Invokable<T, ? extends T> resolveConstructorOverload(
			Type... parameters) {
		return resolveInvokableOverload(getConstructors(), parameters);
	}

	public Invokable<T, ?> resolveMethodOverload(String name, Type... parameters) {
		Set<Invokable<T, ?>> candidates = getMethods().stream()
				.filter(i -> i.getGenericDeclaration().getName().equals(name))
				.collect(Collectors.toSet());

		return resolveInvokableOverload(candidates, parameters);
	}

	private <R> Invokable<T, ? extends R> resolveInvokableOverload(
			Set<? extends Invokable<T, ? extends R>> candidates, Type... parameters) {
		candidates = candidates.stream()
				.map(i -> i.withVariableArityApplicability(parameters))
				.filter(o -> o != null).collect(Collectors.toSet());

		if (candidates.isEmpty())
			throw new IllegalArgumentException();
		else {
			Set<Invokable<T, ? extends R>> moreSpecificCandidates = candidates
					.stream().map(i -> i.withLooseApplicability(parameters))
					.filter(o -> o != null).collect(Collectors.toSet());
			if (!moreSpecificCandidates.isEmpty())
				candidates = moreSpecificCandidates;

			moreSpecificCandidates = candidates.stream()
					.map(i -> i.withStrictApplicability(parameters))
					.filter(o -> o != null).collect(Collectors.toSet());
			if (!moreSpecificCandidates.isEmpty())
				candidates = moreSpecificCandidates;
		}

		return null;
	}
}
