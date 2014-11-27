package uk.co.strangeskies.utilities;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.igj.qual.Mutable;
import org.checkerframework.checker.igj.qual.ReadOnly;

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
public class IdentityComparator<T> implements @ReadOnly Comparator<@Mutable T> {
	private final Map<Integer, List<IDReference<@ReadOnly T>>> collisionMap;

	private final ReferenceQueue<Object> referenceQueue;

	public IdentityComparator() {
		collisionMap = new HashMap<Integer, List<IDReference<@ReadOnly T>>>();

		referenceQueue = new ReferenceQueue<>();
	}

	@Override
	public int compare(@ReadOnly IdentityComparator<T> this, @ReadOnly T first,
			@ReadOnly T second) {
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

		@Mutable
		List<IDReference<@ReadOnly T>> collisions = collisionMap.get(firstHash);
		if (collisions == null) {
			collisions = new ArrayList<IDReference<@ReadOnly T>>();

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
	public void clean(@ReadOnly IdentityComparator<T> this) {
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

	@ReadOnly
	protected class IDReference<R> extends @ReadOnly WeakReference<@Mutable R> {
		private final int id;

		public IDReference(@ReadOnly R referent, int id,
				@ReadOnly ReferenceQueue<? super @ReadOnly R> q) {
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