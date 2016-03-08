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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public abstract class AbstractObservableTreeSet<S extends AbstractObservableTreeSet<S, E>, E> extends TreeSet<E>
		implements ObservableSet<S, E> {
	class ChangeImpl implements Change<E> {
		Set<E> added = Collections.unmodifiableSet(adding);
		Set<E> removed = Collections.unmodifiableSet(removing);

		@Override
		public Set<E> added() {
			return added;
		}

		@Override
		public Set<E> removed() {
			return removed;
		}
	}

	private static final long serialVersionUID = 1L;

	private final ObservableImpl<Change<E>> changeObservable = new ObservableImpl<>();
	private final ObservableImpl<S> stateObservable = new ObservableImpl<>();

	private final ChangeImpl change = new ChangeImpl();
	private boolean firing;
	private final Set<E> adding = new HashSet<>();
	private final Set<E> removing = new HashSet<>();
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
			firing = changeObservable.getObservers().size() > 0;

			return true;
		} else {
			return false;
		}
	}

	protected boolean endChange() {
		if (--changeDepth == 0) {
			if (firing && fireChange(change)) {
				adding.clear();
				removing.clear();
			}

			return true;
		} else {
			return false;
		}
	}

	protected boolean fireChange(Change<E> change) {
		changeObservable.fire(change);
		fireEvent();

		return true;
	}

	protected void fireEvent() {
		stateObservable.fire(getThis());
	}

	@Override
	public boolean add(E e) {
		try {
			beginChange();

			boolean changed = super.add(e);

			if (changed && !removing.remove(e)) {
				adding.add(e);
			}

			return changed;
		} finally {
			endChange();
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		try {
			beginChange();

			boolean changed = false;

			for (E e : c) {
				changed = add(e) || changed;
			}

			return changed;
		} finally {
			endChange();
		}
	}

	@Override
	public void clear() {
		try {
			beginChange();

			for (E e : this) {
				if (!adding.remove(e)) {
					removing.add(e);
				}
			}

			super.clear();
		} finally {
			endChange();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		try {
			beginChange();

			boolean changed = super.remove(o);

			if (changed && !adding.remove(o)) {
				removing.add((E) o);
			}

			return changed;
		} finally {
			endChange();
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		try {
			beginChange();

			boolean changed = false;

			for (Object o : c) {
				changed = remove(o) || changed;
			}

			return changed;
		} finally {
			endChange();
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		try {
			beginChange();

			boolean changed = false;

			Iterator<E> i = iterator();
			while (i.hasNext()) {
				E e = i.next();

				if (!c.contains(e)) {
					changed = remove(e) || changed;
				}
			}

			return changed;
		} finally {
			endChange();
		}
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
