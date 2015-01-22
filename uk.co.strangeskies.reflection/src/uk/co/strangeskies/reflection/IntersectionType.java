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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;

public abstract class IntersectionType implements Type {
	IntersectionType() {}

	public abstract Type[] getTypes();

	public static Type from(Type... types) {
		return from(Arrays.asList(types));
	}

	public static Type from(Collection<? extends Type> types) {
		List<Type> flattenedTypes = new ArrayList<>(types);

		for (Type type : new ArrayList<>(flattenedTypes)) {
			if (type instanceof IntersectionType) {
				flattenedTypes.remove(type);
				flattenedTypes.addAll(Arrays.asList(((IntersectionType) type)
						.getTypes()));
			}
		}

		if (flattenedTypes.isEmpty())
			return Object.class;

		if (flattenedTypes.size() == 1)
			return flattenedTypes.iterator().next();

		Type mostSpecificClass = null;
		for (Type type : new ArrayList<>(flattenedTypes)) {
			Class<?> rawType = Types.getRawType(type);
			if (!rawType.isInterface()) {
				flattenedTypes.remove(type);
				if (mostSpecificClass == null)
					mostSpecificClass = type;
				else if (Types.isAssignable(type, mostSpecificClass))
					mostSpecificClass = type;
				else if (!Types.isAssignable(mostSpecificClass, type))
					throw new TypeInferenceException("Illegal intersection type '"
							+ flattenedTypes
							+ "', cannot contain both of the non-interface classes '"
							+ mostSpecificClass + "' and '" + type + "'.");
			}
		}
		if (mostSpecificClass != null) {
			flattenedTypes.add(0, mostSpecificClass);
		}

		try {
			BoundSet bounds = new BoundSet();
			InferenceVariable inferenceVariable = new InferenceVariable() {};
			for (Type type : flattenedTypes) {
				new ConstraintFormula(Kind.SUBTYPE, inferenceVariable, type)
						.reduceInto(bounds);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new TypeInferenceException("Illegal intersection type '"
					+ flattenedTypes + "'.", e);
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
		return Arrays.stream(getTypes()).map(Types::toString)
				.collect(Collectors.joining(" & "));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IntersectionType))
			return false;
		if (obj == this)
			return true;
		IntersectionType that = (IntersectionType) obj;
		return Arrays.equals(this.getTypes(), that.getTypes());
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(getTypes());
	}

	public static Type[] asArray(Type of) {
		if (of instanceof IntersectionType)
			return ((IntersectionType) of).getTypes();
		else
			return new Type[] { of };
	}
}
