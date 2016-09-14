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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GenericSignature {
	private final List<TypeVariableSignature> typeVariableSignatures;
	private final List<Annotation> annotations;

	public GenericSignature() {
		typeVariableSignatures = new ArrayList<>();
		annotations = new ArrayList<>();

	}

	public TypeVariableSignature addTypeVariable() {
		TypeVariableSignature typeVariable = new TypeVariableSignature(typeVariableSignatures.size());
		typeVariableSignatures.add(typeVariable);
		return typeVariable;
	}

	public GenericSignature withTypeVariable() {
		addTypeVariable().withUpperBounds(new Type[] {});
		return this;
	}

	public GenericSignature withTypeVariable(Type... bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public GenericSignature withTypeVariable(AnnotatedType... bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public GenericSignature withTypeVariable(TypeToken<?>... bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public GenericSignature withTypeVariable(Collection<? extends AnnotatedType> bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public GenericSignature withTypeVariable(Collection<? extends Annotation> annotations,
			Collection<? extends AnnotatedType> bounds) {
		addTypeVariable().withAnnotations(annotations);
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public List<TypeVariableSignature> getTypeVariableSignatures() {
		return Collections.unmodifiableList(typeVariableSignatures);
	}

	public GenericSignature withAnnotations(Annotation... annotations) {
		return withAnnotations(Arrays.asList(annotations));
	}

	public GenericSignature withAnnotations(Collection<? extends Annotation> annotations) {
		this.annotations.clear();
		this.annotations.addAll(annotations);

		return this;
	}

	public List<Annotation> getAnnotations() {
		return Collections.unmodifiableList(annotations);
	}
}
