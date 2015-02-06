package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Set;

public interface TypeLiteral<T> {
	public abstract boolean isAbstract();

	public abstract boolean isFinal();

	public abstract boolean isInterface();

	public abstract boolean isPrivate();

	public abstract boolean isProtected();

	public abstract boolean isPublic();

	public abstract boolean isStatic();

	public abstract Resolver getResolver();

	public abstract String toString();

	public abstract Type getType();

	@SuppressWarnings("unchecked")
	public default Class<? super T> getRawType() {
		return (Class<? super T>) Types.getRawType(getType());
	}

	public default Set<Class<?>> getRawTypes() {
		return Types.getRawTypes(getType());
	}

	public abstract Class<? super T> getGenericDeclaration();

	public abstract Type getDeclaringType();

	public default boolean isPrimitive() {
		return Types.isPrimitive(getType());
	}

	public default boolean isPrimitiveWrapper() {
		return Types.isPrimitiveWrapper(getType());
	}

	@SuppressWarnings("unchecked")
	public default TypeLiteral<T> wrap() {
		if (isPrimitive())
			return (TypeLiteral<T>) ParameterizedTypeLiteral.from(Types
					.wrap(getType()));
		else
			return this;
	}

	@SuppressWarnings("unchecked")
	public default TypeLiteral<T> unwrap() {
		if (isPrimitiveWrapper())
			return (TypeLiteral<T>) ParameterizedTypeLiteral.from(Types
					.unwrap(getType()));
		else
			return this;
	}

	public abstract boolean isAssignableTo(TypeLiteral<?> type);

	public abstract boolean isAssignableTo(Type type);

	public abstract boolean isAssignableFrom(TypeLiteral<?> type);

	public abstract boolean isAssignableFrom(Type type);

	public abstract List<TypeVariable<?>> getTypeParameters();

	public abstract Type getTypeArgument(TypeVariable<?> type);

	public abstract <U> TypeLiteral<? extends U> resolveType(TypeLiteral<U> type);

	public abstract <U> TypeLiteral<? extends U> resolveSupertypeParameters(
			Class<U> superclass);

	public abstract <U> TypeLiteral<? extends U> resolveSubtypeParameters(
			Class<U> subclass);

	public abstract <V> TypeLiteral<T> withTypeArgument(
			TypeParameter<V> parameter, TypeLiteral<V> argument);

	public abstract <V> TypeLiteral<T> withTypeArgument(
			TypeParameter<V> parameter, Class<V> argument);

	public abstract Set<Invokable<T, T>> getConstructors();

	public abstract Set<Invokable<T, ?>> getMethods();

	public abstract Set<? extends Invokable<? super T, ?>> getInvokables();

	public abstract Invokable<T, T> resolveConstructorOverload(Type... parameters);

	public abstract Invokable<T, T> resolveConstructorOverload(
			List<? extends Type> parameters);

	public abstract Invokable<T, ?> resolveMethodOverload(String name,
			Type... parameters);

	public abstract Invokable<T, ?> resolveMethodOverload(String name,
			List<? extends Type> parameters);

	public abstract Type getComponentType();
}
