/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.co.strangeskies.utilities.Isomorphism;

public class GenericDefinition<S extends GenericDefinition<S>> implements GenericDeclaration {
	private final List<TypeVariable<S>> typeVariables;
	private final Map<Class<? extends Annotation>, Annotation> annotations;

	private final Isomorphism isomorphism;
	private final AnnotatedTypeSubstitution boundSubstitution;

	public GenericDefinition(GenericSignature signature) {
		List<TypeVariableSignature> typeVariableSignatures = signature.getTypeVariableSignatures();

		isomorphism = new Isomorphism();
		List<TypeVariable<S>> typeVariables = new ArrayList<>(typeVariableSignatures.size());

		/*
		 * create type substitutions mapping out TypeVariableSignatures to their
		 * actual TypeVariables.
		 */
		boundSubstitution = new AnnotatedTypeSubstitution().where(

				t -> t.getType() instanceof TypeVariableSignature,

				t -> {
					TypeVariable<?> typeVariable = substituteTypeVariableSignature((TypeVariableSignature) t.getType());

					return AnnotatedTypeVariables.over(typeVariable, t.getAnnotations());
				});

		/*
		 * Perform the substitution for each signature
		 */
		for (TypeVariableSignature typeVariableSignature : typeVariableSignatures) {
			typeVariables.add(substituteTypeVariableSignature(typeVariableSignature));
		}
		this.typeVariables = Collections.unmodifiableList(typeVariables);

		this.annotations = unmodifiableMap(
				signature.getAnnotations().stream().collect(toMap(Annotation::annotationType, identity())));
	}

	protected AnnotatedType substituteTypeVariableSignatures(AnnotatedType annotatedType) {
		return boundSubstitution.resolve(annotatedType, isomorphism);
	}

	protected TypeVariable<S> substituteTypeVariableSignature(TypeVariableSignature signature) {
		List<AnnotatedType> bounds = signature.getBounds().stream().map(b -> boundSubstitution.resolve(b, isomorphism))
				.collect(Collectors.toList());

		return TypeVariables.upperBounded(getThis(), signature.getTypeName(), bounds);
	}

	@SuppressWarnings("unchecked")
	protected S getThis() {
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends Annotation> U getAnnotation(Class<U> annotationClass) {
		return (U) annotations.get(annotationClass);
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
	public TypeVariable<?>[] getTypeParameters() {
		return typeVariables.toArray(new TypeVariable<?>[typeVariables.size()]);
	}

	public List<TypeVariable<S>> getTypeVariables() {
		return typeVariables;
	}
}
