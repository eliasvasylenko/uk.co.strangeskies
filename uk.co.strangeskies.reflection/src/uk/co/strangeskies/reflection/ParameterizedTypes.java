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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.utilities.Property;

public class ParameterizedTypes {
	private ParameterizedTypes() {}

	public static boolean isGeneric(Class<?> type) {
		return !getAllTypeParameters(type).isEmpty();
	}

	/**
	 * This method retrieves a list of all type variables present on the given raw
	 * type, as well as all type variables on any enclosing types recursively, in
	 * the order encountered.
	 *
	 * @param rawType
	 * @return
	 */
	public static List<TypeVariable<?>> getAllTypeParameters(Class<?> rawType) {
		Stream<TypeVariable<?>> typeParameters = Stream.empty();
		do {
			typeParameters = Stream.concat(typeParameters,
					Arrays.stream(rawType.getTypeParameters()));
		} while ((rawType = rawType.getEnclosingClass()) != null);
		return typeParameters.collect(Collectors.toList());
	}

	/**
	 * For a given parameterized type, we retrieve a mapping of all type variables
	 * on its raw type, as given by {@link Types#getAllTypeParameters(Class)}, to
	 * their arguments within the context of this type.
	 *
	 * @param type
	 * @return
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
			rawType = rawType.getEnclosingClass();
		} while (type != null && rawType != null);

		return typeArguments;
	}

	static ParameterizedType uncheckedFrom(Type ownerType, Class<?> rawType,
			List<Type> typeArguments) {
		return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
	}

	static <T> Type uncheckedFrom(Class<T> rawType,
			Map<? extends TypeVariable<?>, ? extends Type> typeArguments) {
		Type ownerType = (rawType.getEnclosingClass() == null) ? null
				: uncheckedFrom(rawType.getEnclosingClass(), typeArguments);

		if ((ownerType == null || ownerType instanceof Class)
				&& rawType.getTypeParameters().length == 0)
			return rawType;

		return new ParameterizedTypeImpl(ownerType, rawType, argumentsForClass(
				rawType, typeArguments));
	}

	public static <T> TypeLiteral<? extends T> from(Class<T> rawType) {
		return from(rawType, new HashMap<>());
	}

	@SuppressWarnings("unchecked")
	public static <T> TypeLiteral<? extends T> from(Class<T> rawType,
			Map<? extends TypeVariable<?>, ? extends Type> typeArguments) {
		return (TypeLiteral<? extends T>) TypeLiteral.from(uncheckedFrom(rawType,
				typeArguments));
	}

	public static <T> TypeLiteral<? extends T> from(Class<T> rawType,
			Type... typeArguments) {
		return from(rawType, Arrays.asList(typeArguments));
	}

	@SuppressWarnings("unchecked")
	public static <T> TypeLiteral<? extends T> from(Class<T> rawType,
			List<Type> typeArguments) {
		if (rawType.getEnclosingClass() != null
				&& isGeneric(rawType.getEnclosingClass()))
			throw new IllegalArgumentException();

		return (TypeLiteral<? extends T>) TypeLiteral.from(uncheckedFrom(null,
				rawType, typeArguments));
	}

	private static List<Type> argumentsForClass(Class<?> rawType,
			Map<? extends TypeVariable<?>, ? extends Type> typeArguments) {
		List<Type> arguments = new ArrayList<>();
		for (int i = 0; i < rawType.getTypeParameters().length; i++) {
			Type argument = typeArguments.get(rawType.getTypeParameters()[i]);
			arguments.add((argument != null) ? argument
					: rawType.getTypeParameters()[i]);
		}
		return arguments;
	}

	public static ParameterizedType proxy(Property<?, ParameterizedType> source) {
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

	private static final class ParameterizedTypeImpl implements
			ParameterizedType, Serializable {
		private static final long serialVersionUID = 1L;

		private final Type ownerType;
		private final List<Type> typeArguments;
		private final Class<?> rawType;

		private final Set<Thread> recurringThreads;
		private final Set<Thread> doublyRecurringThreads;

		ParameterizedTypeImpl(Type ownerType, Class<?> rawType,
				List<Type> typeArguments) {
			this.ownerType = ownerType;
			this.rawType = rawType;
			this.typeArguments = typeArguments;

			recurringThreads = new HashSet<>();
			doublyRecurringThreads = new HashSet<>();
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
			if (recurringThreads.add(currentThread)
					|| doublyRecurringThreads.add(currentThread)) {
				builder.append(typeArguments.stream().map(Types::toString)
						.collect(Collectors.joining(", ")));

				if (!doublyRecurringThreads.remove(currentThread))
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
				int hashCode = (ownerType == null ? 0 : ownerType.hashCode())
						^ typeArguments.hashCode() ^ rawType.hashCode();

				recurringThreads.remove(currentThread);

				return hashCode;
			} else
				return 0;
		}

		@Override
		public boolean equals(Object other) {
			Thread currentThread = Thread.currentThread();
			if (recurringThreads.add(currentThread)) {
				Boolean equals = null;

				if (other == this)
					equals = true;

				if (equals == null) {
					if (other == null || !(other instanceof ParameterizedType))
						equals = false;

					if (equals == null) {
						ParameterizedType that = (ParameterizedType) other;

						equals = getRawType().equals(that.getRawType())
								&& Objects.equals(getOwnerType(), that.getOwnerType())
								&& Arrays.equals(getActualTypeArguments(),
										that.getActualTypeArguments());
					}
				}

				recurringThreads.remove(currentThread);

				return equals;
			} else
				return true;
		}
	}

	private static final List<InferenceVariable> SUBSTITUTE_ARGUMENTS = new ArrayList<>();

	public static Type resolveSupertypeParameters(Type type, Class<?> superclass) {
		Class<?> rawType = Types.getRawType(type);

		Map<TypeVariable<?>, InferenceVariable> parameterSubstitutes = new HashMap<>();
		Map<InferenceVariable, Type> substitutedArguments = new HashMap<>();
		int index = 0;
		if (type instanceof ParameterizedType)
			for (Map.Entry<TypeVariable<?>, Type> parameter : getAllTypeArguments(
					(ParameterizedType) type).entrySet()) {
				int finalIndex = index;
				if (index == SUBSTITUTE_ARGUMENTS.size())
					SUBSTITUTE_ARGUMENTS.add(new InferenceVariable() {
						@Override
						public String toString() {
							return "SUB#" + finalIndex;
						}
					});
				InferenceVariable substituteArgument = SUBSTITUTE_ARGUMENTS
						.get(index++);
				parameterSubstitutes.put(parameter.getKey(), substituteArgument);
				substitutedArguments.put(substituteArgument, parameter.getValue());
			}

		System.out
				.println(" ------------ " + type + " /// " + superclass + " /// ");

		Type supertype = new TypeSubstitution(substitutedArguments::get)
				.resolve(from(rawType, parameterSubstitutes)
						.resolveSupertypeParameters(superclass).getType());

		System.out.println(" -============ " + type + " /// " + superclass
				+ " /// " + supertype + " === " + (type.equals(supertype)));

		return supertype;
	}
}
