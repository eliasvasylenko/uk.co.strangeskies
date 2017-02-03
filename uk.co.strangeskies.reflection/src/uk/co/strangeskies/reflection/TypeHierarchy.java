package uk.co.strangeskies.reflection;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.empty;
import static uk.co.strangeskies.reflection.ParameterizedTypes.getAllTypeArguments;
import static uk.co.strangeskies.reflection.Types.getRawType;
import static uk.co.strangeskies.utilities.collection.StreamUtilities.flatMapRecursive;
import static uk.co.strangeskies.utilities.collection.StreamUtilities.iterateOptional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

public class TypeHierarchy {
	/**
	 * Determine the recursive sequence of direct supertypes of a given type which
	 * lead to either the given superclass or a parameterization thereof.
	 * 
	 * @param type
	 *          the type providing a context within which to determine the
	 *          arguments of the supertype
	 * @param superclass
	 *          the class of the supertype parameterization we wish to determine
	 * @return a stream returning the given type and then each direct supertype
	 *         recursively until the given superclass, or a parameterization
	 *         thereof, is reached
	 */
	public static Stream<Type> resolveDirectSupertypeHierarchy(Type type, Class<?> superclass) {
		if (!Types.isAssignable(type, superclass)) {
			throw new ReflectionException(p -> p.cannotResolveSupertype(type, superclass));
		}

		return resolveSupertypeHierarchyImpl(type, superclass);
	}

	/**
	 * Determine the recursive sequence of direct supertypes of a given type which
	 * lead to either the given superclass or a parameterization thereof.
	 * 
	 * @param type
	 *          the type providing a context within which to determine the
	 *          arguments of the supertype
	 * @param superclass
	 *          the class of the supertype parameterization we wish to determine
	 * @return a stream returning the given type and then each direct supertype
	 *         recursively until the given superclass, or a parameterization
	 *         thereof, is reached
	 */
	public static Stream<Type> resolveCompleteSupertypeHierarchy(Type type, Class<?> superclass) {
		validateResolvableSupertype(type, superclass);

		Set<Class<?>> encountered = new HashSet<>();

		return flatMapRecursive(type, t -> resolveImmediateSupertypes(encountered, t, superclass));
	}

	/**
	 * Determine the super type of a given type which is either equal to the given
	 * superclass or a parameterization thereof.
	 * 
	 * @param type
	 *          the type providing a context within which to determine the
	 *          arguments of the supertype
	 * @param superclass
	 *          the class of the supertype parameterization we wish to determine
	 * @return the supertype of the requested class
	 */
	public static Type resolveSupertype(Type type, Class<?> superclass) {
		if (!Types.isAssignable(type, superclass)) {
			throw new ReflectionException(p -> p.cannotResolveSupertype(type, superclass));
		} else if (!Types.isGeneric(superclass)) {
			return superclass;
		}

		return resolveSupertypeHierarchyImpl(type, superclass).reduce((a, b) -> b).get();
	}

	/**
	 * Determine the immediate supertypes of the given type.
	 * 
	 * @param type
	 *          the type providing a context within which to determine the
	 *          arguments of the supertypes
	 * @return a stream of the supertypes of the requested class
	 */
	public static Stream<Type> resolveImmediateSupertypes(Type type) {
		return resolveImmediateSupertypes(null, type, Object.class);
	}

	private static Stream<Type> resolveSupertypeHierarchyImpl(Type type, Class<?> superclass) {
		validateResolvableSupertype(type, superclass);

		return iterateOptional(type, t -> resolveImmediateSupertypes(null, t, superclass).findFirst());
	}

	private static void validateResolvableSupertype(Type type, Class<?> superclass) {
		if (!(type instanceof ParameterizedType) && !(type instanceof Class)) {
			throw new ReflectionException(
					p -> p.cannotResolveSupertype(type, superclass),
					new ReflectionException(p -> p.unsupportedType(type)));
		}
	}

	private static Stream<Type> resolveImmediateSupertypes(Set<Class<?>> encountered, Type type, Class<?> superclass) {
		Class<?> subclass = getRawType(type);

		if (subclass.equals(superclass)) {
			return empty();
		}

		if (encountered != null && !encountered.isEmpty()) {
			encountered.remove(subclass);
		}

		Type genericSuperclass = subclass.getGenericSuperclass();
		Type[] genericInterfaces = subclass.getGenericInterfaces();

		List<Type> lesserSubtypes = new ArrayList<>(genericInterfaces.length + 1);
		stream(genericInterfaces).forEach(lesserSubtypes::add);
		if (genericSuperclass != null) {
			lesserSubtypes.add(genericSuperclass);
		} else if (genericInterfaces.length == 0) {
			lesserSubtypes.add(Object.class);
		}

		/*
		 * If there is more than one supertype in evaluation
		 */
		if (encountered != null && (!encountered.isEmpty() || lesserSubtypes.size() > 1)) {
			for (Iterator<Type> lesserSubtypeIterator = lesserSubtypes.iterator(); lesserSubtypeIterator.hasNext();) {
				Class<?> rawClass = getRawType(lesserSubtypeIterator.next());

				if (encountered.stream().anyMatch(rawClass::isAssignableFrom)) {
					lesserSubtypeIterator.remove();
				} else {
					encountered.add(rawClass);
				}
			}
		}

		return lesserSubtypes.stream().filter(t -> superclass.isAssignableFrom(getRawType(t))).map(subtype -> {
			if (type instanceof ParameterizedType)
				return new TypeSubstitution(
						getAllTypeArguments((ParameterizedType) type).collect(toMap(Entry::getKey, Entry::getValue)))
								.resolve(subtype);
			else
				return subtype;
		});
	}
}
