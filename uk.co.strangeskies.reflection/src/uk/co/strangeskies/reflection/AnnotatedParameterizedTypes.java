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
import java.util.Arrays;
import java.util.Collection;

import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeImpl;

/**
 * A collection of general utility methods relating to annotated types within
 * the Java type system. Utilities related to more specific classes of type may
 * be found in {@link WildcardTypes}, {@link ParameterizedTypes}, and
 * {@link GenericArrayTypes}.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedParameterizedTypes {
	private static class AnnotatedParameterizedTypeImpl extends AnnotatedTypeImpl
			implements AnnotatedParameterizedType {
		private AnnotatedType[] annotatedTypeArguments;

		public AnnotatedParameterizedTypeImpl(ParameterizedType type,
				Collection<? extends Annotation> annotations) {
			super(type, annotations);
		}

		public AnnotatedParameterizedTypeImpl(Class<?> type,
				Collection<? extends Annotation> annotations) {
			super(type, annotations);
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

	public static AnnotatedType over(ParameterizedType type,
			Annotation... annotations) {
		return over(type, Arrays.asList(annotations));
	}

	public static AnnotatedType over(ParameterizedType type,
			Collection<Annotation> annotations) {
		return new AnnotatedParameterizedTypeImpl(type, annotations);
	}
}
