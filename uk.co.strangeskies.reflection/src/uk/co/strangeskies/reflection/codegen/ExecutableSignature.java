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
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static uk.co.strangeskies.reflection.Types.getRawType;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public abstract class ExecutableSignature<S extends ExecutableSignature<S>> extends ParameterizedSignature<S> {
	protected final List<VariableSignature<?>> parameters;
	protected ErasedMethodSignature erasedSignature;

	public ExecutableSignature() {
		this.parameters = emptyList();
	}

	protected ExecutableSignature(
			List<VariableSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables,
			Set<Annotation> annotations) {
		super(typeVariables, annotations);

		this.parameters = parameters;
	}

	public abstract String getName();

	public ErasedMethodSignature erased() {
		if (erasedSignature == null) {
			this.erasedSignature = new ErasedMethodSignature(
					getName(),
					parameters.stream().map(v -> getRawType(v.getType().getType())).toArray(Class<?>[]::new));
		}
		return erasedSignature;
	}

	@Override
	protected S withParameterizedSignatureData(List<TypeVariableSignature> typeVariables, Set<Annotation> annotations) {
		return withExecutableSignatureData(parameters, typeVariables, annotations);
	}

	protected abstract S withExecutableSignatureData(
			List<VariableSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables,
			Set<Annotation> annotations);

	public Stream<? extends VariableSignature<?>> getParameters() {
		return parameters.stream();
	}

	public S withParameters(VariableSignature<?>... parameters) {
		return withParameters(asList(parameters));
	}

	public S withParameters(Collection<? extends VariableSignature<?>> parameters) {
		return withExecutableSignatureData(new ArrayList<>(parameters), typeVariables, annotations);
	}

	protected void appendParameters(StringBuilder builder) {
		builder.append("(").append(getParameters().map(Objects::toString).collect(joining(", "))).append(")");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ExecutableSignature<?>))
			return false;

		ExecutableSignature<?> that = (ExecutableSignature<?>) obj;

		return super.equals(that) && Objects.equals(this.getName(), that.getName())
				&& Objects.equals(this.getParameters().collect(toSet()), that.getParameters().collect(toSet()));
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ this.getName().hashCode() ^ parameters.hashCode();
	}
}
