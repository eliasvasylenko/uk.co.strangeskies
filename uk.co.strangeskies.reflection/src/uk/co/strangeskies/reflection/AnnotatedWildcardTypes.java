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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeImpl;

/**
 * A collection of utility methods relating to annotated wildcard types.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedWildcardTypes {
	private static class AnnotatedWildcardTypeImpl extends AnnotatedTypeImpl
			implements AnnotatedWildcardType {
		private final AnnotatedTypeImpl[] annotatedUpperBounds;
		private final AnnotatedTypeImpl[] annotatedLowerBounds;

		public AnnotatedWildcardTypeImpl(Set<TypeVariable<?>> wrapped,
				AnnotatedWildcardType annotatedWildcardType) {
			super(annotatedWildcardType);

			annotatedUpperBounds = AnnotatedTypes.wrapImpl(wrapped,
					annotatedWildcardType.getAnnotatedUpperBounds());
			annotatedLowerBounds = AnnotatedTypes.wrapImpl(wrapped,
					annotatedWildcardType.getAnnotatedLowerBounds());
		}

		public AnnotatedWildcardTypeImpl(WildcardType type,
				Collection<Annotation> annotations) {
			super(type, annotations);

			annotatedUpperBounds = AnnotatedTypes.overImpl(type.getUpperBounds());
			annotatedLowerBounds = AnnotatedTypes.overImpl(type.getLowerBounds());
		}

		public AnnotatedWildcardTypeImpl(
				Collection<? extends AnnotatedType> upperBounds,
				Collection<? extends AnnotatedType> lowerBounds,
				Collection<Annotation> annotations) {
			super(wildcardFrom(upperBounds, lowerBounds), annotations);

			annotatedUpperBounds = AnnotatedTypes
					.wrapImpl(upperBounds.toArray(new AnnotatedType[upperBounds.size()]));
			annotatedLowerBounds = AnnotatedTypes
					.wrapImpl(lowerBounds.toArray(new AnnotatedType[lowerBounds.size()]));
		}

		private static Type wildcardFrom(
				Collection<? extends AnnotatedType> upperBounds,
				Collection<? extends AnnotatedType> lowerBounds) {
			if (!upperBounds.isEmpty())
				return WildcardTypes.upperBounded(upperBounds.stream()
						.map(AnnotatedType::getType).collect(Collectors.toList()));
			else if (!lowerBounds.isEmpty())
				return WildcardTypes.lowerBounded(lowerBounds.stream()
						.map(AnnotatedType::getType).collect(Collectors.toList()));
			else
				return WildcardTypes.unbounded();
		}

		@Override
		public AnnotatedType[] getAnnotatedUpperBounds() {
			return annotatedUpperBounds.clone();
		}

		@Override
		public AnnotatedType[] getAnnotatedLowerBounds() {
			return annotatedLowerBounds.clone();
		}

		@Override
		public WildcardType getType() {
			return (WildcardType) super.getType();
		}

		@Override
		public String toString(Imports imports) {
			StringBuilder builder = new StringBuilder(
					annotationString(imports, getAnnotations()));

			AnnotatedType[] bounds;
			if ((bounds = getAnnotatedUpperBounds()).length > 1 || (bounds.length == 1
					&& !bounds[0].equals(AnnotatedTypes.over(Object.class)))) {
				builder.append("? extends ");
			} else if ((bounds = getAnnotatedLowerBounds()).length > 0) {
				builder.append("? super ");
			} else {
				bounds = new AnnotatedType[0];
				builder.append("?");
			}

			return builder.append(annotatedBounds(bounds, imports)).toString();
		}

		private String annotatedBounds(AnnotatedType[] bounds, Imports imports) {
			return Arrays.stream(bounds).map(t -> AnnotatedTypes.toString(t, imports))
					.collect(Collectors.joining(" & "));
		}

		@Override
		public int annotationHash() {
			return super.annotationHash() ^ annotationHash(annotatedUpperBounds)
					^ annotationHash(annotatedLowerBounds);
		}
	}

	private AnnotatedWildcardTypes() {}

	/**
	 * Create a new annotated wildcard type over the given wildcard type.
	 * 
	 * @param type
	 *          The wildcard over which to create an annotated wildcard type.
	 * @param annotations
	 *          The annotations to put on the new type.
	 * @return A new AnnotatedWildcardType over the given wildcard type with the
	 *         given annotations.
	 */
	public static AnnotatedWildcardType over(WildcardType type,
			Annotation... annotations) {
		return over(type, Arrays.asList(annotations));
	}

	/**
	 * Create a new annotated wildcard type over the given wildcard type.
	 * 
	 * @param type
	 *          The wildcard over which to create an annotated wildcard type.
	 * @param annotations
	 *          The annotations to put on the new type.
	 * @return A new AnnotatedWildcardType over the given wildcard type with the
	 *         given annotations.
	 */
	public static AnnotatedWildcardType over(WildcardType type,
			Collection<Annotation> annotations) {
		return new AnnotatedWildcardTypeImpl(type, annotations);
	}

	/**
	 * Create an unbounded annotated wildcard type with the given annotations.
	 * 
	 * @param annotations
	 *          The annotations to be contained by the new annotated type.
	 * @return An instance of {@link AnnotatedWildcardType} representing an
	 *         unbounded wildcard.
	 */
	public static AnnotatedWildcardType unbounded(Annotation... annotations) {
		return unbounded(Arrays.asList(annotations));
	}

	/**
	 * Create an unbounded annotated wildcard type with the given annotations.
	 * 
	 * @param annotations
	 *          The annotations to be contained by the new annotated type.
	 * @return An instance of {@link AnnotatedWildcardType} representing an
	 *         unbounded wildcard.
	 */
	public static AnnotatedWildcardType unbounded(
			Collection<Annotation> annotations) {
		return over(WildcardTypes.unbounded(), annotations);
	}

	/**
	 * Create a lower bounded annotated wildcard type with the given annotations.
	 * 
	 * @param annotations
	 *          The annotations to be contained by the new annotated type.
	 * @param bounds
	 *          The types we wish form the lower bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given lower bound.
	 */
	public static AnnotatedWildcardType lowerBounded(
			Collection<Annotation> annotations, AnnotatedType... bounds) {
		return lowerBounded(annotations, Arrays.asList(bounds));
	}

	/**
	 * Create a lower bounded annotated wildcard type with the given annotations.
	 * 
	 * @param annotations
	 *          The annotations to be contained by the new annotated type.
	 * @param bounds
	 *          The types we wish form the lower bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given lower bound.
	 */
	public static AnnotatedWildcardType lowerBounded(
			Collection<Annotation> annotations,
			Collection<? extends AnnotatedType> bounds) {
		return new AnnotatedWildcardTypeImpl(Collections.emptySet(), bounds,
				annotations);
	}

	/**
	 * Create a lower bounded annotated wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the lower bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given lower bound.
	 */
	public static AnnotatedWildcardType lowerBounded(AnnotatedType... bounds) {
		return lowerBounded(Arrays.asList(bounds));
	}

	/**
	 * Create a lower bounded annotated wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the lower bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given lower bound.
	 */
	public static AnnotatedWildcardType lowerBounded(
			Collection<? extends AnnotatedType> bounds) {
		return lowerBounded(Collections.emptySet(), bounds);
	}

	/**
	 * Create an upper bounded annotated wildcard type with the given annotations.
	 * 
	 * @param annotations
	 *          The annotations to be contained by the new annotated type.
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given upper bound.
	 */
	public static AnnotatedWildcardType upperBounded(
			Collection<Annotation> annotations, AnnotatedType... bounds) {
		return upperBounded(annotations, Arrays.asList(bounds));
	}

	/**
	 * Create an upper bounded annotated wildcard type with the given annotations.
	 * 
	 * @param annotations
	 *          The annotations to be contained by the new annotated type.
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given upper bound.
	 */
	public static AnnotatedWildcardType upperBounded(
			Collection<Annotation> annotations,
			Collection<? extends AnnotatedType> bounds) {
		return new AnnotatedWildcardTypeImpl(bounds, Collections.emptySet(),
				annotations);
	}

	/**
	 * Create an upper bounded annotated wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given upper bound.
	 */
	public static AnnotatedWildcardType upperBounded(AnnotatedType... bounds) {
		return upperBounded(Arrays.asList(bounds));
	}

	/**
	 * Create an upper bounded annotated wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given upper bound.
	 */
	public static AnnotatedWildcardType upperBounded(
			Collection<? extends AnnotatedType> bounds) {
		return upperBounded(Collections.emptySet(), bounds);
	}

	protected static AnnotatedWildcardTypeImpl wrapImpl(
			AnnotatedWildcardType type) {
		return wrapImpl(AnnotatedTypes.wrappingVisitedSet(), type);
	}

	protected static AnnotatedWildcardTypeImpl wrapImpl(
			Set<TypeVariable<?>> wrapped, AnnotatedWildcardType type) {
		if (type instanceof AnnotatedWildcardTypeImpl) {
			return (AnnotatedWildcardTypeImpl) type;
		} else
			return new AnnotatedWildcardTypeImpl(wrapped, type);
	}

	/**
	 * Wrap an existing annotated wildcard type.
	 * 
	 * @param type
	 *          The type we wish to wrap.
	 * @return A new instance of {@link AnnotatedWildcardType} which is equal to
	 *         the given type.
	 */
	public static AnnotatedWildcardType wrap(AnnotatedWildcardType type) {
		return wrapImpl(type);
	}
}
