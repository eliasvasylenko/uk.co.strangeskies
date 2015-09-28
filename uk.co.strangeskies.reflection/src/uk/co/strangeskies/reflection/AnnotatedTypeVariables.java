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
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeImpl;

/**
 * A collection of utility methods relating to annotated type variables.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedTypeVariables {
	private static class AnnotatedTypeVariableImpl extends AnnotatedTypeImpl
			implements AnnotatedTypeVariable {
		private final AnnotatedTypeImpl[] annotatedBounds;

		public AnnotatedTypeVariableImpl(Set<TypeVariable<?>> wrapped,
				AnnotatedTypeVariable annotatedTypeVariable) {
			super(annotatedTypeVariable);

			if (wrapped.contains(getType())) {
				annotatedBounds = AnnotatedTypes.overImpl(getType().getBounds());
			} else {
				wrapped.add(getType());
				annotatedBounds = AnnotatedTypes.wrapImpl(wrapped,
						annotatedTypeVariable.getAnnotatedBounds());
				wrapped.remove(getType());
			}
		}

		public AnnotatedTypeVariableImpl(TypeVariable<?> type,
				Collection<Annotation> annotations) {
			super(type, annotations);

			annotatedBounds = AnnotatedTypes.overImpl(type.getBounds());
		}

		@Override
		public TypeVariable<?> getType() {
			return (TypeVariable<?>) super.getType();
		}

		@Override
		public int annotationHash() {
			return super.annotationHash() ^ annotationHash(annotatedBounds);
		}

		@Override
		public AnnotatedType[] getAnnotatedBounds() {
			return annotatedBounds.clone();
		}
	}

	private AnnotatedTypeVariables() {}

	/**
	 * Create a new annotated representation of a given generic type variable.
	 * 
	 * @param typeVariable
	 *          The type variable to be annotated.
	 * @param annotations
	 *          The annotations for the annotated type variable.
	 * @return A new annotated type variable over the given type variable and with
	 *         the given annotations.
	 */
	public static AnnotatedTypeVariable over(TypeVariable<?> typeVariable,
			Annotation... annotations) {
		return over(typeVariable, Arrays.asList(annotations));
	}

	/**
	 * Create a new annotated representation of a given generic type variable.
	 * 
	 * @param typeVariable
	 *          The type variable to be annotated.
	 * @param annotations
	 *          The annotations for the annotated type variable.
	 * @return A new annotated type variable over the given type variable and with
	 *         the given annotations.
	 */
	public static AnnotatedTypeVariable over(TypeVariable<?> typeVariable,
			Collection<Annotation> annotations) {
		return new AnnotatedTypeVariableImpl(typeVariable, annotations);
	}

	protected static AnnotatedTypeVariableImpl wrapImpl(
			AnnotatedTypeVariable type) {
		return wrapImpl(AnnotatedTypes.wrappingVisitedSet(), type);
	}

	protected static AnnotatedTypeVariableImpl wrapImpl(
			Set<TypeVariable<?>> wrapped, AnnotatedTypeVariable type) {
		if (type instanceof AnnotatedTypeVariableImpl) {
			return (AnnotatedTypeVariableImpl) type;
		} else
			return new AnnotatedTypeVariableImpl(wrapped, type);
	}

	/**
	 * Wrap an existing annotated type variable.
	 * 
	 * @param type
	 *          The type we wish to wrap.
	 * @return A new instance of {@link AnnotatedTypeVariable} which is equal to
	 *         the given type.
	 */
	public static AnnotatedTypeVariable wrap(AnnotatedTypeVariable type) {
		return wrapImpl(type);
	}
}
