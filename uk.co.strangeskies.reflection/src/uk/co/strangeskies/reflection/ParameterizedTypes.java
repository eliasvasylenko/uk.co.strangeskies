/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.utilities.EqualityComparator;
import uk.co.strangeskies.utilities.Isomorphism;
import uk.co.strangeskies.utilities.collection.MultiMap;
import uk.co.strangeskies.utilities.collection.MultiTreeMap;

/**
 * A collection of utility methods relating to parameterised types.
 * 
 * @author Elias N Vasylenko
 */
public class ParameterizedTypes {
	static final class ParameterizedTypeImpl implements ParameterizedType, Serializable {
		private static final long serialVersionUID = 1L;

		private final Type ownerType;
		private final List<Type> typeArguments;
		private final Class<?> rawType;

		private final Map<Thread, MultiMap<ParameterizedType, ParameterizedType, Set<ParameterizedType>>> assumedEqualities;

		private Integer hashCode;

		ParameterizedTypeImpl(Type ownerType, Class<?> rawType, List<Type> typeArguments) {
			this.ownerType = ownerType;
			this.rawType = rawType;
			this.typeArguments = typeArguments;

			int i = 0;
			for (Type argument : typeArguments) {
				if (argument instanceof IntersectionType) {
					IntersectionType intersectionType = (IntersectionType) argument;
					if (intersectionType.getTypes().length == 0)
						typeArguments.set(i, Object.class);
					else if (intersectionType.getTypes().length == 1)
						typeArguments.set(i, intersectionType.getTypes()[0]);
				}
				i++;
			}

			assumedEqualities = new IdentityHashMap<>();
		}

		ParameterizedTypeImpl(ParameterizedType type) {
			this(getOwner(type.getOwnerType()), (Class<?>) type.getRawType(), getArguments(type.getActualTypeArguments()));
		}

		private static Type getOwner(Type ownerType) {
			return ownerType instanceof ParameterizedType ? new ParameterizedTypeImpl((ParameterizedType) ownerType)
					: ownerType;
		}

		private static List<Type> getArguments(Type[] actualTypeArguments) {
			List<Type> arguments = new ArrayList<>(actualTypeArguments.length);
			for (Type argument : actualTypeArguments)
				arguments.add(argument);
			return arguments;
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
			return toString(Imports.empty());
		}

		public synchronized String toString(Imports imports) {
			return ParameterizedTypes.toString(this, imports);
		}

		@Override
		public synchronized int hashCode() {
			if (hashCode == null) {
				/*
				 * This way the hash code will return 0 if we encounter it again in the
				 * parameters, rather than recurring infinitely:
				 * 
				 * (this is not a problem for other threads as hashCode is synchronized)
				 */
				hashCode = 0;

				/*
				 * Calculate the hash code properly, now we're guarded against
				 * recursion:
				 */
				this.hashCode = (ownerType == null ? 0 : ownerType.hashCode()) ^ (rawType == null ? 0 : rawType.hashCode())
						^ typeArguments.hashCode();
			}

			return hashCode;
		}

		@Override
		public boolean equals(Object other) {
			boolean equals;

			if (other == this)
				equals = true;
			else if (other instanceof IntersectionType)
				equals = other.equals(this);
			else if (other == null || !(other instanceof ParameterizedType))
				equals = false;
			else if (other instanceof ParameterizedTypeImpl && other.hashCode() != hashCode())
				equals = false;
			else {
				Thread currentThread = Thread.currentThread();
				boolean newThread = false;

				MultiMap<ParameterizedType, ParameterizedType, Set<ParameterizedType>> equalitiesForThread = assumedEqualities
						.get(currentThread);
				if (equalitiesForThread == null) {
					assumedEqualities.put(currentThread,
							equalitiesForThread = new MultiTreeMap<>(EqualityComparator.identityComparator(),
									() -> new TreeSet<>(EqualityComparator.identityComparator())));
					newThread = true;
				}

				ParameterizedType that = (ParameterizedType) other;

				if (equalitiesForThread.contains(this, that) || equalitiesForThread.contains(that, this))
					equals = true;
				else {
					equalitiesForThread.add(this, that);
					equalitiesForThread.add(that, this);

					equals = getRawType().equals(that.getRawType()) && Objects.equals(getOwnerType(), that.getOwnerType())
							&& Arrays.equals(getActualTypeArguments(), that.getActualTypeArguments());

					equalitiesForThread.remove(this, that);
					equalitiesForThread.remove(that, this);
				}

				if (newThread)
					assumedEqualities.remove(currentThread);
			}

			return equals;
		}

