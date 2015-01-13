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

	private Resolver resolver;

	@SuppressWarnings("unchecked")
	protected TypeLiteral() {
		if (getClass().getSuperclass().equals(TypeLiteral.class))
			type = ((ParameterizedType) getClass().getGenericSuperclass())
					.getActualTypeArguments()[0];
		else {
			Resolver resolver = new Resolver();

			Class<?> superClass = getClass();
			do {
				resolver.incorporateTypes(superClass.getGenericSuperclass());
				superClass = superClass.getSuperclass();
			} while (!superClass.equals(TypeLiteral.class));

			type = resolver
					.resolveTypeVariable(TypeLiteral.class.getTypeParameters()[0]);
		}

		rawType = (Class<? super T>) Types.getRawType(type);
	}

	public TypeLiteral(Class<T> exactClass) {
		this(exactClass, exactClass);
	}

	private TypeLiteral(Type type, Class<? super T> rawType) {
		this.type = type;
		this.rawType = rawType;
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

	private Resolver getResolver() {
		if (resolver == null) {
			resolver = new Resolver();
			resolver.incorporateTypes(type);
		}
		return resolver;
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
		return getResolver().resolveType(type);
	}

	@SuppressWarnings("unchecked")
	public <U> TypeLiteral<? extends U> resolveType(TypeLiteral<U> type) {
		return (TypeLiteral<? extends U>) TypeLiteral.from(resolveType(type
				.getType()));
	}

	@SuppressWarnings("unchecked")
	public <U> TypeLiteral<? extends U> resolveSupertypeParameters(Class<U> type) {
		boolean isGeneric = type.getg(type.getEnclosingClass());

		System.out.println("  resolver parameters of '" + type.getName()
				+ "' WRT subtype '" + this + "'.");
		if (!type.isAssignableFrom(rawType))
			throw new IllegalArgumentException();

		Class<?> superClass = rawType;
		Type superType = this.type;
		while (!superClass.equals(type)) {
			Set<Type> superTypes = new HashSet<>(Arrays.asList(superClass
					.getGenericInterfaces()));
			if (superClass.getSuperclass() != null)
				superTypes.addAll(Arrays.asList(superClass.getGenericSuperclass()));

			superType = superTypes.stream()
					.filter(t -> type.isAssignableFrom(Types.getRawType(t))).findAny()
					.get();

			getResolver().incorporateTypes(superType);
			superClass = Types.getRawType(superType);
		}

		if (!(superType instanceof Class))
			superType = resolveType(Types.parameterizedType(type));

		return (TypeLiteral<? extends U>) TypeLiteral.from(superType);
	}

	public <U extends T> TypeLiteral<? extends U> resolveSubtypeParameters(
			Class<U> type) {
		throw new UnsupportedOperationException();
	}

	public TypeLiteral<? extends T> withTypeArguments(
			Map<TypeVariable<? extends Class<?>>, Type> instantiations) {
		throw new UnsupportedOperationException();
	}

	public TypeLiteral<? extends T> withTypeArgument(
			TypeVariable<? extends Class<?>> typeVariable, Type instantiation) {
		throw new UnsupportedOperationException();
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

	public Set<? extends Invokable<? super T, ?>> getMethods() {
		// TODO include inherited methods.
		return Arrays.stream(getRawType().getMethods())
				.map(m -> new Invokable<>(this, from(Object.class), m))
				.collect(Collectors.toSet());
	}

	public Set<? extends Invokable<? super T, ?>> getInvokables() {
		Set<Invokable<? super T, ?>> invokables = new HashSet<>();
		invokables.addAll(getConstructors());
		invokables.addAll(getMethods());
		return invokables;
	}

	public Invokable<T, ? extends T> resolveConstructorOverload(
			Type... parameters) {
		return resolveInvokableOverload(getConstructors(), parameters);
	}

	public Invokable<? super T, ?> resolveMethodOverload(String name,
			Type... parameters) {
		Set<Invokable<? super T, ?>> candidates = getMethods().stream()
				.filter(i -> i.getGenericDeclaration().getName().equals(name))
				.collect(Collectors.toSet());

		return resolveInvokableOverload(candidates, parameters);
	}

	private <R, U> Invokable<U, ? extends R> resolveInvokableOverload(
			Set<? extends Invokable<? super U, ? extends R>> candidates,
			Type... parameters) {
		candidates = candidates.stream()
				.map(i -> i.withVariableArityApplicability(parameters))
				.filter(o -> o != null).collect(Collectors.toSet());

		if (candidates.isEmpty())
			throw new IllegalArgumentException();
		else {
			Set<Invokable<? super U, ? extends R>> moreSpecificCandidates = candidates
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

		throw new UnsupportedOperationException();
	}

	public Type getComponentType() {
		throw new UnsupportedOperationException();
	}
}
