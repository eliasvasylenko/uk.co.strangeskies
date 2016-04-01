/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * An isomorphic mapping from one object graph to another, typically maintained
 * during a set of {@link Copyable#deepCopy(Isomorphism) deep copy} operations.
 * So long as mappings are obtained through the isomorphism they will only be
 * made at most once, and results of mapping will not be re-mapped in chains.
 * <p>
 * An isomorphism can be used across multiple deep copy operations, to build a
 * copy of a "forest" of object graphs.
 * <p>
 * An isomorphism cannot be reused over the same object after that object has
 * been mutated.
 * <p>
 * Please take care! If a different mapping function is used on an object which
 * has already been mapped, heap pollution may occur when the previous mapping
 * result is cast to the new expected result type.
 * 
 * @author Elias N Vasylenko
 */
public class Isomorphism {
	private final Map<?, ?> copiedNodes = new IdentityHashMap<>();

	/**
	 * Make a shallow copy of the given {@link Copyable}, or fetch an existing
	 * mapping if one has been made via this {@link Isomorphism}.
	 * 
	 * @param <S>
	 *          the type of the result
	 * @param node
	 *          the graph node to copy
	 * @return a copy of the given node
	 */
	public <S> S getCopy(Copyable<? extends S> node) {
		return getCopy(node, Copyable::copy);
	}

	/**
	 * Make a deep copy of the given {@link Copyable}, or fetch an existing
	 * mapping if one has been made via this {@link Isomorphism}.
	 * 
	 * @param <S>
	 *          the type of the result
	 * @param node
	 *          the graph node to copy
	 * @return a copy of the given node
	 */
	public <S> S getDeepCopy(Copyable<? extends S> node) {
		return getCopy(node, n -> n.deepCopy(this));
	}

	/**
	 * Make a mapping of the given node, or fetch an existing mapping if one has
	 * been made via this {@link Isomorphism}.
	 * 
	 * @param <S>
	 *          the type of the node
	 * @param <C>
	 *          the type of the result
	 * @param node
	 *          the graph node to map
	 * @param mapping
	 *          the mapping function to apply
	 * @return a mapping of the given node
	 */
	@SuppressWarnings("unchecked")
	public <S, C> S getCopy(C node, Function<C, S> mapping) {
		S copy = ((Map<C, S>) copiedNodes).computeIfAbsent(node, mapping::apply);
		((Map<S, S>) copiedNodes).put(copy, copy);
		return copy;
	}
}