		public boolean equals2(Object other) {
			if (!(other instanceof Type))
				return false;
			else
				return Types.equals(this, (Type) other);
		}
	}

	private ParameterizedTypes() {}

	public static String toString(ParameterizedType parameterizedType, Imports imports) {
		return toString(parameterizedType, imports, new Isomorphism());
	}

	public static String toString(ParameterizedType parameterizedType, Imports imports, Isomorphism isomorphism) {
		return isomorphism.byIdentity().getPartialMapping(parameterizedType, (p, partial) -> {
			/*
			 * This way the string will return "..." if we encounter it again in the
			 * parameters, rather than recurring infinitely:
			 */
			partial.accept(() -> "...");

			Type ownerType = p.getOwnerType();
			Type rawType = p.getRawType();
			Type[] typeArguments = p.getActualTypeArguments();

			/*
			 * Calculate the string properly, now we're guarded against recursion:
			 */
			StringBuilder builder = new StringBuilder();
			if (ownerType == null) {
				builder.append(Types.toString(rawType, imports, isomorphism));
			} else {
				builder.append(Types.toString(ownerType, imports, isomorphism)).append(".");

				if (ownerType instanceof ParameterizedType) {
					String rawTypeName = rawType.getTypeName();
					int index = rawTypeName.indexOf('$');
					if (index > 0) {
						rawTypeName = rawTypeName.substring(index + 1);
					}
					builder.append(rawTypeName);
				} else {
					builder.append(Types.toString(rawType, imports, isomorphism));
				}
			}

			builder.append('<');

			builder.append(Arrays.stream(typeArguments).map(t -> Types.toString(t, imports, isomorphism))
					.collect(Collectors.joining(", ")));

			return builder.append(">").toString();
		});
	}

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
			typeParameters = Stream.concat(typeParameters, Arrays.stream(rawType.getTypeParameters()));
		} while (!Types.isStatic(rawType) && (rawType = rawType.getEnclosingClass()) != null);

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
	public static Map<TypeVariable<?>, Type> getAllTypeArgumentsMap(ParameterizedType type) {
		Map<TypeVariable<?>, Type> typeArguments = new HashMap<>();

		Class<?> rawType = (Class<?>) type.getRawType();
		do {
			for (int i = 0; i < rawType.getTypeParameters().length; i++) {
				TypeVariable<?> typeParameter = rawType.getTypeParameters()[i];
				Type typeArgument = type.getActualTypeArguments()[i];

				typeArguments.put(typeParameter, typeArgument);
			}

			type = type.getOwnerType() instanceof ParameterizedType ? (ParameterizedType) type.getOwnerType() : null;
			rawType = Types.isStatic(rawType) ? null : rawType.getEnclosingClass();

			if (rawType != null && type == null) {
				do {
					for (TypeVariable<?> variable : rawType.getTypeParameters())
						typeArguments.put(variable, variable);
				} while ((rawType = Types.isStatic(rawType) ? null : rawType.getEnclosingClass()) != null);
			}
		} while (type != null && rawType != null);

		return typeArguments;
	}

	/**
	 * For a given parameterized type, we retrieve a list of all its type
	 * arguments arguments in the order the respective parameters are given by
	 * {@link #getAllTypeParameters(Class)}.
	 *
	 * @param type
	 *          The type whose generic type arguments we wish to determine.
	 * @return A mapping of all type variables to their arguments in the context
	 *         of the given type.
	 */
	public static List<Type> getAllTypeArgumentsList(ParameterizedType type) {
		List<Type> typeArguments = new ArrayList<>();

		Class<?> rawType = (Class<?>) type.getRawType();
		do {
			for (int i = 0; i < rawType.getTypeParameters().length; i++) {
				Type typeArgument = type.getActualTypeArguments()[i];

				typeArguments.add(typeArgument);
			}

			type = type.getOwnerType() instanceof ParameterizedType ? (ParameterizedType) type.getOwnerType() : null;
			rawType = Types.isStatic(rawType) ? null : rawType.getEnclosingClass();

			if (rawType != null && type == null) {
				do {
					for (TypeVariable<?> variable : rawType.getTypeParameters())
						typeArguments.add(variable);
				} while ((rawType = Types.isStatic(rawType) ? null : rawType.getEnclosingClass()) != null);
			}
		} while (type != null && rawType != null);

		return typeArguments;
	}

	static ParameterizedType uncheckedFrom(Type ownerType, Class<?> rawType, List<Type> typeArguments) {
		return new ParameterizedTypeImpl(ownerType, rawType, new ArrayList<>(typeArguments));
	}

	static <T> Type uncheckedFrom(Class<T> rawType, Function<? super TypeVariable<?>, ? extends Type> typeArguments) {
		Class<?> enclosing = rawType.getEnclosingClass();
		Type ownerType;
		if (enclosing == null || Types.isStatic(rawType))
			ownerType = enclosing;
		else
			ownerType = uncheckedFrom(enclosing, typeArguments);

		if ((ownerType == null || ownerType instanceof Class) && rawType.getTypeParameters().length == 0)
			return rawType;

		return new ParameterizedTypeImpl(ownerType, rawType, argumentsForClass(rawType, typeArguments));
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
		return (TypeToken<? extends T>) TypeToken.over(uncheckedFrom(rawType, typeArguments));
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
	public static <T> TypeToken<? extends T> from(Class<T> rawType, Type... typeArguments) {
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
	public static <T> TypeToken<? extends T> from(Class<T> rawType, List<Type> typeArguments) {
		if (!Types.isStatic(rawType) && rawType.getEnclosingClass() != null && isGeneric(rawType.getEnclosingClass()))
			throw new IllegalArgumentException();

		return (TypeToken<? extends T>) TypeToken.over(uncheckedFrom(null, rawType, typeArguments));
	}

	private static List<Type> argumentsForClass(Class<?> rawType,
			Function<? super TypeVariable<?>, ? extends Type> typeArguments) {
		List<Type> arguments = new ArrayList<>();
		for (int i = 0; i < rawType.getTypeParameters().length; i++) {
			Type argument = typeArguments.apply(rawType.getTypeParameters()[i]);
			arguments.add((argument != null) ? argument : rawType.getTypeParameters()[i]);
		}
		return arguments;
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
			throw new IllegalArgumentException("Unexpected class '" + type.getClass() + "' of type '" + type + "'.");

		do {
			Set<Type> lesserSubtypes = new HashSet<>(Arrays.asList(subclass.getGenericInterfaces()));
			if (subclass.getSuperclass() != null)
				lesserSubtypes.addAll(Arrays.asList(subclass.getGenericSuperclass()));
			if (lesserSubtypes.isEmpty())
				lesserSubtypes.add(Object.class);

			Type subtype = lesserSubtypes.stream().filter(t -> Types.isAssignable(Types.getRawType(t), superclass)).findAny()
					.get();

			if (type instanceof ParameterizedType)
				type = new TypeSubstitution(getAllTypeArgumentsMap((ParameterizedType) type)).resolve(subtype);
			else
				type = subtype;

			subclass = Types.getRawType(type);
		} while (!subclass.equals(superclass));

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
			throw new IllegalArgumentException("Unexpected class '" + type.getClass() + "' of type '" + type + "'.");

		Map<TypeVariable<?>, InferenceVariable> parameterSubstitutes = new HashMap<>();
		Map<InferenceVariable, Type> substitutedArguments = new HashMap<>();

		int index = 0;
		if (type instanceof ParameterizedType) {
			Map<TypeVariable<?>, Type> arguments = getAllTypeArgumentsMap((ParameterizedType) type);
			for (TypeVariable<?> parameter : getAllTypeParameters(rawType)) {
				InferenceVariable substituteArgument = getSubstitutionArgument(index++);
				parameterSubstitutes.put(parameter, substituteArgument);
				substitutedArguments.put(substituteArgument, arguments.get(parameter));
			}
		}

		Type supertype = new TypeSubstitution(substitutedArguments)
				.resolve(from(rawType, parameterSubstitutes::get).resolveSubtypeParameters(subclass).getType());

		return supertype;
	}
}
