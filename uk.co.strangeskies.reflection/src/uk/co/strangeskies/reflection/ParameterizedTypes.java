/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.utilities.IdentityComparator;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiTreeMap;

/**
 * A collection of utility methods relating to parameterised types.
 * 
 * @author Elias N Vasylenko
 */
public class ParameterizedTypes {
	private ParameterizedTypes() {}

	/**
	 * Determine whether a {@link Class} represents a generic type.
	 * 
	 * @param type
	 *          The type we wish to classify.
	 * @return True if the given class is generic or if a non-statically enclosing
	 *         class is generic, false otherwise.
	 */
	public static boolean isGeneric(Class<?> type) {
		return !getAllTypeParameters(type).isEmpty();
	}

	/**
	 * This method retrieves a list of all type variables present on the given raw
	 * type, as well as all type variables on any enclosing types recursively, in
	 * the order encountered.
	 *
	 * @param rawType
	 *          The class whose generic type parameters we wish to determine.
	 * @return A list of all relevant type variables.
	 */
	public static List<TypeVariable<?>> getAllTypeParameters(Class<?> rawType) {
		Stream<TypeVariable<?>> typeParameters = Stream.empty();
		do {
			typeParameters = Stream.concat(typeParameters,
					Arrays.stream(rawType.getTypeParameters()));
		} while (!Types.isStatic(rawType)
				&& (rawType = rawType.getEnclosingClass()) != null);
		return typeParameters.collect(Collectors.toList());
	}

	/**
	 * For a given parameterized type, we retrieve a mapping of all type variables
	 * on its raw type, as given by {@link #getAllTypeParameters(Class)}, to their
	 * arguments within the context of this type.
	 *
	 * @param type
	 *          The type whose generic type arguments we wish to determine.
	 * @return A mapping of all type variables to their arguments in the context
	 *         of the given type.
	 */
	public static Map<TypeVariable<?>, Type> getAllTypeArguments(
			ParameterizedType type) {
		Map<TypeVariable<?>, Type> typeArguments = new HashMap<>();

		Class<?> rawType = Types.getRawType(type);
		do {
			for (int i = 0; i < rawType.getTypeParameters().length; i++) {
				TypeVariable<?> typeParameter = rawType.getTypeParameters()[i];
				Type typeArgument = type.getActualTypeArguments()[i];

				typeArguments.put(typeParameter, typeArgument);
			}

			type = type.getOwnerType() instanceof ParameterizedType ? (ParameterizedType) type
					.getOwnerType() : null;
			rawType = Types.isStatic(rawType) ? null : rawType.getEnclosingClass();
		} while (type != null && rawType != null);

		return typeArguments;
	}

	/**
	 * @param source
	 *          A supplier of the parameterized class we wish to proxy.
	 * @return A proxy for a parameterized type, forwarding to the instance
	 *         provided by the given supplier at the moment of each invocation.
	 *         This is generally useful for algorithms which deal with infinite
	 *         types.
	 */
	public static ParameterizedType proxy(Supplier<ParameterizedType> source) {
		return new ParameterizedType() {
			@Override
			public Type getRawType() {
				return source.get().getRawType();
			}

			@Override
			public Type getOwnerType() {
				return source.get().getOwnerType();
			}

			@Override
			public Type[] getActualTypeArguments() {
				return source.get().getActualTypeArguments();
			}

			@Override
			public String toString() {
				return source.get().toString();
			}

			@Override
			public boolean equals(Object arg0) {
				return source.get().equals(arg0);
			}

			@Override
			public int hashCode() {
				return source.get().hashCode();
			}
		};
	}

	static ParameterizedType uncheckedFrom(Type ownerType, Class<?> rawType,
			List<Type> typeArguments) {
		return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
	}

