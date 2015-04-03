/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines an arbitrary total ordering over references' identities. Guaranteed
 * to be a consistent ordering for a particular IdentityComparator, but not
 * necessarily between different instances of IdentityComparator.
 *
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of object to compare.
 */
public class IdentityComparator<T> implements Comparator<T> {
	private final Map<Integer, List<IDReference<T>>> collisionMap;

	private final ReferenceQueue<Object> referenceQueue;

	/**
	 * Create a fresh identity comparator.
	 */
	public IdentityComparator() {
		collisionMap = new HashMap<Integer, List<IDReference<T>>>();

		referenceQueue = new ReferenceQueue<>();
	}

	@Override
	public int compare(IdentityComparator<T> this, T first, T second) {
		clean();

		if (first == second) {
			return 0;
		}

		int firstHash = System.identityHashCode(first);
		int secondHash = System.identityHashCode(second);

		if (firstHash != secondHash) {
			return secondHash - firstHash;
		}

		IDReference<T> firstReference = new IDReference<T>(first, firstHash,
				referenceQueue);
		IDReference<T> secondReference = new IDReference<T>(second, secondHash,
				referenceQueue);

		List<IDReference<T>> collisions = collisionMap.get(firstHash);
		if (collisions == null) {
			collisions = new ArrayList<IDReference<T>>();

			collisions.add(firstReference);
			collisions.add(secondReference);

			collisionMap.put(firstHash, collisions);

			return 1;
		} else {
			int firstIndex = collisions.indexOf(firstReference);
			if (firstIndex < 0) {
				collisions.add(0, firstReference);
				firstIndex = 0;
			}

			int secondIndex = collisions.indexOf(secondReference);
			if (secondIndex < 0) {
				collisions.add(0, secondReference);
				secondIndex = 0;
				firstIndex++;
			}

			return secondIndex - firstIndex;
		}
	}

	/**
	 * This method can be called to prune stale references from the hash-collision
	 * map. It is also called automatically
	 */
	public void clean(IdentityComparator<T> this) {
		IDReference<?> oldReference;
		while ((oldReference = (IDReference<?>) referenceQueue.poll()) != null) {
			List<IDReference<T>> collisions = collisionMap.get(oldReference.getId());

			if (collisions.size() > 1) {
				collisions.remove(oldReference);
			} else {
				collisionMap.remove(collisions);
			}
		}
	}

	protected class IDReference<R> extends WeakReference<R> {
		private final int id;

		public IDReference(R referent, int id, ReferenceQueue<? super R> q) {
			super(referent, q);

			this.id = id;
		}

		public int getId() {
			return id;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Reference<?>)) {
				return false;
			}

			return ((Reference<?>) obj).get() == get();
		}

		@Override
		public int hashCode() {
			return get().hashCode();
		}
	}
}
