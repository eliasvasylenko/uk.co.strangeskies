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
package uk.co.strangeskies.utilities.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;
import uk.co.strangeskies.utilities.collection.AbstractObservableArrayList.ChangeImpl;

public abstract class AbstractObservableTreeSet<S extends AbstractObservableTreeSet<S, E>, E> extends TreeSet<E>
		implements ObservableSet<S, E> {
	private static final long serialVersionUID = 1L;

	private final ObservableImpl<Change<E>> changeObservable = new ObservableImpl<>();
	private final ObservableImpl<S> stateObservable = new ObservableImpl<>();

	private E adding;
	private Set<E> addingAll = new HashSet<>();
	private E removing;
	private Set<E> removingAll = new HashSet<>();
	private int changeDepth = 0;

	public AbstractObservableTreeSet() {}

	public AbstractObservableTreeSet(Comparator<? super E> comparator) {
		super(comparator);
	}

	public AbstractObservableTreeSet(Collection<? extends E> c) {
		super(c);
	}

	@SafeVarargs
	public AbstractObservableTreeSet(E... c) {
		super(Arrays.asList(c));
	}

	public AbstractObservableTreeSet(SortedSet<E> s) {
		super(s);
	}

	protected boolean beginChange() {
		if (changeDepth++ == 0) {
			if (changeObservable.getObserverCount() > 0) {
				if (change == null) {
					change = new ChangeImpl();
				}
			} else {
				change = null;
			}

			return true;
		} else {
			return false;
		}
	}

	protected boolean endChange() {
		if (--changeDepth == 0 && change.size > 0) {
			if (fireChange(change)) {
				change = null;
			}

			return true;
		} else {
			return false;
		}
	}
	@Override
	public boolean add(E e) {
		if (super.add(e)) {
			stateObservable.fire(getThis());
			changeObservable.fire(new Change<E>() {
				@Override
				public Type type() {
					return Type.ADDED;
				}

				@Override
				public E element() {
					return e;
				}
			});
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		Iterator<? extends E> i = c.iterator();
		boolean changed = i.hasNext() && addRecursion(i);

		if (changed) {
			stateObservable.fire(getThis());
		}

		return changed;
	}

	/*
	 * Ensure observers are notified only after all items are added, without
	 * having to assign heap memory to determine which adds were successful
	 */
	private boolean addRecursion(Iterator<? extends E> i) {
		E e = i.next();

		boolean changed = super.add(e);

		if (i.hasNext()) {
			addRecursion(i);
		}

		if (changed) {
			changeObservable.fire(new Change<E>() {
				@Override
				public Type type() {
					return Type.ADDED;
				}

				@Override
				public E element() {
					return e;
				}
			});
		}

		return changed;
	}

	@Override
	public boolean remove(Object o) {
		if (super.remove(o)) {
			stateObservable.fire(getThis());
			changeObservable.fire(new Change<E>() {
				@Override
				public Type type() {
					return Type.REMOVED;
				}

				@SuppressWarnings("unchecked")
				@Override
				public E element() {
					return (E) o;
				}
			});
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		Iterator<? extends Object> i = c.iterator();
		boolean changed = i.hasNext() && removeRecursion(i);

		if (changed) {
			stateObservable.fire(getThis());
		}

		return changed;
	}

	/*
	 * Ensure observers are notified only after all items are removed, without
	 * having to assign heap memory to determine which removes were successful
	 */
	private boolean removeRecursion(Iterator<? extends Object> i) {
		Object e = i.next();

		boolean changed = super.remove(e);

		if (i.hasNext()) {
			removeRecursion(i);
		}

		if (changed) {
			changeObservable.fire(new Change<E>() {
				@Override
				public Type type() {
					return Type.REMOVED;
				}

				@SuppressWarnings("unchecked")
				@Override
				public E element() {
					return (E) e;
				}
			});
		}

		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Iterator<? extends Object> i = c.iterator();
		boolean changed = i.hasNext() && retainRecursion(i, c);

		if (changed) {
			stateObservable.fire(getThis());
		}

		return changed;
	}

	/*
	 * Ensure observers are notified only after all items are removed, without
	 * having to assign heap memory to determine which removes were successful
	 */
	private boolean retainRecursion(Iterator<? extends Object> i, Collection<?> c) {
		Object e = i.next();

		boolean changed = !c.contains(e);

		if (i.hasNext()) {
			retainRecursion(i, c);
		}

		if (changed) {
			super.remove(e);
			changeObservable.fire(new Change<E>() {
				@Override
				public Type type() {
					return Type.REMOVED;
				}

				@SuppressWarnings("unchecked")
				@Override
				public E element() {
					return (E) e;
				}
			});
		}

		return changed;
	}

	@Override
	public Observable<Change<E>> changes() {
		return changeObservable;
	}

	@Override
	public boolean addObserver(Consumer<? super S> observer) {
		return stateObservable.addObserver(observer);
	}

	@Override
	public boolean removeObserver(Consumer<? super S> observer) {
		return stateObservable.removeObserver(observer);
	}
}