	static <T> Type uncheckedFrom(Class<T> rawType,
			Function<? super TypeVariable<?>, ? extends Type> typeArguments) {
		Class<?> enclosing = rawType.getEnclosingClass();
		Type ownerType;
		if (enclosing == null || Types.isStatic(rawType))
			ownerType = enclosing;
		else
			ownerType = uncheckedFrom(enclosing, typeArguments);

		if ((ownerType == null || ownerType instanceof Class)
				&& rawType.getTypeParameters().length == 0)
			return rawType;

		return new ParameterizedTypeImpl(ownerType, rawType, argumentsForClass(
				rawType, typeArguments));
	}

	/**
	 * Derive an instance of {@link ParameterizedType} from a raw {@link Class},
	 * substituting the type parameters of that class as their own argument
	 * instantiations.
	 * 
	 * @param <T>
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}.
	 * @param rawType
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}.
	 * @return A {@link ParameterizedType} instance over the given class.
	 */
	public static <T> TypeToken<? extends T> from(Class<T> rawType) {
		return from(rawType, i -> null);
	}

	/**
	 * Derive an instance of {@link ParameterizedType} from a raw {@link Class}
	 * using the given generic type arguments. Type parameters with no provided
	 * argument will be parameterized with the type variables themselves.
	 * 
	 * @param <T>
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}.
	 * @param rawType
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}.
	 * @param typeArguments
	 *          A mapping of generic type variables to arguments.
	 * @return A {@link ParameterizedType} instance over the given class,
	 *         parameterized with the given type arguments.
	 */
	@SuppressWarnings("unchecked")
	public static <T> TypeToken<? extends T> from(Class<T> rawType,
			Function<? super TypeVariable<?>, ? extends Type> typeArguments) {
		return (TypeToken<? extends T>) TypeToken.over(uncheckedFrom(rawType,
				typeArguments));
	}

	/**
	 * Derive an instance of {@link ParameterizedType} from a raw {@link Class}
	 * using the given generic type arguments, in the order given.
	 * 
	 * @param <T>
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}. The raw type must not have a non
	 *          statically enclosing type which is itself generic.
	 * @param rawType
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}. The raw type must not have a non
	 *          statically enclosing type which is itself generic.
	 * @param typeArguments
	 *          A list of {@link Type}s to substitute as type arguments for the
	 *          given generic class. There should be exactly as many type
	 *          arguments as there are type parameters for the given class.
	 * @return A {@link ParameterizedType} instance over the given class,
	 *         parameterized with the given type arguments, in order
	 */
	public static <T> TypeToken<? extends T> from(Class<T> rawType,
			Type... typeArguments) {
		return from(rawType, Arrays.asList(typeArguments));
	}

	/**
	 * Derive an instance of {@link ParameterizedType} from a raw {@link Class}
	 * using the given generic type arguments, in the order given.
	 * 
	 * @param <T>
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}. The raw type must not have a non
	 *          statically enclosing type which is itself generic.
	 * @param rawType
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}. The raw type must not have a non
	 *          statically enclosing type which is itself generic.
	 * @param typeArguments
	 *          A list of {@link Type}s to substitute as type arguments for the
	 *          given generic class. There should be exactly as many type
	 *          arguments as there are type parameters for the given class.
	 * @return A {@link ParameterizedType} instance over the given class,
	 *         parameterized with the given type arguments, in order
	 */
	@SuppressWarnings("unchecked")
	public static <T> TypeToken<? extends T> from(Class<T> rawType,
			List<Type> typeArguments) {
		if (!Types.isStatic(rawType) && rawType.getEnclosingClass() != null
				&& isGeneric(rawType.getEnclosingClass()))
			throw new IllegalArgumentException();

		return (TypeToken<? extends T>) TypeToken.over(uncheckedFrom(null, rawType,
				typeArguments));
	}

