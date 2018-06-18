/*
 * Copyright (C) 2018 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

import uk.co.strangeskies.property.IdentityProperty;
import uk.co.strangeskies.reflection.inference.InferenceVariable;
import uk.co.strangeskies.reflection.model.ExtendedTypes;
import uk.co.strangeskies.reflection.model.TypeMirrorProxy;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * A TypeSubstitution object is a function mapping Type to Type, which
 * recursively visits each type mentioned by a given type and applies a
 * substitution to those it encounters which match a given condition.
 * 
 * @author Elias N Vasylenko
 *
 */
public class TypeSubstitution {
	private final ExtendedTypes types;
	private final Isomorphism isomorphism;
	private final Function<? super TypeMirror, ? extends TypeMirror> mapping;
	private final Supplier<Boolean> empty;

	/**
	 * Create a new TypeSubstitution with no initial substitution rules.
	 */
	public TypeSubstitution(ExtendedTypes types) {
		this.types = types;

		isomorphism = new Isomorphism();
		mapping = t -> null;
		empty = () -> true;
	}

	/**
	 * Create a new TypeSubstitution to apply the given mapping function. Typically
	 * we do something like create an instance from a {@link Map} of Type instances
	 * to other Type instances, then pass the method reference of
	 * {@link Map#get(Object)} for that map to this constructor. For this specific
	 * example use case though, {@link #TypeSubstitution(ExtendedTypes,Map)} would
	 * perform slightly better.
	 * 
	 * @param mapping A mapping function for transforming encountered types to their
	 *                substitution types.
	 */
	public TypeSubstitution(ExtendedTypes types, Function<? super TypeMirror, ? extends TypeMirror> mapping) {
		this.types = types;

		isomorphism = new Isomorphism();
		this.mapping = mapping;
		empty = () -> false;
	}

	/**
	 * Create a new TypeSubstitution to apply the given mapping. This is more
	 * efficient than the more general
	 * {@link #TypeSubstitution(ExtendedTypes,Function)} constructor, as it can skip
	 * type traversal for empty maps.
	 * 
	 * @param mapping A mapping function for transforming encountered types to their
	 *                substitution types.
	 */
	public TypeSubstitution(ExtendedTypes types, Map<? extends TypeMirror, ? extends TypeMirror> mapping) {
		this.types = types;

		isomorphism = new Isomorphism();
		this.mapping = mapping::get;
		empty = mapping::isEmpty;
	}

	private TypeSubstitution(TypeSubstitution substitution) {
		this.types = substitution.types;
		isomorphism = substitution.isomorphism;
		mapping = substitution.mapping;
		empty = substitution.empty;
	}

	private TypeSubstitution(TypeSubstitution substitution, Isomorphism isomorphism) {
		this.types = substitution.types;
		this.isomorphism = isomorphism;
		this.mapping = substitution.mapping;
		this.empty = () -> false;
	}

	/**
	 * Create a new TypeSubstitution by adding a specific single substitution rule
	 * to the receiver of the invocation. The new rule will be checked and applied
	 * before any existing rules. The receiving TypeSubstitution of invocation of
	 * this method will remain unchanged.
	 * 
	 * @param from The type to match in application of this rule.
	 * @param to   The type to substitute for types which match the rule.
	 * @return A new TypeSubstitution object with the rule added.
	 */
	public TypeSubstitution where(TypeMirror from, TypeMirror to) {
		return where(t -> Objects.equals(from, t), t -> to);
	}

	/**
	 * Create a new TypeSubstitution by adding a specific single substitution rule
	 * to the receiver of the invocation. The new rule will be checked and applied
	 * before any existing rules. The receiving TypeSubstitution of invocation of
	 * this method will remain unchanged.
	 * 
	 * @param from The type matching condition of the new rule.
	 * @param to   The substitution transformation to apply to types matching the
	 *             given condition.
	 * @return A new TypeSubstitution object with the rule added.
	 */
	public TypeSubstitution where(Predicate<? super TypeMirror> from,
			Function<? super TypeMirror, ? extends TypeMirror> to) {
		return new TypeSubstitution(types, t -> {
			TypeMirror result = null;
			if (from.test(t)) {
				result = to.apply(t);
			}
			if (result == null) {
				result = mapping.apply(t);
			}
			return result;
		});
	}

	/**
	 * Create a new TypeSubstitution which is the same as the receiver with the
	 * additional behavior that type variables are also included for bounds
	 * substitution. Normally it makes sense to exclude type variable bounds for
	 * substitution, as their type is immutable and baked into their defining class
	 * definition, but sometimes we may still want this behavior.
	 * 
	 * @return a new TypeSubstitution object with the rule added
	 */
	public TypeSubstitution withTypeVariables() {
		return new TypeSubstitution(this);
	}

	/**
	 * Create a new TypeSubstitution which is the same as the receiver with the
	 * additional behavior that it maps types according to the given
	 * {@link Isomorphism}.
	 * 
	 * @param isomorphism an isomorphism
	 * @return a new TypeSubstitution object over the given isomorphism
	 */
	public TypeSubstitution withIsomorphism(Isomorphism isomorphism) {
		return new TypeSubstitution(this, isomorphism);
	}

