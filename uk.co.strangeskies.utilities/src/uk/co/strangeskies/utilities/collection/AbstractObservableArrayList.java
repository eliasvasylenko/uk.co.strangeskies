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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public abstract class AbstractObservableArrayList<S extends AbstractObservableArrayList<S, E>, E> extends ArrayList<E>
		implements ObservableList<S, E> {
	class ChangeImpl implements Change<E> {
		private int[] removedIndices;
		private List<E> removedItems;

		@Override
		public int[] removedIndices() {
			return removedIndices;
		}

		@Override
		public List<E> removedItems() {
			return removedItems;
		}

		@Override
		public int[] addedIndices() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<E> addedItems() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int[] permutedIndices() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<E> permutedItems() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int[] modifiedIndices() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<E> modifiedFromItems() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<E> modifiedToItems() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private static final long serialVersionUID = 1L;

	private final ObservableImpl<Change<E>> changeObservable = new ObservableImpl<>();
	private final ObservableImpl<S> stateObservable = new ObservableImpl<>();

	private ChangeImpl change = new ChangeImpl();
	private int changeDepth = 0;

	public AbstractObservableArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	public AbstractObservableArrayList() {}

	public AbstractObservableArrayList(Collection<? extends E> c) {
		super(c);
	}

	protected boolean beginChange() {
		if (changeDepth++ == 0) {
			if (changeObservable.getObserverCount() > 0) {
				if (change == null) {
					change.removedIndices = null;
					change.removedItems = null;
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
		if (--changeDepth == 0 && change != null && !change.isEmpty()) {
			if (fireChange()) {
				change = null;
			}

			return true;
		} else {
			return false;
		}
	}

	protected boolean fireChange() {
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

			super.add(e);

			if (change != null) {
				change.add(new SingleChangeItemImpl(Type.ADDED, e, true, size()));
			}

			return true;
		} finally {
			endChange();
		}
	}

	@Override
	public void add(int index, E element) {
		try {
			beginChange();

			super.add(index, element);

			if (change != null) {
				change.add(new SingleChangeItemImpl(Type.ADDED, element, true, index));
			}
		} finally {
			endChange();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		try {
			beginChange();

			int index = indexOf(o);
			boolean changed = super.remove(o);

			if (changed && change != null) {
				change.add(new SingleChangeItemImpl(Type.REMOVED, (E) o, true, index));
			}

			return changed;
		} finally {
			endChange();
		}
	}

	@Override
	public E remove(int index) {
		try {
			beginChange();

			E previous = super.remove(index);

			if (change != null) {
				change.add(new SingleChangeItemImpl(Type.REMOVED, previous, false, index));
			}

			return previous;
		} finally {
			endChange();
		}
	}

	@Override
	public E set(int index, E element) {
		try {
			beginChange();

			E previous = super.set(index, element);

			if (change != null) {
				change.add(new SingleChangeItemImpl(Type.CHANGED, element, false, index));
			}

			return previous;
		} finally {
			endChange();
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		try {
			beginChange();

			boolean changed = super.addAll(c);

			if (changed && change != null) {
				change.add(new SingleChangeItemImpl(Type.ADDED, previous, false, index));
			}

			return changed;
		} finally {
			endChange();
		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		try {
			beginChange();

			boolean changed = super.addAll(index, c);

			if (changed && change != null) {
				// TODO do change
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

			boolean changed = super.removeAll(c);

			if (changed && change != null) {
				// TODO do change
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

			boolean changed = super.retainAll(c);

			if (changed && change != null) {
				// TODO do change
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