	private static List<Type> argumentsForClass(Class<?> rawType,
			Function<? super TypeVariable<?>, ? extends Type> typeArguments) {
		List<Type> arguments = new ArrayList<>();
		for (int i = 0; i < rawType.getTypeParameters().length; i++) {
			Type argument = typeArguments.apply(rawType.getTypeParameters()[i]);
			arguments.add((argument != null) ? argument
					: rawType.getTypeParameters()[i]);
		}
		return arguments;
	}

	private static final class ParameterizedTypeImpl implements
			ParameterizedType, Serializable {
		private static final long serialVersionUID = 1L;

		private final Type ownerType;
		private final List<Type> typeArguments;
		private final Class<?> rawType;

		private final Set<Thread> recurringThreads;
		private final Map<Thread, MultiMap<ParameterizedType, ParameterizedType, Set<ParameterizedType>>> assumedEqualities;

		ParameterizedTypeImpl(Type ownerType, Class<?> rawType,
				List<Type> typeArguments) {
			this.ownerType = ownerType;
			this.rawType = rawType;
			this.typeArguments = typeArguments;

			recurringThreads = new HashSet<>();
			assumedEqualities = new HashMap<>();
		}

		@Override
		public Type[] getActualTypeArguments() {
			return typeArguments.toArray(new Type[typeArguments.size()]);
		}

		@Override
		public Type getRawType() {
			return rawType;
		}

		@Override
		public Type getOwnerType() {
			return ownerType;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if (ownerType != null) {
				builder.append(ownerType.getTypeName()).append(".");
				builder.append(rawType.getSimpleName());
			} else
				builder.append(rawType.getTypeName());

			builder.append('<');

			Thread currentThread = Thread.currentThread();
			if (recurringThreads.add(currentThread)) {
				builder.append(typeArguments.stream().map(Types::toString)
						.collect(Collectors.joining(", ")));

				recurringThreads.remove(currentThread);
			} else
				builder.append("...");

			builder.append('>');

			return builder.toString();
		}

		@Override
		public int hashCode() {
			Thread currentThread = Thread.currentThread();
			if (recurringThreads.add(currentThread)) {
				/*
				 * If we have not encountered this type so far in calculating this hash
				 * code:
				 */
				int hashCode = (ownerType == null ? 0 : ownerType.hashCode())
						^ (rawType == null ? 0 : rawType.hashCode())
						^ typeArguments.hashCode();

				recurringThreads.remove(currentThread);

				return hashCode;
			} else
				return 0;
		}

		@Override
		public boolean equals(Object other) {
			Thread currentThread = Thread.currentThread();
			boolean newThread = false;

			MultiMap<ParameterizedType, ParameterizedType, Set<ParameterizedType>> equalitiesForThread = assumedEqualities
					.get(currentThread);
			if (equalitiesForThread == null) {
				assumedEqualities.put(currentThread,
						equalitiesForThread = new MultiTreeMap<>(
								new IdentityComparator<>(), () -> new TreeSet<>(
										new IdentityComparator<>())));
				newThread = true;
			}

			boolean equals;

			if (other == this)
				equals = true;
			else if (other instanceof IntersectionType)
				equals = other.equals(this);
			else if (other == null || !(other instanceof ParameterizedType))
				equals = false;
			else {
				ParameterizedType that = (ParameterizedType) other;

				if (equalitiesForThread.contains(this, that))
					equals = true;
				else {
					equalitiesForThread.add(this, that);
					equalitiesForThread.add(that, this);

					equals = getRawType().equals(that.getRawType())
							&& Objects.equals(getOwnerType(), that.getOwnerType())
							&& Arrays.equals(getActualTypeArguments(),
									that.getActualTypeArguments());

					equalitiesForThread.remove(this, that);
					equalitiesForThread.remove(that, this);
				}
			}

			if (newThread)
				assumedEqualities.remove(currentThread);

			return equals;
		}
	}

	private static final List<InferenceVariable> SUBSTITUTE_ARGUMENTS = new ArrayList<>();