	/**
	 * Resolve the result of this substitution as applied to the given type.
	 * 
	 * @param type The type for which we want to make a substitution.
	 * @return The result of application of this substitution. The result is
	 *         <em>not</em> guaranteed to be well formed with respect to bounds.
	 */
	public TypeMirror resolve(TypeMirror type) {
		if (empty.get())
			return type;
		else
			return resolve(type, new IdentityProperty<>(false));
	}

	protected TypeMirror resolve(TypeMirror type, IdentityProperty<Boolean> changed) {
		if (isomorphism.byIdentity().getMappedNodes().contains(type)) {
			TypeMirror mapping = (TypeMirror) isomorphism.byIdentity().getMapping(type);
			if (mapping != type) {
				changed.set(true);
			}
			return mapping;

		} else {
			TypeMirror mapping = this.mapping.apply(type);
			if (mapping != null) {
				if (mapping != type) {
					changed.set(true);
				}
				return mapping;

			} else {
				if (changed.get()) {
					changed = new IdentityProperty<>(false);
				}

				switch (type.getKind()) {
				case NULL:
				case VOID:
					return type;

				case DECLARED:
					return resolveDeclaredType((DeclaredType) type, changed);

				case INTERSECTION:
					return resolveIntersectionType((IntersectionType) type, changed);

				case UNION:
					return resolveUnionType((UnionType) type, changed);

				case WILDCARD:
					return resolveWildcardType((WildcardType) type, changed);

				case ARRAY:
					return resolveArrayType((ArrayType) type, changed);

				case OTHER:
					if (type instanceof InferenceVariable) {
						return resolveType(type);
					} else {
						throw null;
					}
				case ERROR:
					break;
				case EXECUTABLE:
					break;
				case TYPEVAR:
				case BOOLEAN:
				case BYTE:
				case CHAR:
				case DOUBLE:
				case FLOAT:
				case INT:
				case LONG:
				case MODULE:
				case NONE:
				case PACKAGE:
				case SHORT:
				default:
					break;
				}
			}

			throw new IllegalArgumentException(
					"Cannot resolve unrecognised type '" + type + "' of class'" + type.getClass() + "'.");
		}
	}

	private TypeMirror resolveType(TypeMirror type) {
		return isomorphism.byIdentity().getMapping(type, Function.identity());
	}

	private ArrayType resolveArrayType(ArrayType type, IdentityProperty<Boolean> changedScoped) {
		return isomorphism.byIdentity().getMapping(type,
				t -> types.getArrayType(resolve(t.getComponentType(), changedScoped)));
	}

	private WildcardType resolveWildcardType(WildcardType type, IdentityProperty<Boolean> changed) {
		return isomorphism.byIdentity().getProxiedMapping(type, WildcardType.class, i -> {

			if (type.getSuperBound() != null) {
				TypeMirror bounds = resolve(type.getSuperBound(), changed);
				if (changed.get()) {
					return types.getWildcardType(null, bounds);
				} else {
					return type;
				}

			} else if (type.getExtendsBound() != null) {
				TypeMirror bounds = resolve(type.getExtendsBound(), changed);
				if (changed.get()) {
					return types.getWildcardType(bounds, null);
				} else {
					return type;
				}

			} else
				return type;
		});
	}

	private TypeMirror resolveIntersectionType(IntersectionType type, IdentityProperty<Boolean> changed) {
		return isomorphism.byIdentity().getPartialMapping(type, (i, partial) -> {

			TypeMirrorProxy proxy = types.getProxy();
			partial.accept(() -> proxy);

			TypeMirror result;

			List<TypeMirror> bounds = type.getBounds().stream().map(bound -> resolve(bound, changed)).collect(toList());
			if (changed.get()) {
				result = types.getIntersection(bounds);
			} else {
				result = type;
			}

			proxy.setInstance(result);

			return proxy;
		});
	}

	private TypeMirror resolveUnionType(UnionType type, IdentityProperty<Boolean> changed) {
		return isomorphism.byIdentity().getPartialMapping(type, (i, partial) -> {

			TypeMirrorProxy proxy = types.getProxy();
			partial.accept(() -> proxy);

			TypeMirror result;

			List<TypeMirror> alternatives = type.getAlternatives().stream().map(bound -> resolve(bound, changed))
					.collect(toList());
			if (changed.get()) {
				result = types.getUnion(alternatives);
			} else {
				result = type;
			}

			proxy.setInstance(result);

			return proxy;
		});
	}

	private DeclaredType resolveDeclaredType(DeclaredType type, IdentityProperty<Boolean> changed) {
		TypeMirrorProxy proxy = types.getProxy();
		return isomorphism.byIdentity().getPartialMapping(type, () -> proxy, i -> {

			TypeElement element = (TypeElement) i.asElement();

			TypeMirror[] arguments = i.getTypeArguments().stream().map(argument -> resolve(argument, changed))
					.toArray(TypeMirror[]::new);

			TypeMirror enclosing = i.getEnclosingType();
			DeclaredType instance;
			if (enclosing.getKind() == TypeKind.DECLARED) {
				enclosing = resolve(enclosing, changed);

				instance = types.getDeclaredType((DeclaredType) enclosing, element, arguments);

			} else {
				instance = types.getDeclaredType(element, arguments);
			}
			proxy.setInstance(instance);
			return proxy;
		});
	}
}
