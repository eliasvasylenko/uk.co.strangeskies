/*
 * Copyright (C) 2016 ${copyright.holder.name} <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ParameterizedTypes.ParameterizedTypeImpl;
import uk.co.strangeskies.text.parsing.Parser;
import uk.co.strangeskies.utilities.Isomorphism;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

/**
 * A collection of general utility methods relating to the Java type system.
 * Some utilities related to more specific classes of type may be found in
 * {@link WildcardTypes}, {@link ParameterizedTypes}, and {@link ArrayTypes}..
 * 
 * @author Elias N Vasylenko
 */
public final class Types {
	private static final TypeParser TYPE_PARSER = new TypeParser(Imports.empty());

	private static final Map<Class<?>, Class<?>> WRAPPED_PRIMITIVES = Collections
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

	private static final Map<Class<?>, Class<?>> UNWRAPPED_PRIMITIVES = Collections
			.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
				private static final long serialVersionUID = 1L;

				{
					for (Class<?> primitive : WRAPPED_PRIMITIVES.keySet())
						put(WRAPPED_PRIMITIVES.get(primitive), primitive);
				}
			});

	private Types() {}

	/**
	 * The raw types of the type represented by this TypeToken. In the case of
	 * most simple TypeTokens, this will be a set with one entry, equal to the
	 * result of {@link #getRawType(Type type)}. For more complex types, a set of
	 * multiple raw types may be derived, for example, from each upper bound, of
	 * from each item in an intersection type.
	 * 
	 * @param type
	 *          The type of which we wish to determine the raw types.
	 * @return The raw types of the type represented by this TypeToken.
	 */
	public static Set<Class<?>> getRawTypes(Type type) {
		return getUpperBounds(type).stream().map(Types::getRawType).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * The raw type of the given type. In the case of Types of certain classes,
	 * for example InferenceVariable or IntersectionType, this single raw type may
	 * be insufficient to fully describe the type, in which case
	 * {@link Types#getRawTypes(Type)} may be more appropriate.
	 * 
	 * @param type
	 *          The type of which we wish to determine the raw type.
	 * @return The raw type of the type represented by this TypeToken.
	 */
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
			return Object.class;
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
			return Array.newInstance(getRawType(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
		} else if (type instanceof IntersectionType) {
			if (((IntersectionType) type).getTypes().length == 0)
				return Object.class;
			else
				return getRawType(((IntersectionType) type).getTypes()[0]);
		}
		throw new ReflectionException(p -> p.unsupportedType(type));
	}

	/**
	 * Find the upper bounds of a given type.
	 * 
	 * @param type
	 *          The type whose bounds we wish to discover.
	 * @return The upper bounds of the given type.
	 */
	public static Set<Type> getUpperBounds(Type type) {
		Type[] types;

		if (type instanceof IntersectionType)
			types = ((IntersectionType) type).getTypes();
		else if (type instanceof WildcardType)
			types = ((WildcardType) type).getUpperBounds();
		else if (type instanceof TypeVariable)
			types = ((TypeVariable<?>) type).getBounds();
		else
			types = new Type[] { type };

		return new LinkedHashSet<>(Arrays.asList(types));
	}

	/**
	 * Find the lower bounds of a given type.
	 * 
	 * @param type
	 *          The type whose bounds we wish to discover.
	 * @return The lower bounds of the given type, or null if no such bounds
	 *         exist.
	 */
	public static Set<Type> getLowerBounds(Type type) {
		Type[] types;

		if (type instanceof IntersectionType)
			types = ((IntersectionType) type).getTypes();
		else if (type instanceof WildcardType)
			types = ((WildcardType) type).getLowerBounds();
		else if (type instanceof TypeVariableCapture)
			types = ((TypeVariableCapture) type).getLowerBounds();
		else
			types = new Type[] { type };

		return new LinkedHashSet<>(Arrays.asList(types));
	}

	/**
	 * Get all primitive type classes
	 * 
	 * @return A set containing all primitive types.
	 */
	public static Set<Class<?>> getPrimitives() {
		return WRAPPED_PRIMITIVES.keySet();
	}

	/**
	 * Is the given type a primitive type as per the Java type system.
	 * 
	 * @param type
	 *          The type we wish to classify.
	 * @return True if the type is primitive, false otherwise.
	 */
	public static boolean isPrimitive(Type type) {
		return WRAPPED_PRIMITIVES.keySet().contains(getRawType(type));
	}

	/**
	 * Is the type a wrapper for a primitive type as per the Java type system.
	 * 
	 * @param type
	 *          The type we wish to classify.
	 * @return True if the type is a primitive wrapper, false otherwise.
	 */
	public static boolean isPrimitiveWrapper(Type type) {
		return UNWRAPPED_PRIMITIVES.keySet().contains(getRawType(type));
	}

	/**
	 * If this TypeToken is a primitive type, determine the wrapped primitive
	 * type.
	 * 
	 * @param <T>
	 *          The type we wish to wrap.
	 * @param type
	 *          The type we wish to wrap.
	 * @return The wrapper type of the primitive type this TypeToken represents,
	 *         otherwise this TypeToken itself.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Type> T wrapPrimitive(T type) {
		if (isPrimitive(type))
			return (T) WRAPPED_PRIMITIVES.get(getRawType(type));
		else
			return type;
	}

	/**
	 * If this TypeToken is a wrapper of a primitive type, determine the unwrapped
	 * primitive type.
	 * 
	 * @param <T>
	 *          The type we wish to unwrap.
	 * @param type
	 *          The type we wish to unwrap.
	 * @return The primitive type wrapped by this TypeToken, otherwise this
	 *         TypeToken itself.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Type> T unwrapPrimitive(T type) {
		if (isPrimitiveWrapper(type))
			return (T) UNWRAPPED_PRIMITIVES.get(getRawType(type));
		else
			return type;
	}

	/**
	 * Determine whether a given class is abstract.
	 * 
	 * @param rawType
	 *          The type we wish to classify.
	 * @return True if the type is abstract, false otherwise.
	 */
	public static boolean isAbstract(Class<?> rawType) {
		return Modifier.isAbstract(rawType.getModifiers());
	}

	/**
	 * Determine whether a given class is final.
	 * 
	 * @param rawType
	 *          The type we wish to classify.
	 * @return True if the type is final, false otherwise.
	 */
	public static boolean isFinal(Class<?> rawType) {
		return Modifier.isFinal(rawType.getModifiers());
	}

	/**
	 * Determine whether a given class is an interface.
	 * 
	 * @param rawType
	 *          The type we wish to classify.
	 * @return True if the type is an interface, false otherwise.
	 */
	public static boolean isInterface(Class<?> rawType) {
		return Modifier.isInterface(rawType.getModifiers());
	}

	/**
	 * Determine whether a given class is private.
	 * 
	 * @param rawType
	 *          The type we wish to classify.
	 * @return True if the type is private, false otherwise.
	 */
	public static boolean isPrivate(Class<?> rawType) {
		return Modifier.isPrivate(rawType.getModifiers());
	}

	/**
	 * Determine whether a given class is protected.
	 * 
	 * @param rawType
	 *          The type we wish to classify.
	 * @return True if the type is protected, false otherwise.
	 */
	public static boolean isProtected(Class<?> rawType) {
		return Modifier.isProtected(rawType.getModifiers());
	}

	/**
	 * Determine whether a given class is public.
	 * 
	 * @param rawType
	 *          The type we wish to classify.
	 * @return True if the type is public, false otherwise.
	 */
	public static boolean isPublic(Class<?> rawType) {
		return Modifier.isPublic(rawType.getModifiers());
	}

	/**
	 * Determine whether a given class is static.
	 * 
	 * @param rawType
	 *          The type we wish to classify.
	 * @return True if the type is static, false otherwise.
	 */
	public static boolean isStatic(Class<?> rawType) {
		return Modifier.isStatic(rawType.getModifiers());
	}

	/**
	 * Find the component type of the given type, if the given {@link Type}
	 * instance is an array {@link Class} or an instance of
	 * {@link GenericArrayType}.
	 * 
	 * @param type
	 *          The type of which we wish to determine the component type.
	 * @return The component type of the given type, if it is an array type,
	 *         otherwise null.
	 */
	public static Type getComponentType(Type type) {
		if (type instanceof Class)
			return ((Class<?>) type).getComponentType();
		else if (type instanceof GenericArrayType)
			return ((GenericArrayType) type).getGenericComponentType();
		else
			return null;
	}

	/**
	 * Find the innermost component type of the given type, if the given
	 * {@link Type} instance is an array {@link Class} or an instance of
	 * {@link GenericArrayType} with any number of dimensions.
	 * 
	 * @param type
	 *          The type of which we wish to determine the component type.
	 * @return The component type of the given type if it is an array type,
	 *         otherwise null.
	 */
	public static Type getInnerComponentType(Type type) {
		Type component = null;

		if (type instanceof Class)
			while ((type = ((Class<?>) type).getComponentType()) != null)
				component = type;
		else
			while (type instanceof GenericArrayType && (type = ((GenericArrayType) type).getGenericComponentType()) != null)
				component = type;

		return component;
	}

	/**
	 * Determine the number of array dimensions exist on the given type.
	 * 
	 * @param type
	 *          A type which may or may not be an array.
	 * @return The number of dimensions on the given type, or 0 if it is not an
	 *         array type.
	 */
	public static int getArrayDimensions(Type type) {
		int count = 0;

		if (type instanceof Class)
			while ((type = ((Class<?>) type).getComponentType()) != null)
				count++;
		else
			while (type instanceof GenericArrayType && (type = ((GenericArrayType) type).getGenericComponentType()) != null)
				count++;

		return count;
	}

	/**
	 * Determine if a given type, {@code to}, is legally castable from another
	 * given type, {@code from}.
	 * 
	 * @param from
	 *          The type from which we wish to determine castability.
	 * @param to
	 *          The type to which we wish to determine castability.
	 * @return True if the types are castable, false otherwise.
	 */
	public static boolean isCastable(Type from, Type to) {
		throw new UnsupportedOperationException();
	}

	/**
	 * If a given object is assignable to a given raw type, it will be converted
	 * to that type. Generally this is an identity conversion, but for wrapped
	 * primitive types the extra step is taken to make conversions to which are
	 * consistent with widening primitive conversions.
	 * 
	 * @param <T>
	 *          the type to which we wish to assign
	 * @param object
	 *          the type from which we wish to assign
	 * @param type
	 *          the type to which we wish to assign
	 * @return true if the types are assignable, false otherwise
	 */
	@SuppressWarnings("unchecked")
	public static <T> T assign(Object object, Class<T> type) {
		Class<?> currentType = unwrapPrimitive(object.getClass());
		Class<?> rawTargetType = unwrapPrimitive(getRawType(type));

		if (isStrictInvocationContextCompatible(currentType, rawTargetType)) {
			if (isPrimitive(rawTargetType)) {
				/*
				 * If assignable primitives:
				 */
				if (rawTargetType.equals(double.class)) {
					object = ((Number) object).doubleValue();
				} else if (rawTargetType.equals(float.class)) {
					object = ((Number) object).floatValue();
				} else if (rawTargetType.equals(long.class)) {
					object = ((Number) object).longValue();
				} else if (rawTargetType.equals(int.class)) {
					object = ((Number) object).intValue();
				} else if (rawTargetType.equals(short.class)) {
					object = ((Number) object).shortValue();
				}
			}
		} else {
			Object finalObject = object; // Get your shit together Eclipse ffs.
			throw new ReflectionException(p -> p.invalidAssignmentObject(finalObject, type));
		}

		return (T) object;
	}

	/**
	 * Test whether two types are equal to one another.
	 * 
	 * @param a
	 *          the first type
	 * @param b
	 *          the second type
	 * @return true if the two given types are equal, false otherwise
	 */
	public static boolean equals(Type a, Type b) {
		return equals(a, b, new Isomorphism());
	}

	private static class Equality {
		private Type a, b;

		public Equality(Type a, Type b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Equality))
				return false;

			Equality that = (Equality) obj;
			return (a == that.a && b == that.b) || (a == that.b && b == that.a);
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(a) ^ (System.identityHashCode(b) * 7);
		}
	}

	private static boolean equals(Type first, Type second, Isomorphism isomorphism) {
		Type a;
		Type b;

		if (first instanceof IntersectionType && ((IntersectionType) first).getTypes().length == 1) {
			a = ((IntersectionType) first).getTypes()[0];
		} else {
			a = first;
		}
		if (second instanceof IntersectionType && ((IntersectionType) second).getTypes().length == 1) {
			b = ((IntersectionType) second).getTypes()[0];
		} else {
			b = second;
		}

		return isomorphism.byEquality().getPartialMapping(new Equality(a, b), () -> true, e -> {

			boolean equal;

			if (a == b) {
				equal = true;

			} else if (a instanceof ParameterizedTypeImpl && b instanceof ParameterizedTypeImpl) {
				equal = a.hashCode() == b.hashCode() && parameterizedTypeEquals((ParameterizedType) a, (ParameterizedType) b);

			} else if (a instanceof ParameterizedType && b instanceof ParameterizedType) {
				equal = parameterizedTypeEquals((ParameterizedType) a, (ParameterizedType) b);

			} else if (a instanceof IntersectionType && b instanceof IntersectionType) {
				IntersectionType aIntersectionType = (IntersectionType) a;
				IntersectionType bIntersectionType = (IntersectionType) b;

				if (aIntersectionType.getTypes().length != bIntersectionType.getTypes().length) {
					equal = false;
				} else {
					equal = true;

					for (Type aType : ((IntersectionType) a).getTypes()) {
						boolean contains = false;

						for (Type bType : ((IntersectionType) b).getTypes()) {
							if (Types.equals(aType, bType)) {
								contains = true;
								break;
							}
						}

						if (!contains) {
							equal = false;
							break;
						}
					}
				}

			} else if (a instanceof Class && b instanceof Class) {
				equal = a.equals(b);

			} else {
				equal = false;
			}

			return equal;
		});
	}

	private static boolean parameterizedTypeEquals(ParameterizedType a, ParameterizedType b) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Determine if a given type, {@code to}, is assignable from another given
	 * type, {@code from}. Or in other words, if {@code to} is a supertype of
	 * {@code from}. Types are considered assignable if they involve unchecked
	 * generic casts.
	 * 
	 * @param from
	 *          The type from which we wish to determine assignability.
	 * @param to
	 *          The type to which we wish to determine assignability.
	 * @return True if the types are assignable, false otherwise.
	 */
	public static boolean isAssignable(Type from, Type to) {
		return isAssignable(from, to, new Isomorphism());
	}

	/**
	 * Determine if the given type, {@code from}, contains the given type,
	 * {@code to}. In other words, if either of the given types are wildcards,
	 * determine if every possible instantiation of {@code to} is also a valid
	 * instantiation of {@code from}. Or if neither type is a wildcard, determine
	 * whether both types are assignable to each other as per
	 * {@link Types#isAssignable(Type, Type)}.
	 * 
	 * @param from
	 *          The type within which we are determining containment.
	 * @param to
	 *          The type of which we are determining containment.
	 * @return True if {@code from} <em>contains</em> {@code to}, false otherwise.
	 */
	public static boolean isContainedBy(Type from, Type to) {
		return isContainedBy(from, to, new Isomorphism());
	}

	private static class Assignment {
		private Type from, to;

		public Assignment(Type from, Type to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Assignment))
				return false;

			Assignment that = (Assignment) obj;
			return from == that.from && to == that.to;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(from) ^ (System.identityHashCode(to) * 7);
		}

		@Override
		public String toString() {
			return from + "@" + System.identityHashCode(from) + " <: " + to + "@" + System.identityHashCode(to);
		}
	}

	private static boolean isAssignable(Type from, Type[] toAll, Isomorphism isomorphism) {
		return Arrays.stream(toAll).allMatch(t -> isAssignable(from, t, isomorphism));
	}

	private static boolean isAssignable(Type[] fromAll, Type to, Isomorphism isomorphism) {
		return fromAll.length == 0 || Arrays.stream(fromAll).anyMatch(f -> isAssignable(f, to, isomorphism));
	}

	private static boolean isAssignable(Type from, Type to, Isomorphism isomorphism) {
		if (from instanceof IntersectionType && ((IntersectionType) from).getTypes().length == 1)
			from = ((IntersectionType) from).getTypes()[0];
		if (to instanceof IntersectionType && ((IntersectionType) to).getTypes().length == 1)
			to = ((IntersectionType) to).getTypes()[0];

		Type fromFinal = from;
		Type toFinal = to;

		return isomorphism.byEquality().getPartialMapping(new Assignment(from, to), (a, partial) -> {
			partial.accept(() -> true);

			return isAssignableImpl(fromFinal, toFinal, isomorphism);
		});
	}

	private static Boolean isAssignableImpl(Type from, Type to, Isomorphism isomorphism) {
		boolean assignable;

		if (from == null || to == null || to.equals(Object.class) || from == to) {
			/*
			 * We can always assign to or from 'null', and we can always assign to
			 * Object.
			 */
			assignable = true;
		} else if (to instanceof IntersectionType) {
			/*
			 * We must be able to assign to each member of the intersection type.
			 */
			Type[] types = ((IntersectionType) to).getTypes();

			assignable = Arrays.stream(types).allMatch(t -> isAssignable(from, t, isomorphism));
		} else if (from instanceof IntersectionType) {
			/*
			 * We must be able to assign from at least one member of the intersection
			 * type.
			 */
			Type[] types = ((IntersectionType) from).getTypes();

			assignable = isAssignable(types, to, isomorphism);
		} else if (from instanceof WildcardType) {
			/*
			 * We must be able to assign from at least one of the upper bounds,
			 * including the implied upper bound of Object, to the target type.
			 */
			Type[] upperBounds = ((WildcardType) from).getUpperBounds();
			if (upperBounds.length == 0)
				upperBounds = new Type[] { Object.class };

			assignable = isAssignable(upperBounds, to, isomorphism);
		} else if (to instanceof WildcardType) {
			/*
			 * If there are no lower bounds the target may be arbitrarily specific, so
			 * we can never assign to it. Otherwise we must be able to assign to each
			 * lower bound.
			 */
			Type[] lowerBounds = ((WildcardType) to).getLowerBounds();

			if (lowerBounds.length == 0)
				assignable = false;
			else
				assignable = isAssignable(from, lowerBounds, isomorphism);
		} else if (from instanceof TypeVariable) {
			/*
			 * We must be able to assign from at least one of the upper bound,
			 * including the implied upper bound of Object, to the target type.
			 */
			Type[] upperBounds = ((TypeVariable<?>) from).getBounds();
			if (upperBounds.length == 0)
				upperBounds = new Type[] { Object.class };

			assignable = isAssignable(upperBounds, to, isomorphism);

			if (!assignable && to instanceof TypeVariableCapture) {
				assignable = Arrays.asList(((TypeVariableCapture) to).getLowerBounds()).contains(from);
			}
		} else if (to instanceof TypeVariableCapture) {
			/*
			 * We assign to a type variable capture if we can assign to its lower
			 * bounds, or if it is from the exact same type, or explicitly mentioned
			 * in an upper bound or intersection type.
			 */
			assignable = ((TypeVariableCapture) to).getLowerBounds().length > 0
					&& isAssignable(from, ((TypeVariableCapture) to).getLowerBounds(), isomorphism);
		} else if (to instanceof TypeVariable) {
			/*
			 * We can only assign to a type variable if it is from the exact same
			 * type, or explicitly mentioned in an upper bound or intersection type.
			 */
			assignable = false;
		} else if (from instanceof GenericArrayType) {
			GenericArrayType fromArray = (GenericArrayType) from;

			if (to instanceof Class) {
				Class<?> toClass = (Class<?>) to;

				assignable = toClass.isArray()
						&& isAssignable(fromArray.getGenericComponentType(), toClass.getComponentType(), isomorphism);
			} else if (to instanceof GenericArrayType) {
				GenericArrayType toArray = (GenericArrayType) to;

				assignable = isAssignable(fromArray.getGenericComponentType(), toArray.getGenericComponentType(), isomorphism);
			} else
				assignable = false;
		} else if (to instanceof GenericArrayType) {
			GenericArrayType toArray = (GenericArrayType) to;
			if (from instanceof Class) {
				Class<?> fromClass = (Class<?>) from;
				assignable = fromClass.isArray()
						&& isAssignable(fromClass.getComponentType(), toArray.getGenericComponentType(), isomorphism);
			} else
				assignable = false;
		} else if (to instanceof Class) {
			assignable = ((Class<?>) to).isAssignableFrom(getRawType(from));
		} else if (to instanceof ParameterizedType) {
			Class<?> matchedClass = getRawType(to);

			System.out.println("#");
			System.out.println(from);
			System.out.println(to);

			if (from instanceof Class && matchedClass.isAssignableFrom((Class<?>) from)) {
				assignable = true;
			} else if (!matchedClass.isAssignableFrom(getRawType(from))) {
				assignable = false;
			} else {
				Type fromParameterization = ParameterizedTypes.resolveSupertypeParameters(from, matchedClass);

				if (!(fromParameterization instanceof ParameterizedType))
					assignable = false;
				else {
					Iterator<Type> toTypeArguments = ParameterizedTypes.getAllTypeArguments((ParameterizedType) to)
							.map(Map.Entry::getValue).iterator();
					Iterator<Type> fromTypeArguments = ParameterizedTypes
							.getAllTypeArguments((ParameterizedType) fromParameterization).map(Map.Entry::getValue).iterator();

					assignable = true;
					while (toTypeArguments.hasNext()) {
						if (!isContainedBy(fromTypeArguments.next(), toTypeArguments.next(), isomorphism))
							assignable = false;
					}

					assignable = assignable && isAssignable(((ParameterizedType) from).getOwnerType(),
							((ParameterizedType) to).getOwnerType(), isomorphism);
				}
			}
		} else {
			assignable = false;
		}

		return assignable;
	}

	private static boolean isContainedBy(Type from, Type to, Isomorphism isomorphism) {
		boolean contained;

		if (to.equals(from)) {
			contained = true;
		} else if (to instanceof Class || to instanceof ParameterizedType || to instanceof IntersectionType) {
			contained = isAssignable(from, to, isomorphism) && isAssignable(to, from, isomorphism);
		} else if (to instanceof WildcardType) {
			WildcardType toWildcard = (WildcardType) to;

			contained = (toWildcard.getUpperBounds().length == 0
					|| isAssignable(from, toWildcard.getUpperBounds(), isomorphism));

			contained = contained
					&& (toWildcard.getLowerBounds().length == 0 || isAssignable(toWildcard.getLowerBounds(), from, isomorphism));
		} else
			contained = false;

		return contained;
	}

	/**
	 * <p>
	 * Determine whether a given type, {@code from}, is compatible with a given
	 * type, {@code to}, within a strict invocation context.
	 * 
	 * 
	 * <p>
	 * Types are considered so compatible if assignment is possible through
	 * application of the following conversions:
	 * 
	 * <ul>
	 * <li>an identity conversion (§5.1.1)</li>
	 * <li>a widening primitive conversion (§5.1.2)</li>
	 * <li>a widening reference conversion (§5.1.5)</li>
	 * </ul>
	 * 
	 * @param from
	 *          The type from which to determine compatibility.
	 * @param to
	 *          The type to which to determine compatibility.
	 * @return True if the type {@code from} is compatible with the type
	 *         {@code to}, false otherwise.
	 */
	public static boolean isStrictInvocationContextCompatible(Type from, Type to) {
		if (isPrimitive(from)) {
			if (isPrimitive(to)) {
				if (to.equals(from) || to.equals(double.class)) {
					return true;
				} else if (to.equals(float.class)) {
					return !from.equals(double.class);
				} else if (to.equals(long.class)) {
					return !from.equals(double.class) && !from.equals(float.class);
				} else if (to.equals(int.class)) {
					return from.equals(byte.class) || from.equals(short.class) || from.equals(char.class);
				} else if (to.equals(short.class)) {
					return from.equals(byte.class);
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if (isPrimitive(to)) {
			return false;
		}

		return isAssignable(from, to);
	}

	/**
	 * <p>
	 * Determine whether a given type, {@code from}, is compatible with a given
	 * type, {@code to}, within a loose invocation context.
	 * 
	 * 
	 * <p>
	 * Types are considered so compatible if assignment is possible through
	 * application of the following conversions:
	 * 
	 * <ul>
	 * <li>an identity conversion</li>
	 * <li>a widening primitive conversion</li>
	 * <li>a widening reference conversion</li>
	 * <li>a boxing conversion, optionally followed by widening reference
	 * conversion</li>
	 * <li>an unboxing conversion, optionally followed by a widening primitive
	 * conversion</li>
	 * </ul>
	 * 
	 * @param from
	 *          The type from which to determine compatibility.
	 * @param to
	 *          The type to which to determine compatibility.
	 * @return True if the type {@code from} is compatible with the type
	 *         {@code to}, false otherwise.
	 */
	public static boolean isLooseInvocationContextCompatible(Type from, Type to) {
		if (isPrimitive(from) && !isPrimitive(to))
			from = wrapPrimitive(from);
		else if (!isPrimitive(from) && isPrimitive(to))
			from = unwrapPrimitive(from);

		return isAssignable(from, to);
	}

	/**
	 * Ensure all intersection types and parameterized types mentioned by the
	 * given type are sound. An exception is thrown if the type is not valid.
	 * 
	 * @param type
	 *          the type to be validated
	 */
	public static void validate(Type type) {
		RecursiveTypeVisitor.build().visitBounds().visitEnclosedTypes().visitEnclosingTypes().visitParameters()
				.visitSupertypes().parameterizedTypeVisitor(TypeToken::overType)
				.intersectionTypeVisitor(i -> IntersectionType.from(i.getTypes())).create().visit(type);
	}

	/**
	 * Search through all types mentioned by a given type, whether by identity, or
	 * through bound relationships, type parameterizations, type intersections, or
	 * generic array components, and collect all types meeting a given condition.
	 * 
	 * @param type
	 *          The type to search for mentions which match the given condition.
	 * @param condition
	 *          The condition to classify matching types.
	 * @return A set of all mentioned types matching the condition.
	 */
	public static Set<Type> getAllMentionedBy(Type type, Predicate<Type> condition) {
		Set<Type> types = new HashSet<>();

		Consumer<Type> conditionalAdd = t -> {
			if (condition.test(t))
				types.add(t);
		};

		RecursiveTypeVisitor.build().visitEnclosingTypes().visitParameters().visitBounds()
				.classVisitor(conditionalAdd::accept).genericArrayVisitor(conditionalAdd::accept)
				.intersectionTypeVisitor(conditionalAdd::accept).inferenceVariableVisitor(conditionalAdd::accept)
				.parameterizedTypeVisitor(conditionalAdd::accept).wildcardVisitor(conditionalAdd::accept)
				.typeVariableVisitor(conditionalAdd::accept).create().visit(type);

		return types;
	}

	/**
	 * See {@link Types#leastUpperBound(Collection)}.
	 * 
	 * @param upperBounds
	 *          Forwards to {@code upperBounds} parameter.
	 * @return As referenced method.
	 */
	public static Type leastUpperBound(Type... upperBounds) {
		return leastUpperBound(Arrays.asList(upperBounds));
	}

	/**
	 * Derive the least upper bound for a set of types, as defined in the Java
	 * language specification.
	 * 
	 * @param upperBounds
	 *          A collection of types representing the upper bounds of an unknown
	 *          type.
	 * @return The least specific single type which, as an upper bound, will also
	 *         satisfy each upper bound in the given set.
	 */
	public static Type leastUpperBound(Collection<Type> upperBounds) {
		Type upperBound = leastUpperBoundImpl(upperBounds, new Isomorphism());

		/*
		 * Not sure if this is necessary! But it's cheap enough to check. Can't
		 * validate IntersectionTypes and ParameterizedTypes as we create them, as
		 * they may contain uninitialised proxies in place of ParameterizedTypes.
		 */
		Types.validate(upperBound);

		return upperBound;
	}

	private static Type leastUpperBoundImpl(Collection<Type> upperBounds, Isomorphism isomorphism) {
		if (upperBounds.size() == 1)
			/*
			 * If k = 1, then the lub is the type itself: lub(U) = U.
			 */
			return upperBounds.iterator().next();
		else {
			/*
			 * For each Ui (1 ≤ i ≤ k):
			 */
			Iterator<Type> lowerBoundsIterator = upperBounds.iterator();
			MultiMap<Class<?>, ParameterizedType, ? extends Set<ParameterizedType>> erasedCandidates = new MultiHashMap<>(
					HashSet::new);
			erasedCandidates.addAll(getErasedSupertypes(lowerBoundsIterator.next()));

			while (lowerBoundsIterator.hasNext()) {
				Type t = lowerBoundsIterator.next();
				Map<Class<?>, ParameterizedType> erasedSupertypes = getErasedSupertypes(t);
				erasedCandidates.keySet().retainAll(erasedSupertypes.keySet());
				for (Map.Entry<Class<?>, ParameterizedType> erasedSupertype : erasedSupertypes.entrySet())
					if (erasedCandidates.containsKey(erasedSupertype.getKey()) && erasedSupertype.getValue() != null)
						erasedCandidates.add(erasedSupertype.getKey(), erasedSupertype.getValue());
			}

			minimiseCandidates(erasedCandidates);

			List<Type> bestTypes = erasedCandidates.entrySet().stream()
					.map(e -> best(e.getKey(), new ArrayList<>(e.getValue()), isomorphism)).collect(Collectors.toList());

			return IntersectionType.uncheckedFrom(bestTypes);
		}
	}

	private static void minimiseCandidates(
			MultiMap<Class<?>, ParameterizedType, ? extends Set<ParameterizedType>> erasedCandidates) {
		List<Class<?>> minimalCandidates = new ArrayList<>(erasedCandidates.keySet());
		if (minimalCandidates.size() > 1)
			for (int i = 0; i < minimalCandidates.size(); i++)
				for (int j = i + 1; j < minimalCandidates.size(); j++) {
					if (minimalCandidates.get(i).isAssignableFrom(minimalCandidates.get(j))) {
						minimalCandidates.remove(i);
						j = i;
					} else if (minimalCandidates.get(j).isAssignableFrom(minimalCandidates.get(i))) {
						minimalCandidates.remove(j--);
					}
				}
		erasedCandidates.keySet().retainAll(minimalCandidates);
	}

	/**
	 * Given a number of candidate parameterizations of a given class, derive the
	 * most specific possible parameterization which is a supertype of all
	 * candidates according to the Java 8 language specification regarding type
	 * inference.
	 * 
	 * @param rawClass
	 *          the class to be parameterized
	 * @param parameterizations
	 *          the candidate parameterizations
	 * @return the parameterized type which minimally contains all the given types
	 */
	public static Type best(Class<?> rawClass, List<ParameterizedType> parameterizations) {
		return best(rawClass, parameterizations, new Isomorphism());
	}

	/**
	 * Given a number of candidate parameterizations of a given class, derive the
	 * most specific possible parameterization which is a supertype of all
	 * candidates according to the Java 8 language specification regarding type
	 * inference.
	 * 
	 * @param rawClass
	 *          the class to be parameterized
	 * @param parameterizations
	 *          the candidate parameterizations
	 * @param isomorphism
	 *          an isomorphism to avoid unnecessary repeat calculations
	 * @return the parameterized type which minimally contains all the given types
	 */
	public static Type best(Class<?> rawClass, List<ParameterizedType> parameterizations, Isomorphism isomorphism) {
		if (parameterizations.isEmpty())
			return rawClass;
		else if (parameterizations.size() == 1) {
			Type parameterization = parameterizations.iterator().next();
			return parameterization == null ? rawClass : parameterization;
		}

		List<TypeVariable<?>> typeParameters = ParameterizedTypes.getAllTypeParameters(rawClass).collect(toList());
		/*
		 * Proxy guard against recursive generation of infinite types
		 */
		return isomorphism.byEquality().getProxiedMapping(new HashSet<>(parameterizations), ParameterizedType.class,
				p -> bestImpl(rawClass, typeParameters, parameterizations, isomorphism));
	}

	private static ParameterizedType bestImpl(Class<?> rawClass, List<TypeVariable<?>> typeParameters,
			List<ParameterizedType> parameterizations, Isomorphism isomorphism) {
		Map<TypeVariable<?>, Type> leastContainingParameterization = new HashMap<>();

		for (int j = 0; j < typeParameters.size(); j++) {
			TypeVariable<?> variable = typeParameters.get(j);
			for (int i = 0; i < parameterizations.size(); i++) {
				ParameterizedType parameterization = parameterizations.get(i);
				if (parameterization != null) {
					Type argumentU = parameterization.getActualTypeArguments()[j];
					Type argumentV = leastContainingParameterization.get(variable);

					if (argumentV == null)
						leastContainingParameterization.put(variable, argumentU);
					else {
						leastContainingParameterization.put(variable, leastContainingArgument(argumentU, argumentV, isomorphism));
					}
				}
			}
		}

		ParameterizedType best = ParameterizedTypes.parameterizeUnchecked(rawClass,
				leastContainingParameterization::get);

		return best;
	}

	/**
	 * Fetch the least containing argument of type type arguments according to the
	 * Java 8 language specification.
	 * 
	 * @param argumentU
	 *          the first argument
	 * @param argumentV
	 *          the second argument
	 * @return the type argument which minimally contains both the given type
	 *         arguments
	 */
	public static Type leastContainingArgument(Type argumentU, Type argumentV) {
		return leastContainingArgument(argumentU, argumentV, new Isomorphism());
	}

	/**
	 * Fetch the least containing argument of type type arguments according to the
	 * Java 8 language specification.
	 * 
	 * @param argumentU
	 *          the first argument
	 * @param argumentV
	 *          the second argument
	 * @param isomorphism
	 *          an isomorphism to avoid unnecessary repeat calculations
	 * @return the type argument which minimally contains both the given type
	 *         arguments
	 */
	public static Type leastContainingArgument(Type argumentU, Type argumentV, Isomorphism isomorphism) {
		if (argumentU instanceof WildcardType
				&& (!(argumentV instanceof WildcardType) || ((WildcardType) argumentV).getUpperBounds().length > 0)) {
			Type swap = argumentU;
			argumentU = argumentV;
			argumentV = swap;
		}

		if (argumentU instanceof WildcardType) {
			WildcardType wildcardU = (WildcardType) argumentU;
			WildcardType wildcardV = (WildcardType) argumentV;

			if (wildcardU.getUpperBounds().length > 0) {
				if (wildcardV.getUpperBounds().length > 0) {
					/*
					 * lcta(? extends U, ? extends V) = ? extends lub(U, V)
					 */
					List<Type> aggregation = Arrays.asList(IntersectionType.from(wildcardU.getUpperBounds()),
							IntersectionType.from(wildcardV.getUpperBounds()));
					return WildcardTypes.upperBounded(leastUpperBoundImpl(aggregation, isomorphism));
				} else {
					/*
					 * lcta(? extends U, ? super V) = U if U = V, otherwise ?
					 */
					return argumentU.equals(argumentV) ? argumentU : WildcardTypes.unbounded();
				}
			} else {
				/*
				 * lcta(? super U, ? super V) = ? super glb(U, V)
				 */
				return WildcardTypes.lowerBounded(
						greatestLowerBound(IntersectionType.uncheckedFrom(((WildcardType) argumentU).getLowerBounds()),
								IntersectionType.uncheckedFrom(((WildcardType) argumentV).getLowerBounds())));
			}
		} else if (argumentV instanceof WildcardType) {
			if (((WildcardType) argumentV).getUpperBounds().length > 0) {
				/*
				 * lcta(U, ? extends V) = ? extends lub(U, V)
				 */
				List<Type> bounds = new ArrayList<>(Arrays.asList(((WildcardType) argumentV).getUpperBounds()));
				bounds.add(argumentU);
				return WildcardTypes.upperBounded(leastUpperBoundImpl(bounds, isomorphism));
			} else {
				/*
				 * lcta(U, ? super V) = ? super glb(U, V)
				 */
				return WildcardTypes.lowerBounded(
						greatestLowerBound(argumentU, IntersectionType.uncheckedFrom(((WildcardType) argumentV).getLowerBounds())));
			}
		} else {
			/*
			 * lcta(U, V) = U if U = V, otherwise ? extends lub(U, V)
			 */
			return argumentU.equals(argumentV) ? argumentU
					: WildcardTypes.upperBounded(leastUpperBoundImpl(Arrays.asList(argumentU, argumentV), isomorphism));
		}
	}

	private static Map<Class<?>, ParameterizedType> getErasedSupertypes(Type of) {
		Map<Class<?>, ParameterizedType> supertypes = new HashMap<>();

		RecursiveTypeVisitor.build().visitSupertypes().classVisitor(type -> {
			Type parameterized = ParameterizedTypes.resolveSupertypeParameters(of, type);
			supertypes.put(type, (parameterized instanceof ParameterizedType) ? (ParameterizedType) parameterized : null);
		}).parameterizedTypeVisitor(type -> supertypes.put(getRawType(type), type)).create().visit(of);

		return supertypes;
	}

	/**
	 * See {@link Types#greatestLowerBound(Collection)}.
	 * 
	 * @param lowerBounds
	 *          Forwards to {@code lowerBounds} parameter.
	 * @return As referenced method.
	 */
	public static Type greatestLowerBound(Type... lowerBounds) {
		return greatestLowerBound(Arrays.asList(lowerBounds));
	}

	/**
	 * Derive the greatest lower bound for a set of types, as defined in the Java
	 * language specification.
	 * 
	 * @param lowerBounds
	 *          A collection of types representing the lower bounds of an unknown
	 *          type.
	 * @return The most specific single type which, as a lower bound, will also
	 *         satisfy each lower bound in the given set.
	 */
	public static Type greatestLowerBound(Collection<? extends Type> lowerBounds) {
		return IntersectionType.from(lowerBounds);
	}

	/**
	 * Give a canonical String representation of a given type, which is intended
	 * to be more easily human-readable than implementations of
	 * {@link Object#toString()} for certain implementations of {@link Type}.
	 * 
	 * @param type
	 *          The type for which we wish to determine a string representation.
	 * @return A canonical string representation of the given type.
	 */
	public static String toString(Type type) {
		return toString(type, Imports.empty());
	}

	/**
	 * Give a canonical String representation of a given type, which is intended
	 * to be more easily human-readable than implementations of
	 * {@link Object#toString()} for certain implementations of {@link Type}.
	 * Provided class and package imports allow the names of some classes to be
	 * output without full package qualification.
	 * 
	 * @param imports
	 *          classes and packages for which full package qualification may be
	 *          omitted from output
	 * @param type
	 *          the type for which we wish to determine a string representation
	 * @return A canonical string representation of the given type.
	 */
	public static String toString(Type type, Imports imports) {
		return toString(type, imports, new Isomorphism());
	}

	/**
	 * Give a canonical String representation of a given type, which is intended
	 * to be more easily human-readable than implementations of
	 * {@link Object#toString()} for certain implementations of {@link Type}.
	 * Provided class and package imports allow the names of some classes to be
	 * output without full package qualification.
	 * 
	 * @param imports
	 *          classes and packages for which full package qualification may be
	 *          omitted from output
	 * @param type
	 *          the type for which we wish to determine a string representation
	 * @param isomorphism
	 *          an type to string isomorphic mapping to deal with recursion
	 * @return A canonical string representation of the given type.
	 */
	public static String toString(Type type, Imports imports, Isomorphism isomorphism) {
		if (type instanceof Class) {
			if (((Class<?>) type).isArray())
				return new StringBuilder(toString(((Class<?>) type).getComponentType(), imports)).append("[]").toString();
			else
				return imports.getClassName((Class<?>) type);
		} else if (type instanceof ParameterizedType) {
			return ParameterizedTypes.toString((ParameterizedType) type, imports, isomorphism);
		} else if (type instanceof GenericArrayType) {
			return new StringBuilder(toString(((GenericArrayType) type).getGenericComponentType(), imports)).append("[]")
					.toString();
		} else if (type instanceof WildcardType) {
			WildcardType wildcardType = (WildcardType) type;
			StringBuilder builder = new StringBuilder("?");

			appendBounds(builder, wildcardType.getUpperBounds(), wildcardType.getLowerBounds(), imports, isomorphism);

			return builder.toString();
		} else if (type instanceof TypeVariableCapture) {
			TypeVariableCapture typeVariableCapture = (TypeVariableCapture) type;
			StringBuilder builder = new StringBuilder(typeVariableCapture.getName());

			appendBounds(builder, typeVariableCapture.getUpperBounds(), typeVariableCapture.getLowerBounds(), imports,
					isomorphism);

			return builder.toString();
		} else if (type instanceof IntersectionType) {
			return ((IntersectionType) type).toString(imports);
		} else
			return type.getTypeName();
	}

	static String toString(Type[] types, String delimiter, Imports imports, Isomorphism isomorphism) {
		return Arrays.stream(types).map(t -> toString(t, imports, isomorphism)).collect(Collectors.joining(delimiter));
	}

	private static void appendBounds(StringBuilder builder, Type[] upperBounds, Type[] lowerBounds, Imports imports,
			Isomorphism isomorphism) {
		if (upperBounds.length > 0
				&& (upperBounds.length != 1 || (upperBounds[0] != null && !upperBounds[0].equals(Object.class))))
			builder.append(" extends ").append(toString(upperBounds, " & ", imports, isomorphism));

		if (lowerBounds.length > 0 && !(lowerBounds.length == 1 && lowerBounds[0] == null))
			builder.append(" super ").append(toString(lowerBounds, " & ", imports, isomorphism));
	}

	/**
	 * Create a Type instance from a parsed String. Here infinitely recurring
	 * types are represented by, for example:
	 * 
	 * {@code java.util.List<java.lang.Number & java.lang.Comparable<? extends java.lang.Number & java.lang.Comparable<? extends java.lang.Number & java.lang.Comparable
	 * <...>>>>}
	 * 
	 * Where "..." would be substituted, recursively, with the parameterization of
	 * the an outer instance of the same raw class. TODO add clarity, and a proper
	 * description of how ambiguity is resolved here.
	 * 
	 * @param typeString
	 *          The String to parse.
	 * @return The type described by the String.
	 */
	public static Type fromString(String typeString) {
		return fromString(typeString, Imports.empty());
	}

	/**
	 * Create a Type instance from a parsed String. Here infinitely recurring
	 * types are represented by, for example:
	 * 
	 * {@code java.util.List<java.lang.Number & java.lang.Comparable<? extends java.lang.Number & java.lang.Comparable<? extends java.lang.Number & java.lang.Comparable
	 * <...>>>>}
	 * 
	 * Where "..." would be substituted, recursively, with the parameterization of
	 * the an outer instance of the same raw class. TODO add clarity, and a proper
	 * description of how ambiguity is resolved here.
	 * 
	 * @param typeString
	 *          The String to parse.
	 * @param imports
	 *          Classes and packages for which full package qualification may be
	 *          omitted from input.
	 * @return The type described by the String.
	 */
	private static Type fromString(String typeString, Imports imports) {
		return new TypeParser(imports).getClassType().parse(typeString);
	}

	/**
	 * Get the default type parser. All type names will need to be fully qualified
	 * to correctly parse.
	 * 
	 * @return The default annotated type parser
	 */
	public static TypeParser getParser() {
		return TYPE_PARSER;
	}

	/**
	 * Get a type parser with knowledge of the given imports. Type names may omit
	 * full qualification if those types are imported according to the given
	 * imports.
	 * 
	 * @param imports
	 *          A list of imports the type parser should be aware of
	 * @return A type parser with knowledge of the given imports
	 */
	public static TypeParser getParser(Imports imports) {
		return new TypeParser(imports);
	}

	/**
	 * A parser for {@link Type}s, and various related types.
	 * 
	 * @author Elias N Vasylenko
	 */
	public static class TypeParser {
		private final Parser<Class<?>> rawType;

		private final Parser<Type> classOrArrayType;
		private final Parser<WildcardType> wildcardType;
		private final Parser<Type> typeParameter;

		private TypeParser(Imports imports) {
			rawType = Parser.matching("[_a-zA-Z][_a-zA-Z0-9]*(\\.[_a-zA-Z][_a-zA-Z0-9]*)*").transform(imports::getNamedClass);

			classOrArrayType = rawType.transform(Type.class::cast)
					.tryAppendTransform(
							Parser.list(Parser.proxy(this::getType), "\\s*,\\s*").prepend("\\s*<\\s*").append("\\s*>\\s*"),
							(t, p) -> ParameterizedTypes.parameterize((Class<?>) t, p))
					.appendTransform(Parser.list(Parser.matching("\\s*\\[\\s*\\]"), "\\s*").prepend("\\s*"), (t, l) -> {
						t = ArrayTypes.fromComponentType(t, l.size());
						return t;
					});

			wildcardType = Parser.matching("\\s*\\?\\s*extends(?![_a-zA-Z0-9])\\s*")
					.appendTransform(Parser.list(classOrArrayType, "\\s*\\&\\s*"), (s, t) -> WildcardTypes.upperBounded(t))
					.orElse(Parser.matching("\\s*\\?\\s*super(?![_a-zA-Z0-9])\\s*")
							.appendTransform(Parser.list(classOrArrayType, "\\s*\\&\\s*"), (s, t) -> WildcardTypes.lowerBounded(t)))
					.orElse(Parser.matching("\\s*\\?").transform(s -> WildcardTypes.unbounded()));

			typeParameter = classOrArrayType.orElse(wildcardType.transform(Type.class::cast));
		}

		/**
		 * A parser for raw class types.
		 * 
		 * @return The raw type of the parsed type name
		 */
		public Parser<Class<?>> getRawType() {
			return rawType;
		}

		/**
		 * A parser for a class type, which may be parameterized.
		 * 
		 * @return The type of the expressed name, and the given parameterization
		 *         where appropriate
		 */
		public Parser<Type> getClassType() {
			return classOrArrayType;
		}

		/**
		 * A parser for a wildcard type.
		 * 
		 * @return The type of the expressed wildcard type
		 */
		public Parser<WildcardType> getWildcardType() {
			return wildcardType;
		}

		/**
		 * A parser for a class type or wildcard type.
		 * 
		 * @return The annotated type of the expressed type
		 */
		public Parser<Type> getType() {
			return typeParameter;
		}
	}
}
