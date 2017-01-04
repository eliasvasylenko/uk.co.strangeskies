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

import static java.util.Collections.emptySet;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.Visibility;

/**
 * This type is a placeholder for a {@link TypeVariable} on a
 * {@link GenericDeclaration} produced from a {@link ParameterizedSignature}.
 * 
 * @author Elias N Vasylenko
 */
public abstract class MemberSignature<S extends MemberSignature<S>> implements AnnotatedSignature<S> {
	final String name;
	final Set<Annotation> annotations;
	final Visibility visibility;

	/**
	 * @param name
	 *          the name of the declared type parameter
	 */
	protected MemberSignature(String name) {
		this(name, emptySet(), Visibility.PACKAGE_PRIVATE);
	}

	protected MemberSignature(String name, Set<Annotation> annotations, Visibility visibility) {
		this.name = name;
		this.annotations = annotations;
		this.visibility = visibility;
	}

	protected abstract S withMemberSignatureData(String name, Set<Annotation> annotations, Visibility visibility);

	@Override
	public Stream<? extends Annotation> getAnnotations() {
		return annotations.stream();
	}

	@Override
	public S withAnnotations(Collection<? extends Annotation> annotations) {
		return withMemberSignatureData(name, new HashSet<>(annotations), visibility);
	}

	public String getName() {
		return name;
	}

	public S withVisibility(Visibility visibility) {
		return withMemberSignatureData(name, annotations, visibility);
	}

	public Visibility getVisibility() {
		return visibility;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof MemberSignature<?>))
			return false;

		MemberSignature<?> that = (MemberSignature<?>) obj;

		return AnnotatedSignature.equals(this, that) && Objects.equals(this.getName(), that.getName())
				&& Objects.equals(this.getVisibility(), that.getVisibility());
	}

	@Override
	public int hashCode() {
		return AnnotatedSignature.hashCode(this) ^ getName().hashCode() ^ getVisibility().hashCode();
	}
}
