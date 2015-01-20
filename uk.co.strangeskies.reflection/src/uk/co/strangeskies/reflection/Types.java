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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Types {
	public static final Map<Class<?>, Class<?>> WRAPPED_PRIMITIVES = Collections
			.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
				private static final long serialVersionUID = 1L;
				{
					put(void.class, Void.class);
					put(boolean.class, Boolean.class);
					put(byte.class, Byte.class);
					put(char.class, Character.class);
					put(short.class, Short.class);
					put(int.class, Integer.class);
					put(long.class, Long.class);
					put(float.class, Float.class);
					put(double.class, Double.class);
				}
			});

	public static final Map<Class<?>, Class<?>> UNWRAPPED_PRIMITIVES = Collections
			.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
				private static final long serialVersionUID = 1L;
				{
					for (Class<?> primitive : WRAPPED_PRIMITIVES.keySet())
						put(WRAPPED_PRIMITIVES.get(primitive), primitive);
				}
			});

	private Types() {}

	public static Class<?> getRawType(Type type) {
		if (type == null) {
			return null;
		} else if (type instanceof TypeVariable) {
			Type[] bounds = ((TypeVariable<?>) type).getBounds();
			if (bounds.length == 0)
				return Object.class;
			else
				return getRawType(bounds[0]);
		} else if (type instanceof InferenceVariable) {
			Type[] bounds = ((InferenceVariable) type).getUpperBounds();
			if (bounds.length == 0)
				return Object.class;
			else
				return getRawType(bounds[0]);
		} else if (type instanceof WildcardType) {
			Type[] bounds = ((WildcardType) type).getUpperBounds();
			if (bounds.length == 0)
				return Object.class;
			else
				return getRawType(bounds[0]);
		} else if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		} else if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof GenericArrayType) {
			return Array.newInstance(
					(getRawType(((GenericArrayType) type).getGenericComponentType())), 0)
					.getClass();
		} else if (type instanceof IntersectionType) {
			return getRawType(((IntersectionType) type).getTypes()[0]);
		}
		throw new IllegalArgumentException("Type of type '" + type
				+ "' is unsupported.");
	}

	public static boolean isPrimitive(Type type) {
		return WRAPPED_PRIMITIVES.keySet().contains(getRawType(type));
	}

	public static boolean isPrimitiveWrapper(Type type) {
		return UNWRAPPED_PRIMITIVES.keySet().contains(getRawType(type));
	}

	public static Type wrap(Type type) {
		if (isPrimitive(type))
			return WRAPPED_PRIMITIVES.get(getRawType(type));
		else
			return type;
	}

	public static Type unwrap(Type type) {
		if (isPrimitiveWrapper(type))
			return UNWRAPPED_PRIMITIVES.get(getRawType(type));
		else
			return type;
	}

	public static Type getComponentType(Type type) {
		if (type instanceof Class)
			return ((Class<?>) type).getComponentType();
		else if (type instanceof GenericArrayType)
			return ((GenericArrayType) type).getGenericComponentType();
		else
			return null;
	}

	public static String toString(Type type) {
		if (type instanceof Class && getRawType(type).isArray())
			return toString(((Class<?>) type).getComponentType()) + "[]";
		else
			return type == null ? "null" : type.getTypeName();
	}

	public static boolean isProperType(Type type) {
		return !(type instanceof InferenceVariable)
				&& InferenceVariable.getAllMentionedBy(type).isEmpty();
	}

	public static boolean isAssignable(Type from, Type to) {
		if (from == null || from.equals(to) || to == null
				|| to.equals(Object.class)) {
			/*
			 * We can always assign to or from 'null', and we can always assign to
			 * Object.
			 */
			return true;
		} else if (to instanceof CaptureType) {
			return ((CaptureType) to).getLowerBounds().length > 0
					&& isAssignable(from,
							IntersectionType.uncheckedFrom(((CaptureType) to).getLowerBounds()));
		} else if (to instanceof TypeVariable) {
			/*
			 * We can only assign to a type variable if it is from the exact same
			 * type.
			 */
			return false;
		} else if (from instanceof TypeVariable) {
			/*
			 * We must be able to assign from at least one of the upper bound,
			 * including the implied upper bound of Object, to the target type.
			 */
			Type[] upperBounds = ((TypeVariable<?>) from).getBounds();
			if (upperBounds.length == 0)
				upperBounds = new Type[] { Object.class };
			return isAssignable(IntersectionType.from(upperBounds), to);
		} else if (from instanceof IntersectionType) {
			/*
			 * We must be able to assign from at least one member of the intersection
			 * type.
			 */
			Type[] types = ((IntersectionType) from).getTypes();
			return types.length == 0
					|| Arrays.stream(types).anyMatch(f -> isAssignable(f, to));
		} else if (to instanceof IntersectionType) {
			/*
			 * We must be able to assign to each member of the intersection type.
			 */
			Type[] types = ((IntersectionType) to).getTypes();
			return Arrays.stream(types).allMatch(t -> isAssignable(from, t));
		} else if (from instanceof WildcardType) {
			/*
			 * We must be able to assign from at least one of the upper bound,
			 * including the implied upper bound of Object, to the target type.
			 */
			Type[] upperBounds = ((WildcardType) from).getUpperBounds();
			if (upperBounds.length == 0)
				upperBounds = new Type[] { Object.class };
			return isAssignable(IntersectionType.from(upperBounds), to);
		} else if (to instanceof WildcardType) {
			/*
			 * If there are no lower bounds the target may be arbitrarily specific, so
			 * we can never assign to it. Otherwise we must be able to assign to each
			 * lower bound.
			 */
			Type[] lowerBounds = ((WildcardType) to).getLowerBounds();
			if (lowerBounds.length == 0)
				return false;
			else
				return isAssignable(from, IntersectionType.from(lowerBounds));
		} else if (from instanceof GenericArrayType) {
			GenericArrayType fromArray = (GenericArrayType) from;
			if (to instanceof Class) {
				Class<?> toClass = (Class<?>) to;
				return toClass.isArray()
						&& isAssignable(fromArray.getGenericComponentType(),
								toClass.getComponentType());
			} else if (to instanceof GenericArrayType) {
				GenericArrayType toArray = (GenericArrayType) to;
				return isAssignable(fromArray.getGenericComponentType(),
						toArray.getGenericComponentType());
			} else
				return false;
		} else if (to instanceof GenericArrayType) {
			GenericArrayType toArray = (GenericArrayType) to;
			if (from instanceof Class) {
				Class<?> fromClass = (Class<?>) from;
				return fromClass.isArray()
						&& isAssignable(fromClass.getComponentType(),
								toArray.getGenericComponentType());
			} else
				return false;
		} else if (to instanceof Class) {
			return ((Class<?>) to).isAssignableFrom(getRawType(from));
		} else if (to instanceof ParameterizedType) {
			Class<?> matchedClass = getRawType(to);

			if (from instanceof Class
					&& matchedClass.isAssignableFrom((Class<?>) from)) {
				return true;
			} else if (!matchedClass.isAssignableFrom(getRawType(from))) {
				return false;
			}

			Type fromParameterization = TypeLiteral.from(from)
					.resolveSupertypeParameters(matchedClass).getType();
			if (!(fromParameterization instanceof ParameterizedType))
				return false;

			Type[] typeParams = matchedClass.getTypeParameters();
			Type[] toTypeArgs = ((ParameterizedType) to).getActualTypeArguments();
			Type[] fromTypeArgs = ((ParameterizedType) fromParameterization)
					.getActualTypeArguments();

			for (int i = 0; i < typeParams.length; i++) {
				if (!isContainedBy(fromTypeArgs[i], toTypeArgs[i]))
					return false;
			}

			return isAssignable(((ParameterizedType) from).getOwnerType(),
					((ParameterizedType) to).getOwnerType());
		} else
			return false;
	}

	public static boolean isContainedBy(Type from, Type to) {
		if (to.equals(from))
			return true;

		if (to instanceof Class || to instanceof ParameterizedType)
			return isAssignable(from, to) && isAssignable(to, from);
		else if (to instanceof IntersectionType) {
			return Arrays.stream(((IntersectionType) to).getTypes()).allMatch(
					t -> isContainedBy(from, t));
		} else if (to instanceof WildcardType) {
			WildcardType toWildcard = (WildcardType) to;

			boolean contained = (toWildcard.getUpperBounds().length == 0 || isAssignable(
					from, IntersectionType.from(toWildcard.getUpperBounds())));

			contained = contained
					&& (toWildcard.getLowerBounds().length == 0 || isAssignable(
							IntersectionType.from(toWildcard.getLowerBounds()), from));

			return contained;
		} else
			return false;
	}

	public static boolean isStrictInvocationContextCompatible(Type from, Type to) {
		if (isPrimitive(from))
			if (isPrimitive(to))
				return true; // TODO check widening primitive conversion
			else
				return false;
		else if (isPrimitive(to))
			return false;

		return isAssignable(from, to);
	}

	public static boolean isLooseInvocationContextCompatible(Type from, Type to) {
		if (isPrimitive(from) && !isPrimitive(to))
			from = wrap(from);
		else if (!isPrimitive(from) && isPrimitive(to))
			from = unwrap(from);
		return isStrictInvocationContextCompatible(from, to);
	}

	public static void validate(Type type) {
		RecursiveTypeVisitor.build().visitBounds().visitEnclosedTypes()
				.visitEnclosingTypes().visitParameters().visitSupertypes()
				.parameterizedTypeVisitor(TypeLiteral::from)
				.intersectionTypeVisitor(i -> IntersectionType.from(i)).create()
				.visit(type);
	}

	public static Set<Type> getAllMentionedBy(Type type, Predicate<Type> condition) {
		Set<Type> types = new HashSet<>();

		Consumer<Type> conditionalAdd = t -> {
			if (condition.test(t))
				types.add(t);
		};

		RecursiveTypeVisitor.build().visitEnclosingTypes().visitParameters()
				.visitBounds().classVisitor(conditionalAdd::accept)
				.genericArrayVisitor(conditionalAdd::accept)
				.intersectionTypeVisitor(conditionalAdd::accept)
				.inferenceVariableVisitor(conditionalAdd::accept)
				.parameterizedTypeVisitor(conditionalAdd::accept)
				.wildcardVisitor(conditionalAdd::accept)
				.typeVariableVisitor(conditionalAdd::accept).create().visit(type);

		return types;
	}
}
