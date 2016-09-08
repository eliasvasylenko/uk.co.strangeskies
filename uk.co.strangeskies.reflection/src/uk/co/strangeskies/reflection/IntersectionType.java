/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.osgi.annotation.versioning.ProviderType;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;

/**
 * An intersection type, as described in the Java 8 language specification.
 * Roughly, such a type generally behaves as a class which extends each of its
 * component types, but is otherwise an empty definition.
 * 
 * @author Elias N Vasylenko
 */
@ProviderType
public abstract class IntersectionType implements Type {
	private static class IntersectionTypeProxy extends IntersectionType {
		private final Supplier<IntersectionType> source;

		public IntersectionTypeProxy(Supplier<IntersectionType> source) {
			this.source = source;
		}

		@Override
		public Type[] getTypes() {
			return source.get().getTypes();
		}

		@Override
		public String toString(Imports imports) {
			return source.get().toString(imports);
		}
	}

	IntersectionType() {}

	/**
	 * @return Each type which is a member of this intersection.
	 */
	public abstract Type[] getTypes();

	/**
	 * @param source
	 *          A supplier of the intersection type we wish to proxy.
	 * @return A proxy for an intersection type, forwarding to the instance
	 *         provided by the given supplier at the moment of each invocation.
	 *         This is generally useful for algorithms which deal with infinite
	 *         types.
	 */
	public static IntersectionType proxy(Supplier<IntersectionType> source) {
		return new IntersectionTypeProxy(source);
	}

	/**
	 * @param types
	 *          The set of types from which to derive a new intersection type.
	 * @return An intersection type containing each of the given types, or a
	 *         single type, if they can all be represented as such.
	 */
	public static Type from(Type... types) {
		return from(Arrays.asList(types));
	}

	/**
	 * @param types
	 *          The set of types from which to derive a new intersection type.
	 * @return An intersection type containing each of the given types, or a
	 *         single type, if they can all be represented as such.
	 */
	public static Type from(Collection<? extends Type> types) {
		return from(types, new BoundSet());
	}

	/*
	 * Ensure no member is itself an intersection type.
	 */
	private static List<Type> flatten(Collection<? extends Type> types) {
		List<Type> flattenedTypes = new ArrayList<>(types);

		for (int i = 0; i < flattenedTypes.size(); i++) {
			Type type = flattenedTypes.get(i);

			if (Object.class.equals(type)) {
				flattenedTypes.remove(i);
			} else if (type instanceof IntersectionType) {
				flattenedTypes.remove(i);
				flattenedTypes.addAll(Arrays.asList(((IntersectionType) type).getTypes()));
			} else {
				i++;
			}
		}

		return flattenedTypes;
	}

	/*
	 * remove redundancies and order so that the remaining class is at the front,
	 * if one exists.
	 */
	private static void orderAndMinimise(final List<Type> flattenedTypes) {
		Map<Type, Boolean> properType = new IdentityHashMap<>();
		for (Type type : flattenedTypes) {
			properType.put(type, InferenceVariable.isProperType(type));
		}

		/*
		 * Loop through each pair of types, removing those which are redundant due
		 * to subtype relation.
		 * 
		 * At same time, search for the most specific class type.
		 */
		int mostSpecificIndex = -1;
		for (int i = 0; i < flattenedTypes.size(); i++) {
			Type iType = flattenedTypes.get(i);
			if (properType.get(iType)) {
				/*
				 * For each proper type i
				 */

				boolean iInterface = Types.getRawType(iType).isInterface();

				if (mostSpecificIndex == -1 && !iInterface) {
					mostSpecificIndex = i;
				}

				for (int j = i + 1; j < flattenedTypes.size(); j++) {
					Type jType = flattenedTypes.get(j);
					if (properType.get(jType)) {
						/*
						 * For each proper type j, coming after i
						 */

						if (Types.isAssignable(iType, jType)) {
							flattenedTypes.remove(j--);

							if (mostSpecificIndex > j) {
								mostSpecificIndex--;
							}
						} else if (Types.isAssignable(jType, iType)) {
							if (i == mostSpecificIndex) {
								mostSpecificIndex = j;
							}

							flattenedTypes.remove(i--);

							if (mostSpecificIndex >= i) {
								mostSpecificIndex--;
							}
							break;
						} else if (!iInterface && !Types.getRawType(jType).isInterface()) {
							throw new ReflectionException(p -> p.invalidIntersectionTypes(flattenedTypes, iType, jType));
						}
					}
				}
			}
		}

		if (mostSpecificIndex >= 0) {
			Type mostSpecificType = flattenedTypes.remove(mostSpecificIndex);
			flattenedTypes.add(0, mostSpecificType);
		}
	}

	/**
	 * Create an intersection type from the given types, with leniency towards
	 * validation of intersections between types which may contain inference
	 * variables according to the given bound set.
	 * 
	 * @param types
	 *          The set of types from which to derive a new intersection type.
	 * @param bounds
	 *          The bound set which provides context for any inference variables
	 *          which may be mentioned by the given types.
	 * @return An intersection type containing each of the given types, or a
	 *         single type, if they can all be represented as such.
	 */
	public static Type from(Collection<? extends Type> types, BoundSet bounds) {
		List<Type> flattenedTypes = flatten(types);

		if (flattenedTypes.isEmpty())
			return Object.class;

		if (flattenedTypes.size() == 1)
			return flattenedTypes.iterator().next();

		orderAndMinimise(flattenedTypes);

		if (flattenedTypes.isEmpty())
			return Object.class;

		if (flattenedTypes.size() == 1)
			return flattenedTypes.iterator().next();

		try {
			bounds = bounds.copy();
			InferenceVariable inferenceVariable = new InferenceVariable();
			bounds.addInferenceVariable(inferenceVariable);

			for (Type type : flattenedTypes)
				ConstraintFormula.reduce(Kind.SUBTYPE, inferenceVariable, type, bounds);
		} catch (Exception e) {
			throw new ReflectionException(p -> p.invalidIntersectionType(flattenedTypes), e);
		}

		return fromImpl(flattenedTypes);
	}

	static IntersectionType uncheckedFrom(Type... types) {
		return uncheckedFrom(Arrays.asList(types));
	}

	static IntersectionType uncheckedFrom(Collection<? extends Type> types) {
		return fromImpl(types);
	}

	static IntersectionType fromImpl(Collection<? extends Type> types) {
		return new IntersectionType() {
			String string;
			final Type[] typeArray = types.toArray(new Type[types.size()]);

			@Override
			public Type[] getTypes() {
				return typeArray;
			}

			@Override
			public String toString(Imports imports) {
				if (string == null) {
					string = "...";

					string = Arrays.stream(getTypes()).map(t -> {
						String typeName = Types.toString(t, imports);
						if (t instanceof TypeVariableCapture)
							typeName = new StringBuilder().append("[ ").append(typeName).append(" ]").toString();
						return typeName;
					}).collect(Collectors.joining(" & "));
				}

				return string;
			}
		};
	}

	@Override
	public String toString() {
		return toString(Imports.empty());
	}

	/**
	 * Give a canonical String representation of an intersection type which
	 * supports infinite types. Provided class and package imports allow the names
	 * of some classes to be output without full package qualification.
	 * 
	 * @param imports
	 *          Classes and packages for which full package qualification may be
	 *          omitted from output.
	 * @return A canonical string representation of the given type.
	 */
	public abstract String toString(Imports imports);

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof IntersectionType))
			return false;

		IntersectionType that = (IntersectionType) obj;
		return Arrays.equals(this.getTypes(), that.getTypes());
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(getTypes());
	}
}