	private static InferenceVariable getSubstitutionArgument(int index) {
		while (index >= SUBSTITUTE_ARGUMENTS.size()) {
			String name = "SUB#" + SUBSTITUTE_ARGUMENTS.size();
			SUBSTITUTE_ARGUMENTS.add(new InferenceVariable() {
				@Override
				public String toString() {
					return name;
				}
			});
		}
		return SUBSTITUTE_ARGUMENTS.get(index);
	}

	/**
	 * For a given generic superclass of a given exact type, determine the type
	 * arguments of the exact supertype.
	 * 
	 * @param type
	 *          The type providing a context within which to determine the
	 *          arguments of the supertype.
	 * @param superclass
	 *          The class of the supertype parameterization we wish to determine.
	 * @return A TypeToken over the supertype of the requested class.
	 */
	public static Type resolveSupertypeParameters(Type type, Class<?> superclass) {
		if (!isGeneric(superclass))
			return superclass;

		Class<?> subclass = Types.getRawType(type);

		if (subclass.equals(superclass))
			return type;

		if (!(type instanceof ParameterizedType) && !(type instanceof Class))
			throw new IllegalArgumentException("Unexpected class '" + type.getClass()
					+ "' of type '" + type + "'.");

		do {
			Set<Type> lesserSubtypes = new HashSet<>(Arrays.asList(subclass
					.getGenericInterfaces()));
			if (subclass.getSuperclass() != null)
				lesserSubtypes.addAll(Arrays.asList(subclass.getGenericSuperclass()));
			if (lesserSubtypes.isEmpty())
				lesserSubtypes.add(Object.class);

			Type subtype = lesserSubtypes.stream()
					.filter(t -> Types.isAssignable(Types.getRawType(t), superclass))
					.findAny().get();

			if (type instanceof ParameterizedType)
				type = new TypeSubstitution(
						getAllTypeArguments((ParameterizedType) type)).resolve(subtype);
			else
				type = subtype;

			subclass = Types.getRawType(type);
		} while (subclass != superclass);

		return type;
	}

	/**
	 * Attempt to determine the type arguments with which a subtype of a given
	 * class would be parameterized such that it be a valid subtype. This may not
	 * always be possible, but for certain subtype relations it will, based on the
	 * reduction and incorporation rules of the Java type inference algorithm.
	 * 
	 * @param type
	 *          The type providing a context within which to determine the
	 *          arguments of the subtype.
	 * @param subclass
	 *          The class of the subtype parameterization we wish to determine.
	 * @return A TypeToken over the best effort parameterization of the requested
	 *         class such that it be a subtype.
	 */
	public static Type resolveSubtypeParameters(Type type, Class<?> subclass) {
		if (!isGeneric(subclass))
			return subclass;

		Class<?> rawType = Types.getRawType(type);

		if (rawType.equals(subclass))
			return type;

		if (!(type instanceof ParameterizedType) && !(type instanceof Class))
			throw new IllegalArgumentException("Unexpected class '" + type.getClass()
					+ "' of type '" + type + "'.");

		Map<TypeVariable<?>, InferenceVariable> parameterSubstitutes = new HashMap<>();
		Map<InferenceVariable, Type> substitutedArguments = new HashMap<>();

		int index = 0;
		if (type instanceof ParameterizedType) {
			Map<TypeVariable<?>, Type> arguments = getAllTypeArguments((ParameterizedType) type);
			for (TypeVariable<?> parameter : getAllTypeParameters(rawType)) {
				InferenceVariable substituteArgument = getSubstitutionArgument(index++);
				parameterSubstitutes.put(parameter, substituteArgument);
				substitutedArguments.put(substituteArgument, arguments.get(parameter));
			}
		}

		Type supertype = new TypeSubstitution(substitutedArguments).resolve(from(
				rawType, parameterSubstitutes::get).resolveSubtypeParameters(subclass)
				.getType());

		return supertype;
	}
}
