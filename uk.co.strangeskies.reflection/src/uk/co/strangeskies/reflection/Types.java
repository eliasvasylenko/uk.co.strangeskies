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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.collection.computingmap.CacheComputingMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

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

	/*
	 * A ComputingMap doesn't really make much sense here over a regular Map...
	 * But it doesn't hurt anything, and it gives us soft-references for values
	 * out of the box.
	 */
	private static final CacheComputingMap<Set<ParameterizedType>, IdentityProperty<ParameterizedType>> BEST_PARAMETERIZATIONS = new CacheComputingMap<>(
			c -> new IdentityProperty<>(), true);

	private Types() {}

	public static Set<Class<?>> getRawTypes(Type type) {
		if (type instanceof IntersectionType) {
			return Arrays.stream(((IntersectionType) type).getTypes())
					.flatMap(t -> getRawTypes(t).stream()).collect(Collectors.toSet());
		} else if (type instanceof WildcardType) {
			return getRawTypes(IntersectionType.uncheckedFrom(((WildcardType) type)
					.getUpperBounds()));
		} else
			return new HashSet<>(Arrays.asList(getRawType(type)));
	}

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
			// throw new RuntimeException("fix needed?"); TODO
			// return getRawType(IntersectionType.from(((InferenceVariable) type)
			// .getLowerBounds()));
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

	public static Type fromString(String typeString) {
		// TODO Auto-generated method stub
		return null;
	}

	public static boolean isAssignable(Type from, Type to) {
		if (from == null || from.equals(to) || to == null
				|| to.equals(Object.class)) {
			/*
			 * We can always assign to or from 'null', and we can always assign to
			 * Object.
			 */
			return true;
		} else if (to instanceof TypeVariableCapture) {
			return ((TypeVariableCapture) to).getLowerBounds().length > 0
					&& isAssignable(from,
							IntersectionType.uncheckedFrom(((TypeVariableCapture) to)
									.getLowerBounds()));
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
			return isAssignable(IntersectionType.uncheckedFrom(upperBounds), to);
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

			List<TypeVariable<?>> typeParameters = ParameterizedTypes
					.getAllTypeParameters(matchedClass);
			Map<TypeVariable<?>, Type> toTypeArguments = ParameterizedTypes
					.getAllTypeArguments((ParameterizedType) to);
			Map<TypeVariable<?>, Type> fromTypeArguments = ParameterizedTypes
					.getAllTypeArguments((ParameterizedType) fromParameterization);

			for (TypeVariable<?> parameter : typeParameters) {
				if (!isContainedBy(fromTypeArguments.get(parameter),
						toTypeArguments.get(parameter)))
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

	public static Type leastUpperBound(Type... upperBounds) {
		return leastUpperBound(Arrays.asList(upperBounds));
	}

	public static Type leastUpperBound(Collection<Type> upperBounds) {
		Type upperBound = leastUpperBoundImpl(upperBounds);

		/*
		 * Not sure if this is necessary! But it's cheap enough to check. Can't
		 * validate IntersectionTypes and ParameterizedTypes as we create them, as
		 * they may contain uninitialised proxies in place of ParameterizedTypes.
		 */
		Types.validate(upperBound);

		return upperBound;
	}

	private static Type leastUpperBoundImpl(Collection<Type> upperBounds) {
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
				for (Map.Entry<Class<?>, ParameterizedType> erasedSupertype : erasedSupertypes
						.entrySet())
					if (erasedCandidates.containsKey(erasedSupertype.getKey())
							&& erasedSupertype.getValue() != null)
						erasedCandidates.add(erasedSupertype.getKey(),
								erasedSupertype.getValue());
			}

			minimiseCandidates(erasedCandidates);

			List<Type> bestTypes = erasedCandidates
					.entrySet()
					.stream()
					.map(
							e -> best(e.getKey(),
									new ArrayList<ParameterizedType>(e.getValue())))
					.collect(Collectors.toList());

			return IntersectionType.uncheckedFrom(bestTypes);
		}
	}

	private static void minimiseCandidates(
			MultiMap<Class<?>, ParameterizedType, ? extends Set<ParameterizedType>> erasedCandidates) {
		List<Class<?>> minimalCandidates = new ArrayList<>(
				erasedCandidates.keySet());
		if (minimalCandidates.size() > 1)
			for (int i = 0; i < minimalCandidates.size(); i++)
				for (int j = i + 1; j < minimalCandidates.size(); j++) {
					if (minimalCandidates.get(i).isAssignableFrom(
							minimalCandidates.get(j))) {
						minimalCandidates.remove(i);
						j = i;
					} else if (minimalCandidates.get(j).isAssignableFrom(
							minimalCandidates.get(i))) {
						minimalCandidates.remove(j--);
					}
				}
		erasedCandidates.keySet().retainAll(minimalCandidates);
	}

	private static Type best(Class<?> rawClass,
			List<ParameterizedType> parameterizations) {
		if (parameterizations.isEmpty())
			return rawClass;
		else if (parameterizations.size() == 1) {
			Type parameterization = parameterizations.iterator().next();
			return parameterization == null ? rawClass : parameterization;
		}

		/*
		 * Proxy guard against recursive generation of infinite types
		 */
		IdentityProperty<ParameterizedType> bestResult;
		synchronized (BEST_PARAMETERIZATIONS) {
			if (BEST_PARAMETERIZATIONS.keySet().contains(
					new HashSet<>(parameterizations)))
				return BEST_PARAMETERIZATIONS.get(new HashSet<>(parameterizations))
						.get();

			bestResult = new IdentityProperty<>();
			BEST_PARAMETERIZATIONS.putGet(new HashSet<>(parameterizations)).set(
					ParameterizedTypes.proxy(bestResult));
		}

		Map<TypeVariable<?>, Type> leastContainingParameterization = new HashMap<>();

		List<TypeVariable<?>> typeParameters = ParameterizedTypes
				.getAllTypeParameters(rawClass);
		for (int i = 0; i < parameterizations.size(); i++) {
			ParameterizedType parameterization = parameterizations.get(i);
			for (int j = 0; j < typeParameters.size(); j++) {
				TypeVariable<?> variable = typeParameters.get(j);
				if (parameterization != null) {
					Type argumentU = parameterization.getActualTypeArguments()[j];
					Type argumentV = leastContainingParameterization.get(variable);

					if (argumentV == null)
						leastContainingParameterization.put(variable, argumentU);
					else {
						leastContainingParameterization.put(variable,
								leastContainingArgument(argumentU, argumentV));
					}
				}
			}
			parameterizations.set(i,
					(ParameterizedType) parameterization.getOwnerType());
		}

		ParameterizedType best = (ParameterizedType) ParameterizedTypes
				.uncheckedFrom(rawClass, leastContainingParameterization);

		bestResult.set(best);

		return best;
	}

	private static Type leastContainingArgument(Type argumentU, Type argumentV) {
		if (argumentU instanceof WildcardType
				&& (!(argumentV instanceof WildcardType) || ((WildcardType) argumentV)
						.getUpperBounds().length > 0)) {
			Type swap = argumentU;
			argumentU = argumentV;
			argumentV = swap;
		}

		if (argumentU instanceof WildcardType) {
			if (((WildcardType) argumentU).getUpperBounds().length > 0) {
				if (((WildcardType) argumentV).getUpperBounds().length > 0) {
					/*
					 * lcta(? extends U, ? extends V) = ? extends lub(U, V)
					 */
					return WildcardTypes
							.upperBounded(leastUpperBoundImpl(Arrays.asList(IntersectionType
									.uncheckedFrom(((WildcardType) argumentU).getUpperBounds()),
									IntersectionType.uncheckedFrom(((WildcardType) argumentV)
											.getUpperBounds()))));
				} else {
					/*
					 * lcta(? extends U, ? super V) = U if U = V, otherwise ?
					 */
					return argumentU.equals(argumentV) ? argumentU : WildcardTypes
							.unbounded();
				}
			} else {
				/*
				 * lcta(? super U, ? super V) = ? super glb(U, V)
				 */
				return WildcardTypes.lowerBounded(greatestLowerBound(IntersectionType
						.uncheckedFrom(((WildcardType) argumentU).getLowerBounds()),
						IntersectionType.uncheckedFrom(((WildcardType) argumentV)
								.getLowerBounds())));
			}
		} else if (argumentV instanceof WildcardType) {
			if (((WildcardType) argumentV).getUpperBounds().length > 0) {
				/*
				 * lcta(U, ? extends V) = ? extends lub(U, V)
				 */
				List<Type> bounds = new ArrayList<>(
						Arrays.asList(((WildcardType) argumentV).getUpperBounds()));
				bounds.add(argumentU);
				return WildcardTypes.upperBounded(leastUpperBoundImpl(bounds));
			} else {
				/*
				 * lcta(U, ? super V) = ? super glb(U, V)
				 */
				return WildcardTypes.lowerBounded(greatestLowerBound(argumentU,
						IntersectionType.uncheckedFrom(((WildcardType) argumentV)
								.getLowerBounds())));
			}
		} else {
			/*
			 * lcta(U, V) = U if U = V, otherwise ? extends lub(U, V)
			 */
			return argumentU.equals(argumentV) ? argumentU
					: WildcardTypes.upperBounded(leastUpperBoundImpl(Arrays.asList(
							argumentU, argumentV)));
		}
	}

	private static Map<Class<?>, ParameterizedType> getErasedSupertypes(Type of) {
		Map<Class<?>, ParameterizedType> supertypes = new HashMap<>();

		TypeLiteral<?> ofLiteral = TypeLiteral.from(of);

		RecursiveTypeVisitor
				.build()
				.visitSupertypes()
				.classVisitor(
						type -> {
							Type parameterized = ofLiteral.resolveSupertypeParameters(type)
									.getType();
							supertypes
									.put(
											type,
											(parameterized instanceof ParameterizedType) ? (ParameterizedType) parameterized
													: null);
						})
				.parameterizedTypeVisitor(
						type -> supertypes.put(Types.getRawType(type), type)).create()
				.visit(of);

		return supertypes;
	}

	public static Type greatestLowerBound(Type... lowerBounds) {
		return greatestLowerBound(Arrays.asList(lowerBounds));
	}

	public static Type greatestLowerBound(Collection<? extends Type> lowerBounds) {
		return IntersectionType.from(lowerBounds);
	}
}
