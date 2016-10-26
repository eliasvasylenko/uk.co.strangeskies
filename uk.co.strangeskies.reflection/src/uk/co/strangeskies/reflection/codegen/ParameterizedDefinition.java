/*
 * Copyright (C) 2016 ${copyright.holder.name} <eliasvasylenko@strangeskies.co.uk>
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
import static java.util.stream.Collectors.toMap;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.AnnotatedTypeSubstitution;
import uk.co.strangeskies.reflection.AnnotatedTypeVariables;
import uk.co.strangeskies.reflection.TypeVariables;
import uk.co.strangeskies.utilities.Isomorphism;

public class ParameterizedDefinition<S extends ParameterizedDefinition<S>> implements GenericDeclaration {
	private final List<TypeVariable<S>> typeVariables;
	private final Map<Class<? extends Annotation>, Annotation> annotations;

	private final Isomorphism isomorphism;
	private final AnnotatedTypeSubstitution boundSubstitution;

	public ParameterizedDefinition(ParameterizedDeclaration signature) {
		List<TypeVariableDeclaration> typeVariableSignatures = signature.getTypeVariableSignatures();

		isomorphism = new Isomorphism();
		List<TypeVariable<S>> typeVariables = new ArrayList<>(typeVariableSignatures.size());

		/*
		 * create type substitutions mapping out TypeVariableSignatures to their
		 * actual TypeVariables.
		 */
		boundSubstitution = new AnnotatedTypeSubstitution().where(

				t -> t.getType() instanceof TypeVariableDeclaration,

				t -> {
					TypeVariable<?> typeVariable = substituteTypeVariableSignature((TypeVariableDeclaration) t.getType());

					return AnnotatedTypeVariables.over(typeVariable, t.getAnnotations());
				});

		/*
		 * Perform the substitution for each signature
		 */
		for (TypeVariableDeclaration typeVariableSignature : typeVariableSignatures) {
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
				signature.getAnnotations().stream().collect(toMap(Annotation::annotationType, identity())));
	}

	protected AnnotatedType substituteTypeVariableSignatures(AnnotatedType annotatedType) {
		return boundSubstitution.resolve(annotatedType, isomorphism);
	}

	protected TypeVariable<S> substituteTypeVariableSignature(TypeVariableDeclaration signature) {
		List<AnnotatedType> bounds = signature.getBounds().stream().map(b -> boundSubstitution.resolve(b, isomorphism))
				.collect(Collectors.toList());

		return TypeVariables.upperBoundedTypeVariable(getThis(), signature.getTypeName(), bounds);
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
