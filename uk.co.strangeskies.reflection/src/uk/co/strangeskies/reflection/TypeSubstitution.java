/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,L__   __||  _ `.        / \     |  \   | |  ,-`__`]  ,-`__`]
 *   ( (_`-`   | |   | | ) |       / . \    | . \  | | / .`  `  / .`  `
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `-`.   / /   \ \  | | \ \| || |   | || +--J
 *  \ \__.` /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,-
 *   `.__.-`   L_|   L_|    L_|/_/       \_\L_|   \__|  `-.__.'  `-.__.]
 *                   __    _         _      __      __
 *                 ,`_ `, | |   _   | |  ,-`__`]  ,`_ `,
 *                ( (_`-` | '-.) |  | | / .`  `  ( (_`-`
 *                 `._ `. | +-. <   | || '--.     `._ `.
 *                _   `. \| |  `-`. | || +--J    _   `. \
 *               \ \__.` /| |    \ \| | \ `.__,-\ \__.` /
 *                `.__.-` L_|    L_|L_|  `-.__.] `.__.-`
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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Isomorphism;

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
	private final Supplier<Boolean> empty;
	private final boolean includeTypeVariables;

	/**
	 * Create a new TypeSubstitution with no initial substitution rules.
	 */
	public TypeSubstitution() {
		mapping = t -> null;
		empty = () -> true;
		includeTypeVariables = false;
	}

	/**
	 * Create a new TypeSubstitution to apply the given mapping function.
	 * Typically we do something like create an instance from a {@link Map} of
	 * Type instances to other Type instances, then pass the method reference of
	 * {@link Map#get(Object)} for that map to this constructor. For this specific
	 * example use case though, {@link #TypeSubstitution(Map)} would perform
	 * slightly better.
	 * 
	 * @param mapping
	 *          A mapping function for transforming encountered types to their
	 *          substitution types.
	 */
	public TypeSubstitution(Function<? super Type, ? extends Type> mapping) {
		this.mapping = mapping;
		empty = () -> false;
		includeTypeVariables = false;
	}

	/**
	 * Create a new TypeSubstitution to apply the given mapping. This is more
	 * efficient than the more general {@link #TypeSubstitution(Function)}
	 * constructor, as it can skip type traversal for empty maps.
	 * 
	 * @param mapping
	 *          A mapping function for transforming encountered types to their
	 *          substitution types.
	 */
	public TypeSubstitution(Map<?, ? extends Type> mapping) {
		this.mapping = mapping::get;
		empty = mapping::isEmpty;
		includeTypeVariables = false;
	}

	private TypeSubstitution(TypeSubstitution substitution) {
		mapping = substitution.mapping;
		empty = substitution.empty;
		includeTypeVariables = true;
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
		return new TypeSubstitution(t -> Objects.equals(from, t) ? to : mapping.apply(t));
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
	public TypeSubstitution where(Predicate<? super Type> from, Function<? super Type, ? extends Type> to) {
		return new TypeSubstitution(t -> from.test(t) ? to.apply(t) : mapping.apply(t));
	}

	/**
	 * Create a new TypeSubstitution which is the same as the receiver with the
	 * additional behavior that type variables are also included for bounds
	 * substitution. Normally it makes sense to exclude type variable bounds for
	 * substitution, as their type is immutable and baked into their defining
	 * class definition, but sometimes we may still want this behavior.
	 * 
	 * @return A new TypeSubstitution object with the rule added.
	 */
	public TypeSubstitution withTypeVariables() {
		return new TypeSubstitution(this);
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
		if (empty.get())
			return type;
		else
			return resolve(type, new Isomorphism());
	}

	/**
	 * Resolve the result of this substitution as applied to the given type.
	 * 
	 * @param type
	 *          The type for which we want to make a substitution.
	 * @param isomorphism
	 *          the isomorphism for dealing with self bounded and infinite types
	 * @return The result of application of this substitution. The result is
	 *         <em>not</em> guaranteed to be well formed with respect to bounds.
	 */
	public Type resolve(Type type, Isomorphism isomorphism) {
		return resolve(type, isomorphism, new IdentityProperty<>(false));
	}

	protected Type resolve(Type type, Isomorphism isomorphism, IdentityProperty<Boolean> changed) {
		Type mapping = this.mapping.apply(type);
		if (mapping != null) {
			if (mapping != type) {
				changed.set(true);
			}
			return mapping;

		} else {
			if (changed.get()) {
				changed = new IdentityProperty<>(false);
			}

			if (type == null) {
				return null;

			} else if (type instanceof Class) {
				return type;

			} else if (type instanceof TypeVariable<?>) {
				return includeTypeVariables ? resolveTypeVariable((TypeVariable<?>) type, isomorphism, changed) : type;

			} else if (type instanceof InferenceVariable) {
				return type;

			} else if (type instanceof IntersectionType) {
				return resolveIntersectionType((IntersectionType) type, isomorphism, changed);

			} else if (type instanceof WildcardType) {
				return resolveWildcardType((WildcardType) type, isomorphism, changed);

			} else if (type instanceof GenericArrayType) {
				return ArrayTypes
						.fromComponentType(resolve(((GenericArrayType) type).getGenericComponentType(), isomorphism, changed));

			} else if (type instanceof ParameterizedType) {
				return resolveParameterizedType((ParameterizedType) type, isomorphism, changed);
			}
		}

		throw new IllegalArgumentException(
				"Cannot resolve unrecognised type '" + type + "' of class'" + type.getClass() + "'.");
	}

	private Type resolveTypeVariable(TypeVariable<?> type, Isomorphism isomorphism, IdentityProperty<Boolean> changed) {
		return isomorphism.byIdentity().getProxiedMapping(type, TypeVariable.class, i -> {

			if (type.getBounds().length > 0) {
				List<Type> bounds = resolveTypes(type.getBounds(), isomorphism, changed);
				if (changed.get()) {
					return TypeVariables.upperBounded(type.getGenericDeclaration(), type.getName(), AnnotatedTypes.over(bounds));
				} else {
					return type;
				}

			} else
				return type;
		});
	}

	private Type resolveWildcardType(WildcardType type, Isomorphism isomorphism, IdentityProperty<Boolean> changed) {
		return isomorphism.byIdentity().getProxiedMapping(type, WildcardType.class, i -> {

			if (type.getLowerBounds().length > 0) {
				List<Type> bounds = resolveTypes(type.getLowerBounds(), isomorphism, changed);
				if (changed.get()) {
					return WildcardTypes.lowerBounded(bounds);
				} else {
					return type;
				}

			} else if (type.getUpperBounds().length > 0) {
				List<Type> bounds = resolveTypes(type.getUpperBounds(), isomorphism, changed);
				if (changed.get()) {
					return WildcardTypes.upperBounded(bounds);
				} else {
					return type;
				}

			} else
				return type;
		});
	}

	private Type resolveIntersectionType(IntersectionType type, Isomorphism isomorphism,
			IdentityProperty<Boolean> changed) {
		return isomorphism.byIdentity().getPartialMapping(type, (i, partial) -> {

			IdentityProperty<IntersectionType> property = new IdentityProperty<>();
			Type proxy = IntersectionType.proxy(property::get);
			partial.accept(() -> proxy);

			IntersectionType result;

			List<Type> types = resolveTypes(type.getTypes(), isomorphism, changed);
			if (changed.get()) {
				result = IntersectionType.uncheckedFrom(types);
			} else {
				result = type;
			}

			property.set(result);

			return result;
		});
	}

	private Type resolveParameterizedType(ParameterizedType type, Isomorphism isomorphism,
			IdentityProperty<Boolean> changed) {
		return isomorphism.byIdentity().getProxiedMapping(type, ParameterizedType.class, i -> {

			List<Type> arguments = resolveTypes(type.getActualTypeArguments(), isomorphism, changed);

			if (changed.get()) {
				return ParameterizedTypes.uncheckedFrom(resolve(type.getOwnerType(), isomorphism), Types.getRawType(type),
						arguments);
			} else {
				return type;
			}
		});
	}

	private List<Type> resolveTypes(Type[] types, Isomorphism isomorphism, IdentityProperty<Boolean> changed) {
		return Arrays.stream(types).map(t -> resolve(t, isomorphism, changed)).collect(Collectors.toList());
	}
}
