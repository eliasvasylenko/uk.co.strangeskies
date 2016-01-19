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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
	IntersectionType() {}

	/**
	 * @return Each type which is a member of this intersection.
	 */
	public abstract Type[] getTypes();

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
		List<Type> flattenedTypes = new ArrayList<>(types);

		for (Type type : new ArrayList<>(flattenedTypes)) {
			if (type instanceof IntersectionType) {
				flattenedTypes.remove(type);
				flattenedTypes
						.addAll(Arrays.asList(((IntersectionType) type).getTypes()));
			}
		}

		if (flattenedTypes.isEmpty())
			return Object.class;

		if (flattenedTypes.size() == 1)
			return flattenedTypes.iterator().next();

		Type mostSpecificType = null;

		for (Type type : new ArrayList<>(flattenedTypes)) {
			if (InferenceVariable.isProperType(type)) {
				Class<?> rawType = Types.getRawType(type);

				if (!rawType.isInterface()) {
					flattenedTypes.remove(type);

					if (mostSpecificType == null
							|| Types.isAssignable(type, mostSpecificType)) {
						mostSpecificType = type;
					} else if (!Types.isAssignable(mostSpecificType, type)) {
						throw new TypeException(
								"Illegal intersection type '" + flattenedTypes
										+ "', cannot contain both of the non-interface classes '"
										+ mostSpecificType + "' and '" + type + "'.");
					}
				}
			}
		}

		if (mostSpecificType != null)
			flattenedTypes.add(0, mostSpecificType);

		for (int i = 0; i < flattenedTypes.size(); i++) {
			Type iType = flattenedTypes.get(i);

			if (InferenceVariable.isProperType(iType))
				for (int j = i + 1; j < flattenedTypes.size(); j++) {
					Type jType = flattenedTypes.get(j);

					if (InferenceVariable.isProperType(jType))
						if (Types.isAssignable(iType, jType))
							flattenedTypes.remove(j--);
						else if (Types.isAssignable(jType, iType)) {
							flattenedTypes.remove(i--);
							break;
						}
				}
		}

		flattenedTypes.remove(Object.class);

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
			throw new TypeException(
					"Illegal intersection type '" + flattenedTypes + "'.", e);
		}

		return uncheckedFrom(flattenedTypes);
	}

	static IntersectionType uncheckedFrom(Type... types) {
		return uncheckedFrom(Arrays.asList(types));
	}

	static IntersectionType uncheckedFrom(Collection<? extends Type> types) {
		return new IntersectionType() {
			Type[] typeArray = types.toArray(new Type[types.size()]);

			@Override
			public Type[] getTypes() {
				return typeArray;
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
	public String toString(Imports imports) {
		return Arrays.stream(getTypes()).map(t -> {
			String typeName = Types.toString(t, imports);
			if (t instanceof TypeVariableCapture)
				typeName = new StringBuilder().append("[ ").append(typeName)
						.append(" ]").toString();
			return typeName;
		}).collect(Collectors.joining(" & "));
	}

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
