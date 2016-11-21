/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.reflection.codegen;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.TypeVariables.typeVariableExtending;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.AnnotatedTypeSubstitution;
import uk.co.strangeskies.reflection.AnnotatedTypeVariables;
import uk.co.strangeskies.utilities.Isomorphism;

public class ParameterizedDeclaration<S extends ParameterizedSignature<?>> extends AnnotatedDeclaration<S>
		implements GenericDeclaration {
	private final List<TypeVariable<? extends ParameterizedDeclaration<S>>> typeVariables;
	private final Map<Class<? extends Annotation>, Annotation> annotations;

	private final Isomorphism isomorphism;
	private final AnnotatedTypeSubstitution boundSubstitution;

	public ParameterizedDeclaration(S signature) {
		super(signature);

		Map<String, TypeVariableSignature> typeVariableSignatures = signature
				.getTypeVariables()
				.collect(toMap(TypeVariableSignature::getName, identity()));

		isomorphism = new Isomorphism();
		List<TypeVariable<? extends ParameterizedDeclaration<S>>> typeVariables = new ArrayList<>(
				typeVariableSignatures.size());

		/*
		 * create type substitutions mapping out TypeVariableSignatures to their
		 * actual TypeVariables.
		 */
		boundSubstitution = new AnnotatedTypeSubstitution().where(
				t -> t.getType() instanceof TypeVariableSignature.Reference || t.getType() instanceof TypeVariable<?>,

				t -> {
					TypeVariable<?> typeVariable = substituteTypeVariableSignature(
							typeVariableSignatures.get(t.getType().getTypeName()));

					return AnnotatedTypeVariables.over(typeVariable, t.getAnnotations());
				});

		/*
		 * Perform the substitution for each signature
		 */
		for (TypeVariableSignature typeVariableSignature : typeVariableSignatures.values()) {
			typeVariables.add(substituteTypeVariableSignature(typeVariableSignature));
		}
		this.typeVariables = Collections.unmodifiableList(typeVariables);

		/*
		 * Check consistency of type bounds
		 */
		for (TypeVariable<?> typeVariable : typeVariables) {
			intersectionOf(typeVariable.getBounds());
		}

		this.annotations = unmodifiableMap(
				signature.getAnnotations().collect(toMap(Annotation::annotationType, identity())));
	}

	protected AnnotatedType substituteTypeVariableSignatures(AnnotatedType annotatedType) {
		return boundSubstitution.resolve(annotatedType, isomorphism);
	}

	protected TypeVariable<? extends ParameterizedDeclaration<S>> substituteTypeVariableSignature(
			TypeVariableSignature typeVariable) {
		List<AnnotatedType> bounds = typeVariable
				.getBounds()
				.map(b -> boundSubstitution.resolve(b, isomorphism))
				.collect(toList());

		return typeVariableExtending(this, typeVariable.getName(), bounds);
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

	public Stream<? extends TypeVariable<? extends ParameterizedDeclaration<S>>> getTypeVariables() {
		return typeVariables.stream();
	}

	/**
	 * @return true if the declaration has type parameters, false otherwise
	 */
	public boolean isParameterized() {
		return !typeVariables.isEmpty();
	}
}
