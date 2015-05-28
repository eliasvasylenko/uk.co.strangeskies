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
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A collection of general utility methods relating to annotated types within
 * the Java type system. Utilities related to more specific classes of type may
 * be found in {@link WildcardTypes}, {@link ParameterizedTypes}, and
 * {@link GenericArrayTypes}.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedTypes {
	private static class AnnotatedTypeImpl implements AnnotatedType {
		private final Type type;
		private final Map<Class<? extends Annotation>, Annotation> annotations;

		public AnnotatedTypeImpl(Type type) {
			this.type = type;
			annotations = Collections.emptyMap();
		}

		public AnnotatedTypeImpl(Type type,
				Collection<? extends Annotation> annotations) {
			this.type = type;
			this.annotations = new LinkedHashMap<>();
			for (Annotation annotation : annotations)
				this.annotations.put(annotation.getClass(), annotation);
		}

		@SuppressWarnings("unchecked")
		@Override
		public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return (T) annotations.get(annotationClass);
		}

		@Override
		public final Annotation[] getAnnotations() {
			return annotations.values().toArray(new Annotation[annotations.size()]);
		}

		@Override
		public final Annotation[] getDeclaredAnnotations() {
			return getAnnotations();
		}

		@Override
		public Type getType() {
			return type;
		}
	}

	private static class AnnotatedParameterizedTypeImpl extends AnnotatedTypeImpl
			implements AnnotatedParameterizedType {
		private AnnotatedType[] annotatedTypeArguments;

		public AnnotatedParameterizedTypeImpl(ParameterizedType type) {
			super(type);
		}

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

	private AnnotatedTypes() {}

	public static AnnotatedType over(Type type) {
		if (type instanceof Class) {
			return new AnnotatedTypeImpl(type);
		} else if (type instanceof ParameterizedType) {
			return new AnnotatedParameterizedTypeImpl((ParameterizedType) type);
		}

		throw new IllegalArgumentException("Unexpected class '" + type.getClass()
				+ "' of type '" + type + "'.");
	}
}
