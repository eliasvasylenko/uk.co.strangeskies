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
package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ParameterizedDeclaration {
	private final List<TypeVariableDeclaration> typeVariableSignatures;
	private final List<Annotation> annotations;

	public ParameterizedDeclaration() {
		typeVariableSignatures = new ArrayList<>();
		annotations = new ArrayList<>();

	}

	public TypeVariableDeclaration addTypeVariable() {
		TypeVariableDeclaration typeVariable = new TypeVariableDeclaration(typeVariableSignatures.size());
		typeVariableSignatures.add(typeVariable);
		return typeVariable;
	}

	public ParameterizedDeclaration withTypeVariable() {
		addTypeVariable().withUpperBounds(new Type[] {});
		return this;
	}

	public ParameterizedDeclaration withTypeVariable(Type... bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public ParameterizedDeclaration withTypeVariable(AnnotatedType... bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public ParameterizedDeclaration withTypeVariable(TypeToken<?>... bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public ParameterizedDeclaration withTypeVariable(Collection<? extends AnnotatedType> bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public ParameterizedDeclaration withTypeVariable(Collection<? extends Annotation> annotations,
			Collection<? extends AnnotatedType> bounds) {
		addTypeVariable().withAnnotations(annotations);
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public List<TypeVariableDeclaration> getTypeVariableSignatures() {
		return Collections.unmodifiableList(typeVariableSignatures);
	}

	public ParameterizedDeclaration withAnnotations(Annotation... annotations) {
		return withAnnotations(Arrays.asList(annotations));
	}

	public ParameterizedDeclaration withAnnotations(Collection<? extends Annotation> annotations) {
		this.annotations.clear();
		this.annotations.addAll(annotations);

		return this;
	}

	public List<Annotation> getAnnotations() {
		return Collections.unmodifiableList(annotations);
	}
}
