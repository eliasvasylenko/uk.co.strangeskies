package uk.co.strangeskies.reflection;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
			Function<Type, InferenceVariable> inferenceVariables = t -> null;
			do {
				resolver.incorporateType(new TypeSubstitution(inferenceVariables)
						.resolve(superClass.getGenericSuperclass()));
				superClass = superClass.getSuperclass();

				Class<?> subClass = superClass;
				inferenceVariables = t -> {
					if (t instanceof TypeVariable)
						return resolver.getInferenceVariable(subClass, (TypeVariable<?>) t);
					else
						return null;
				};

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

		getResolver();
	}

	public static TypeLiteral<?> from(Type type) {
		return new TypeLiteral<>(type, Types.getRawType(type));
	}

	public static <T> TypeLiteral<T> from(Class<T> type) {
		return new TypeLiteral<>(type);
	}

	public static <T> TypeLiteral<T> fromString(String typeString) {
		throw new UnsupportedOperationException(); // TODO
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(rawType.getModifiers());
	}

	public boolean isFinal() {
		return Modifier.isFinal(rawType.getModifiers());
	}

	public boolean isInterface() {
		return Modifier.isInterface(rawType.getModifiers());
	}

	public boolean isPrivate() {
		return Modifier.isPrivate(rawType.getModifiers());
	}

	public boolean isProtected() {
		return Modifier.isProtected(rawType.getModifiers());
	}

	public boolean isPublic() {
		return Modifier.isPublic(rawType.getModifiers());
	}

	public boolean isStatic() {
		return Modifier.isStatic(rawType.getModifiers());
	}

	private Resolver getResolver() {
		if (resolver == null) {
			resolver = new Resolver();
			resolver.incorporateType(type);
		}
		return resolver;
	}

	@Override
	public String toString() {
		return Types.toString(type);
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

	public boolean isAssignableTo(TypeLiteral<?> type) {
		return isAssignableTo(type.getType());
	}

	public boolean isAssignableTo(Type type) {
		return Types.isAssignable(this.type, type);
	}

	public boolean isAssignableFrom(TypeLiteral<?> type) {
		return isAssignableFrom(type.getType());
	}

	public boolean isAssignableFrom(Type type) {
		return Types.isAssignable(type, this.type);
	}

	public List<TypeVariable<?>> getTypeParameters() {
		return Types.getTypeParameters(rawType);
	}

	public Type getTypeArgument(TypeVariable<?> type) {
		return getResolver().resolveTypeVariable(rawType, type);
	}

	@SuppressWarnings("unchecked")
	public <U> TypeLiteral<? extends U> resolveType(TypeLiteral<U> type) {
		return (TypeLiteral<? extends U>) TypeLiteral.from(getResolver()
				.resolveType(type.getType()));
	}

	@SuppressWarnings("unchecked")
	public <U> TypeLiteral<? extends U> resolveSupertypeParameters(Class<U> type) {
		if (Types.getTypeParameters(type).isEmpty())
			return TypeLiteral.from(type);

		if (!type.isAssignableFrom(rawType))
			throw new IllegalArgumentException();

		Class<?> superClass = rawType;
		Type superType = this.type;
		Function<Type, Type> inferenceVariables = t -> {
			if (t instanceof TypeVariable)
				return getResolver().getInferenceVariable(rawType, (TypeVariable<?>) t);
			else
				return null;
		};
		while (!superClass.equals(type)) {
			Set<Type> superTypes = new HashSet<>(Arrays.asList(superClass
					.getGenericInterfaces()));
			if (superClass.getSuperclass() != null)
				superTypes.addAll(Arrays.asList(superClass.getGenericSuperclass()));

			superType = superTypes.stream()
					.filter(t -> type.isAssignableFrom(Types.getRawType(t))).findAny()
					.get();
			superType = new TypeSubstitution(inferenceVariables).resolve(superType);

			getResolver().incorporateType(superType);
			Class<?> subClass = superClass;
			superClass = Types.getRawType(superType);

			inferenceVariables = t -> {
				if (t instanceof TypeVariable)
					return getResolver().getInferenceVariable(subClass,
							(TypeVariable<?>) t);
				else
					return null;
			};
		}

		Type capturedType;
		if (superType instanceof Class)
			capturedType = type;
		else
			capturedType = getResolver().resolveType(type,
					Types.uncheckedParameterizedType(type, new HashMap<>()));

		return (TypeLiteral<? extends U>) TypeLiteral.from(capturedType);
	}

	public <U extends T> TypeLiteral<? extends U> resolveSubtypeParameters(
			Class<U> type) {
		throw new UnsupportedOperationException(); // TODO
	}

	@SuppressWarnings("unchecked")
	public TypeLiteral<? extends T> withTypeArguments(
			Map<TypeVariable<?>, Type> arguments) {
		Resolver resolver = new Resolver(getResolver());

		for (TypeVariable<?> parameter : arguments.keySet())
			resolver.incorporateInstantiation(parameter, arguments.get(parameter));

		return (TypeLiteral<? extends T>) TypeLiteral.from(resolver
				.resolveType(rawType));
	}

	public TypeLiteral<? extends T> withTypeArgument(TypeVariable<?> parameter,
			Type argument) {
		HashMap<TypeVariable<?>, Type> arguments = new HashMap<>();
		arguments.put(parameter, argument);
		return withTypeArguments(arguments);
	}

	public <V> TypeLiteral<? extends T> withTypeArgument(
			TypeParameter<V> parameter, TypeLiteral<V> argument) {
		return withTypeArgument(parameter.getType(), argument.getType());
	}

	public <V> TypeLiteral<? extends T> withTypeArgument(
			TypeParameter<V> parameter, Class<V> argument) {
		return withTypeArgument(parameter.getType(), argument);
	}

	public Set<Invokable<T, T>> getConstructors() {
		return Arrays.stream(getRawType().getConstructors())
				.map(m -> new Invokable<T, T>(this, m)).collect(Collectors.toSet());
	}

	public Set<? extends Invokable<? super T, ?>> getMethods() {
		// TODO include inherited methods.
		return Arrays.stream(getRawType().getMethods())
				.map(m -> new Invokable<>(this, m)).collect(Collectors.toSet());
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
		Set<? extends Invokable<? super U, ? extends R>> compatibleCandidates = candidates
				.stream().map(i -> i.withLooseApplicability(parameters))
				.filter(o -> o != null).collect(Collectors.toSet());

		if (compatibleCandidates.isEmpty()) {
			compatibleCandidates = candidates.stream()
					.map(i -> i.withVariableArityApplicability(parameters))
					.filter(o -> o != null).collect(Collectors.toSet());
		} else {
			candidates = compatibleCandidates;
			compatibleCandidates = compatibleCandidates.stream()
					.map(i -> i.withStrictApplicability(parameters))
					.filter(o -> o != null).collect(Collectors.toSet());
			if (compatibleCandidates.isEmpty())
				compatibleCandidates = candidates;
		}

		throw new UnsupportedOperationException(); // TODO
	}

	public Type getComponentType() {
		return Types.getComponentType(type);
	}
}
