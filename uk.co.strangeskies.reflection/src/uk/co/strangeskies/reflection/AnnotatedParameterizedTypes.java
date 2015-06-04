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
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeImpl;

/**
 * A collection of utility methods relating to annotated parameterised types.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedParameterizedTypes {
	private static class AnnotatedParameterizedTypeImpl extends AnnotatedTypeImpl
			implements AnnotatedParameterizedType {
		private final AnnotatedType[] annotatedTypeArguments;

		public AnnotatedParameterizedTypeImpl(ParameterizedType type,
				Collection<? extends Annotation> annotations) {
			super(type, annotations);

			annotatedTypeArguments = AnnotatedTypes.over(type
					.getActualTypeArguments());
		}

		public AnnotatedParameterizedTypeImpl(
				Class<?> rawType,
				Function<? super TypeVariable<?>, ? extends AnnotatedType> annotatedTypes,
				Collection<? extends Annotation> annotations) {
			super(ParameterizedTypes.from(rawType,
					annotatedTypes.andThen(AnnotatedType::getType)).getType(),
					annotations);

			annotatedTypeArguments = (AnnotatedType[]) Arrays
					.stream(rawType.getTypeParameters()).map(p -> {
						AnnotatedType type = annotatedTypes.apply(p);
						if (type == null)
							type = AnnotatedTypes.over(p);
						return type;
					}).toArray();
		}

		@Override
		public AnnotatedType[] getAnnotatedActualTypeArguments() {
			return annotatedTypeArguments;
		}

		@Override
		public ParameterizedType getType() {
			return (ParameterizedType) super.getType();
		}
	}

	private AnnotatedParameterizedTypes() {}

	/**
	 * Annotate an existing {@link ParameterizedType} with the given annotations.
	 * 
	 * @param type
	 *          The parameterized type we wish to annotate.
	 * @param annotations
	 *          Annotations to put on the resulting
	 *          {@link AnnotatedParameterizedType}.
	 * @return A new {@link AnnotatedParameterizedType} instance over the given
	 *         parameterized type, with the given annotations.
	 */
	public static AnnotatedParameterizedType over(ParameterizedType type,
			Annotation... annotations) {
		return over(type, Arrays.asList(annotations));
	}

	/**
	 * Annotate an existing {@link ParameterizedType} with the given annotations.
	 * 
	 * @param type
	 *          The parameterized type we wish to annotate.
	 * @param annotations
	 *          Annotations to put on the resulting
	 *          {@link AnnotatedParameterizedType}.
	 * @return A new {@link AnnotatedParameterizedType} instance over the given
	 *         parameterized type, with the given annotations.
	 */
	public static AnnotatedParameterizedType over(ParameterizedType type,
			Collection<Annotation> annotations) {
		return new AnnotatedParameterizedTypeImpl(type, annotations);
	}

	/**
	 * Parameterize a generic class with the given annotated type arguments.
	 * 
	 * @param rawType
	 *          The generic class we wish to parameterize.
	 * @param arguments
	 *          A mapping from the type variables on the generic class to their
	 *          annotated arguments.
	 * @param annotations
	 *          Annotations to put on the resulting
	 *          {@link AnnotatedParameterizedType}.
	 * @return A new {@link AnnotatedParameterizedType} instance with the given
	 *         type arguments, and the given annotations.
	 */
	public static AnnotatedParameterizedType from(Class<?> rawType,
			Function<? super TypeVariable<?>, ? extends AnnotatedType> arguments,
			Annotation... annotations) {
		return new AnnotatedParameterizedTypeImpl(rawType, arguments,
				Arrays.asList(annotations));
	}
}
