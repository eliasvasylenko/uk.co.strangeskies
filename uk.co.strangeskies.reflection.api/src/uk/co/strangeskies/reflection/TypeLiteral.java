package uk.co.strangeskies.reflection;

import java.lang.reflect.Executable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;

public class TypeLiteral<T> {
	private final Type type;
	private final Class<? super T> rawType;

	private final Resolver resolver;

	@SuppressWarnings("unchecked")
	protected TypeLiteral() {
		Type superType = getClass();

		do {
			if (superType instanceof Class)
				superType = ((Class<?>) superType).getGenericSuperclass();
			else
				;// superType = ((ParameterizedType) superType).get
		} while (false);

		type = ((ParameterizedType) superType).getActualTypeArguments()[0];
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

	public static TypeLiteral<?> of(Type type) {
		return new TypeLiteral<>(type, Types.getRawType(type));
	}

	public Type getType() {
		return type;
	}

	public Class<? super T> getRawType() {
		return rawType;
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
			return (TypeLiteral<T>) of(Types.wrap(rawType));
		else
			return this;
	}

	@SuppressWarnings("unchecked")
	public TypeLiteral<T> unwrap() {
		if (isPrimitiveWrapper())
			return (TypeLiteral<T>) of(Types.unwrap(rawType));
		else
			return this;
	}

	public <U> TypeLiteral<? extends U> resolveType(TypeLiteral<U> type) {
		return null;
	}

	public Type resolveType(Type type) {
		if (type instanceof TypeVariable) {
			if (typeMappings.containsKey(type))
				return typeMappings.get(type);

			TypeVariable<?> typeVariable = (TypeVariable<?>) type;
			if (typeVariable.getGenericDeclaration() instanceof Executable)
				return type;

			Class<?> declaringClass = (Class<?>) typeVariable.getGenericDeclaration();
			if (!declaringClass.isAssignableFrom(rawType))
				return type;

			return null;
		} else {
			TypeLiteral<?> ofLiteral = TypeLiteral.of(type);

			;
		}
	}

	public TypeLiteral<? extends T> withTypeArguments(
			Map<TypeVariable<? extends Class<?>>, Type> instantiations) {
		BoundSet bounds = new BoundSet(this.bounds);
		for (TypeVariable<?> typeVariable : instantiations.keySet())
			bounds.incorporate(new ConstraintFormula(Kind.CONTAINMENT, typeVariable,
					instantiations.get(typeVariable)));

		Deque<Type> supertypeStack;
		new TypeVisitor() {

		}.visit(type);
	}

	@SuppressWarnings("serial")
	public TypeLiteral<? extends T> withTypeArgument(
			TypeVariable<? extends Class<?>> typeVariable, Type instantiation) {
		return withTypeArguments(new HashMap<TypeVariable<? extends Class<?>>, Type>() {
			{
				put(typeVariable, instantiation);
			}
		});
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
				.map(m -> new Invokable<>(this, of(Object.class), m))
				.collect(Collectors.toSet());
	}

	public Set<Invokable<T, ?>> getInvokables() {
		Set<Invokable<T, ?>> invokables = new HashSet<>();
		invokables.addAll(getConstructors());
		invokables.addAll(getMethods());
		return invokables;
	}

	public Invokable<T, T> resolveConstructorOverload(Type... parameters) {
		return resolveInvokableOverload(getConstructors(), parameters);
	}

	/*-
	public Invokable<T, ?> resolveMethodOverload(String name, Type... parameters) {
		return resolveInvokableOverload(
				getMethods().stream()
						.filter(i -> i.getExecutable().getName().equals(name))
						.collect(Collectors.toSet()), parameters);
	}*/

	private <R> Invokable<T, R> resolveInvokableOverload(
			Set<Invokable<T, R>> candidates, Type... parameters) {
		Set<Invokable<T, R>> applicableCandidates = candidates.stream()
				.map(i -> i.withStrictApplicability(parameters)).filter(o -> o != null)
				.collect(Collectors.toSet());

		if (applicableCandidates.isEmpty())
			applicableCandidates = candidates.stream()
					.map(i -> i.withLooseApplicability(parameters))
					.filter(o -> o != null).collect(Collectors.toSet());

		if (applicableCandidates.isEmpty())
			applicableCandidates = candidates.stream()
					.map(i -> i.withVariableArityApplicability(parameters))
					.filter(o -> o != null).collect(Collectors.toSet());

		return null;
	}
}
