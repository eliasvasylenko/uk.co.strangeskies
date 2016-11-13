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
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public abstract class ParameterizedSignature<S extends ParameterizedSignature<S>> extends AnnotatedSignature<S> {
	protected final Collection<? extends TypeVariableSignature> typeVariables;

	public ParameterizedSignature() {
		typeVariables = emptySet();
	}

	protected ParameterizedSignature(
			Collection<? extends TypeVariableSignature> typeVariables,
			Collection<? extends Annotation> annotations) {
		super(annotations);
		this.typeVariables = typeVariables;
	}

	@Override
	protected S withAnnotatedDeclarationData(Collection<? extends Annotation> annotations) {
		return withParameterizedSignatureData(typeVariables, annotations);
	}

	protected abstract S withParameterizedSignatureData(
			Collection<? extends TypeVariableSignature> typeVariables,
			Collection<? extends Annotation> annotations);

	public S withTypeVariables(String... names) {
		return withTypeVariables(stream(names).map(TypeVariableSignature::typeVariableSignature).collect(toList()));
	}

	public S withTypeVariables(TypeVariableSignature... typeVariables) {
		return withTypeVariables(asList(typeVariables));
	}

	public S withTypeVariables(List<TypeVariableSignature> typeVariables) {
		return withParameterizedSignatureData(typeVariables, annotations);
	}

	public Stream<? extends TypeVariableSignature> getTypeVariables() {
		return typeVariables.stream();
	}
}
