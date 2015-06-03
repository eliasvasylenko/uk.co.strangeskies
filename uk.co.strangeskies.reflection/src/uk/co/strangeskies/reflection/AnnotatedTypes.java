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
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
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
	static class AnnotatedTypeImpl implements AnnotatedType {
		private final Type type;
		private final Map<Class<? extends Annotation>, Annotation> annotations;

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

	private AnnotatedTypes() {}

	public static AnnotatedType over(Type type, Annotation... annotations) {
		return over(type, Arrays.asList(annotations));
	}

	public static AnnotatedType over(Type type, Collection<Annotation> annotations) {
		if (type instanceof ParameterizedType) {
			return AnnotatedParameterizedTypes.over((ParameterizedType) type,
					annotations);
		} else if (type instanceof WildcardType) {
			return AnnotatedWildcardTypes.over((WildcardType) type, annotations);
		} else if (type instanceof GenericArrayType) {
			return AnnotatedArrayTypes.over((GenericArrayType) type, annotations);
		} else if (type instanceof Class && ((Class<?>) type).isArray()) {
			return AnnotatedArrayTypes.over((Class<?>) type, annotations);
		} else {
			return new AnnotatedTypeImpl(type, annotations);
		}
	}
}
