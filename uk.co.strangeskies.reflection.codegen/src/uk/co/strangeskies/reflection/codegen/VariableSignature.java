/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen;

import static java.lang.System.identityHashCode;
import static java.util.Collections.emptySet;
import static uk.co.strangeskies.reflection.AnnotatedTypes.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.TypeToken;

public class VariableSignature<T> implements AnnotatedSignature<VariableSignature<T>> {
	private final String variableName;
	private final AnnotatedType type;
	private final Set<Annotation> annotations;

	protected VariableSignature(String variableName, AnnotatedType type) {
		this.variableName = variableName;
		this.type = type;
		this.annotations = emptySet();
	}

	protected VariableSignature(String variableName, AnnotatedType type, Set<Annotation> annotations) {
		this.variableName = variableName;
		this.type = type;
		this.annotations = annotations;
	}

	public static VariableSignature<?> variableSignature(String variableName, AnnotatedType type) {
		return new VariableSignature<>(variableName, type);
	}

	public static VariableSignature<?> variableSignature(String variableName, Type type) {
		return new VariableSignature<>(variableName, annotated(type));
	}

	public static <U> VariableSignature<U> variableSignature(String variableName, Class<U> type) {
		return new VariableSignature<>(variableName, annotated(type));
	}

	public static <U> VariableSignature<U> variableSignature(String variableName, TypeToken<U> type) {
		return new VariableSignature<>(variableName, type.getAnnotatedDeclaration());
	}

	public static <U> VariableSignature<U> variableSignature(Parameter parameter) {
		return new VariableSignature<U>(parameter.getName(), annotated(parameter.getType()))
				.withAnnotations(parameter.getAnnotations());
	}

	@Override
	public Stream<? extends Annotation> getAnnotations() {
		return annotations.stream();
	}

	@Override
	public VariableSignature<T> withAnnotations(Collection<? extends Annotation> annotations) {
		return new VariableSignature<>(variableName, type, new HashSet<>(annotations));
	}

	public String getVariableName() {
		return variableName;
	}

	public AnnotatedType getType() {
		return type;
	}

	@Override
	public String toString() {
		return getType() + " " + getVariableName();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	@Override
	public int hashCode() {
		return identityHashCode(this);
	}
}
