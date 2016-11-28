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
package uk.co.strangeskies.reflection.token;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeResolver;
import uk.co.strangeskies.reflection.Types;

/**
 * A partial implementation of {@link MemberToken} with some common
 * implementation and functionality.
 * 
 * @author Elias N Vasylenko
 *
 * @param <O>
 *          the owner type of the member
 */
public abstract class AbstractMemberToken<O, M extends Member> implements MemberToken<O> {
	private final M member;
	private final TypeToken<?> ownerType;
	private final Map<TypeVariable<?>, Type> containerTypeArguments;

	public AbstractMemberToken(M member, TypeResolver resolver, TypeToken<?> ownerType) {
		this.member = member;
		this.ownerType = ownerType;

		containerTypeArguments = determineContainerTypeArguments(resolver);
	}

	protected AbstractMemberToken(AbstractMemberToken<O, M> from) {
		this.member = from.member;
		this.ownerType = from.ownerType;
		this.containerTypeArguments = from.containerTypeArguments;
	}

	@Override
	public TypeToken<?> getOwnerType() {
		return ownerType;
	}

	@Override
	public M getMember() {
		return member;
	}

	protected Map<TypeVariable<?>, Type> determineContainerTypeArguments(TypeResolver resolver) {
		Map<TypeVariable<?>, Type> containerArguments;

		if (ownerType.getType() == void.class || !Types.isGeneric(getMember().getDeclaringClass())) {
			containerArguments = new LinkedHashMap<>();
		} else {
			List<TypeToken<?>> containerSupertypeList = ownerType
					.resolveSupertypeHierarchy(getMember().getDeclaringClass())
					.collect(toList());

			Type containerSupertype = containerSupertypeList.get(containerSupertypeList.size() - 1).getType();

			if (containerSupertype instanceof Class<?>) {
				containerArguments = null;

			} else {
				boolean containerSupertypeIsExact = !containerSupertypeList
						.stream()
						.anyMatch(t -> t.getType() instanceof InferenceVariable);

				if (containerSupertypeIsExact && stream(((ParameterizedType) containerSupertype).getActualTypeArguments())
						.anyMatch(WildcardType.class::isInstance)) {
					throw new ReflectionException(
							p -> p.cannotResolveInvocationOnTypeWithWildcardParameters(ownerType.getType()));
				}

				containerArguments = ParameterizedTypes.getAllTypeArguments((ParameterizedType) containerSupertype).collect(
						toMap(Entry::getKey, e -> resolver.resolveType(e.getValue()), (a, b) -> a, LinkedHashMap::new));
			}
		}

		return containerArguments == null ? null : Collections.unmodifiableMap(containerArguments);
	}

	protected Map<TypeVariable<?>, Type> getContainerTypeArguments() {
		return containerTypeArguments;
	}

	@Override
	public boolean isRaw() {
		return containerTypeArguments == null;
	}
}
