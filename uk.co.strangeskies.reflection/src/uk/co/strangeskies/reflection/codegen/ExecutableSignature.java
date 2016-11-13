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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public abstract class ExecutableSignature<S extends ExecutableSignature<S>> extends ParameterizedSignature<S> {
	protected final Collection<? extends VariableSignature<?>> parameters;

	public ExecutableSignature() {
		this.parameters = Collections.emptySet();
	}

	protected ExecutableSignature(
			Collection<? extends VariableSignature<?>> parameters,
			Collection<? extends TypeVariableSignature> typeVariables,
			Collection<? extends Annotation> annotations) {
		super(typeVariables, annotations);

		this.parameters = parameters;
	}

	@Override
	protected S withParameterizedSignatureData(
			Collection<? extends TypeVariableSignature> typeVariables,
			Collection<? extends Annotation> annotations) {
		return withExecutableSignatureData(parameters, typeVariables, annotations);
	}

	protected abstract S withExecutableSignatureData(
			Collection<? extends VariableSignature<?>> parameters2,
			Collection<? extends TypeVariableSignature> typeVariables,
			Collection<? extends Annotation> annotations);

	public Stream<? extends VariableSignature<?>> getParameters() {
		return parameters.stream();
	}

	public S withParameters(VariableSignature<?>... parameters) {
		return withParameters(asList(parameters));
	}

	public S withParameters(Collection<? extends VariableSignature<?>> parameters) {
		return withExecutableSignatureData(new ArrayList<>(parameters), typeVariables, annotations);
	}
}
