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

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.TypeVariable;

public class AnnotatedTypeToken<T> extends TypeToken<T> {
	private final AnnotatedType annotatedType;

	protected AnnotatedTypeToken() {
		Class<?> subclass;
		Class<?> superclass = getClass();
		AnnotatedType annotatedType;

		do {
			subclass = superclass;
			superclass = subclass.getSuperclass();
			annotatedType = subclass.getAnnotatedSuperclass();
		} while (!Types.getRawType(annotatedType.getType()).equals(
				AnnotatedTypeToken.class));

		/*
		 * I'm pretty sure this shouldn't be needed, Eclipse... Is subclass really
		 * not already effectively final?
		 */
		Class<?> finalSubclass = subclass;

		if (!Types.getAllMentionedBy(
				getType(),
				t -> t instanceof TypeVariable<?>
						&& ((TypeVariable<?>) t).getGenericDeclaration().equals(
								finalSubclass)).isEmpty())
			throw new TypeException(
					"Cannot create "
							+ AnnotatedTypeToken.class.getSimpleName()
							+ " literal instance containing type variables belonging to a subtype.");

		this.annotatedType = ((AnnotatedParameterizedType) annotatedType)
				.getAnnotatedActualTypeArguments()[0];
	}

	public AnnotatedTypeToken(AnnotatedType type) {
		super(type);

		if (!isProper())
			throw new TypeException("Cannot create "
					+ AnnotatedTypeToken.class.getSimpleName()
					+ " instance containing inference variables.");

		annotatedType = type;
	}

	public static AnnotatedTypeToken<?> over(AnnotatedType type) {
		return new AnnotatedTypeToken<>(type);
	}

	public AnnotatedType getAnnotatedType() {
		return annotatedType;
	}
}
