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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import uk.co.strangeskies.utilities.IdentityComparator;
import uk.co.strangeskies.utilities.IdentityProperty;

/**
 * A TypeSubstitution object is a function mapping Type to Type, which
 * recursively visits each type mentioned by a given type and applies a
 * substitution to those it encounters which match a given condition.
 * 
 * @author Elias N Vasylenko
 *
 */
public class TypeSubstitution {
	private final Function<? super Type, ? extends Type> mapping;

	/**
	 * Create a new TypeSubstitution with no initial substitution rules.
	 */
	public TypeSubstitution() {
		mapping = t -> null;
	}

	/**
	 * Create a new TypeSubstitution to apply the given mapping function.
	 * Typically we might create an instance from a {@link Map} of Type instances
	 * to other Type instances by passing the method reference of
	 * {@link Map#get(Object)} for that map to this constructor.
	 * 
	 * @param mapping
	 *          A mapping function for transforming encountered types to their
	 *          substitution types.
	 */
	public TypeSubstitution(Function<? super Type, ? extends Type> mapping) {
		this.mapping = mapping;
	}

	/**
	 * Create a new TypeSubstitution by adding a specific single substitution rule
	 * to the receiver of the invocation. The new rule will be checked and applied
	 * before any existing rules. The receiving TypeSubstitution of invocation of
	 * this method will remain unchanged.
	 * 
	 * @param from
	 *          The type to match in application of this rule.
	 * @param to
	 *          The type to substitute for types which match the rule.
	 * @return A new TypeSubstitution object with the rule added.
	 */
	public TypeSubstitution where(Type from, Type to) {
		return new TypeSubstitution(t -> Objects.equals(from, t) ? to
				: mapping.apply(t));
	}

	/**
	 * Create a new TypeSubstitution by adding a specific single substitution rule
	 * to the receiver of the invocation. The new rule will be checked and applied
	 * before any existing rules. The receiving TypeSubstitution of invocation of
	 * this method will remain unchanged.
	 * 
	 * @param from
	 *          The type matching condition of the new rule.
	 * @param to
	 *          The substitution transformation to apply to types matching the
	 *          given condition.
	 * @return A new TypeSubstitution object with the rule added.
	 */
	public TypeSubstitution where(Predicate<Type> from, Function<Type, Type> to) {
		return new TypeSubstitution(t -> from.test(t) ? to.apply(t)
				: mapping.apply(t));
	}

	/**
	 * Resolve the result of this substitution as applied to the given type.
	 * 
	 * @param type
	 *          The type for which we want to make a substitution.
	 * @return The result of application of this substitution. The result
	 *         <em>is</em> guaranteed to be well formed with respect to bounds,
	 *         otherwise an exception will be thrown.
	 */
	public TypeToken<?> resolve(TypeToken<?> type) {
		return TypeToken.of(resolve(type.getType()));
	}

	/**
	 * Resolve the result of this substitution as applied to the given type.
	 * 
	 * @param type
	 *          The type for which we want to make a substitution.
	 * @return The result of application of this substitution. The result is
	 *         <em>not</em> guaranteed to be well formed with respect to bounds.
	 */
	public Type resolve(Type type) {
		return resolve(type, new TreeMap<>(
				new IdentityComparator<ParameterizedType>()));
	}

	private Type resolve(Type type,
			Map<ParameterizedType, ParameterizedType> visited) {
		Type mapping = this.mapping.apply(type);
		if (mapping != null)
			return mapping;
		else if (type == null)
			return null;
		else if (type instanceof Class)
			return type;
		else if (type instanceof TypeVariable)
			return type;
		else if (type instanceof InferenceVariable)
			return type;
		else if (type instanceof IntersectionType)
			return IntersectionType.uncheckedFrom(Arrays
					.stream(((IntersectionType) type).getTypes())
					.map(t -> resolve(t, visited)).collect(Collectors.toList()));
		else if (type instanceof WildcardType) {
			WildcardType wildcardType = (WildcardType) type;

			if (wildcardType.getLowerBounds().length > 0) {
				return WildcardTypes.lowerBounded(resolve(
						IntersectionType.uncheckedFrom(wildcardType.getLowerBounds()),
						visited));

			} else if (wildcardType.getUpperBounds().length > 0) {
				return WildcardTypes.upperBounded(resolve(
						IntersectionType.from(wildcardType.getUpperBounds()), visited));

			} else
				return WildcardTypes.unbounded();
		} else if (type instanceof GenericArrayType)
			return GenericArrayTypes.fromComponentType(resolve(
					((GenericArrayType) type).getGenericComponentType(), visited));
		else if (type instanceof ParameterizedType)
			return resolveParameterizedType((ParameterizedType) type, visited);

		throw new IllegalArgumentException("Cannot resolve unrecognised type '"
				+ type + "' of class'" + type.getClass() + "'.");
	}

	private Type resolveParameterizedType(ParameterizedType type,
			Map<ParameterizedType, ParameterizedType> visited) {
		/*
		 * Here we deal with recursion in infinite types.
		 */
		if (visited.containsKey(type))
			return visited.get(type);

		IdentityProperty<ParameterizedType> result = new IdentityProperty<>();
		visited.put(type, ParameterizedTypes.proxy(result));

		result.set(ParameterizedTypes.uncheckedFrom(
				resolve(type.getOwnerType(), visited), Types.getRawType(type), Arrays
						.stream(type.getActualTypeArguments())
						.map(t -> resolve(t, visited)).collect(Collectors.toList())));

		return result.get();
	}
}
