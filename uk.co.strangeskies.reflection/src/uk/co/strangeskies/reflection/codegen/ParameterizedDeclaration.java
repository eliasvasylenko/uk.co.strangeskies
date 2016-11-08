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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.reflection.token.TypeToken;

public class ParameterizedDeclaration {
	private final List<DeclaredTypeVariable> typeVariables;
	private final List<Annotation> annotations;

	protected ParameterizedDeclaration(List<String> typeVariableNames, List<Annotation> annotations) {
		this.typeVariables = unmodifiableList(typeVariableNames.stream().map(DeclaredTypeVariable::new).collect(toList()));
		this.annotations = unmodifiableList(annotations);

	}

	public ParameterizedDeclaration withTypeVariables(String... names) {
		return withTypeVariables(asList(names));
	}

	public ParameterizedDeclaration withTypeVariables(List<String> names) {
		return new ParameterizedDeclaration(names, annotations);
	}

	public List<DeclaredTypeVariable> getTypeVariables() {
		return typeVariables;
	}

	public ParameterizedDeclaration withTypeVariableBounds(String typeVariableName, Type... bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public ParameterizedDeclaration withTypeVariable(String typeVariableName, AnnotatedType... bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public ParameterizedDeclaration withTypeVariable(String typeVariableName, TypeToken<?>... bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public ParameterizedDeclaration withTypeVariable(String typeVariableName,
			Collection<? extends AnnotatedType> bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public ParameterizedDeclaration withTypeVariable(String typeVariableName,
			Collection<? extends Annotation> annotations, Collection<? extends AnnotatedType> bounds) {
		addTypeVariable().withAnnotations(annotations);
		addTypeVariable().withUpperBounds(bounds);
		return this;
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
