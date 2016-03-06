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
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public abstract class AbstractObservableArrayList<S extends AbstractObservableArrayList<S, E>, E> extends ArrayList<E>
		implements ObservableList<S, E> {
	class ChangeImpl implements Change<E> {
		int[] changeIndex;
		List<E>[] changeData;
		boolean[] isAdded;
		int index = -1;
		int size;

		@Override
		public boolean next() {
			return index++ < size;
		}

		@Override
		public void reset() {
			index = -1;
		}

		@Override
		public int index() {
			return index;
		}

		@Override
		public List<E> getRemoved() {
			return isAdded[index] ? Collections.emptyList() : changeData[index];
		}

		@Override
		public List<E> getAdded() {
			return isAdded[index] ? changeData[index] : Collections.emptyList();
		}
	}

	private static final long serialVersionUID = 1L;

	private final ObservableImpl<Change<E>> changeObservable = new ObservableImpl<>();
	private final ObservableImpl<S> stateObservable = new ObservableImpl<>();

	private ChangeImpl change;
	private int changeDepth = 0;

	public AbstractObservableArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	public AbstractObservableArrayList() {}

	public AbstractObservableArrayList(Collection<? extends E> c) {
		super(c);
	}

	protected boolean beginChange() {
		synchronized (changeObservable) {
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
	}

	protected boolean endChange() {
		synchronized (changeObservable) {
			if (--changeDepth == 0 && change.size > 0) {
				if (fireChange(change)) {
					change = null;
				}

				return true;
			} else {
				return false;
			}
		}
	}

	protected boolean fireChange(Change<E> change) {
		fireEvent();
		changeObservable.fire(change);

		return true;
	}

	protected void fireEvent() {
		stateObservable.fire(getThis());
	}

	@Override
	public boolean add(E e) {
		synchronized (changeObservable) {
			try {
				beginChange();

				super.add(e);

				if (change != null) {
					// TODO do change
				}

				return true;
			} finally {
				endChange();
			}
		}
	}

	@Override
	public void add(int index, E element) {
		synchronized (changeObservable) {
			try {
				beginChange();

				super.add(index, element);

				if (change != null) {
					// TODO do change
				}
			} finally {
				endChange();
			}
		}
	}

	@Override
	public boolean remove(Object o) {
		synchronized (changeObservable) {
			try {
				beginChange();

				boolean changed = super.remove(o);

				if (changed && change != null) {
					// TODO do change
				}

				return changed;
			} finally {
				endChange();
			}
		}
	}

	@Override
	public E remove(int index) {
		synchronized (changeObservable) {
			try {
				beginChange();

				E previous = super.remove(index);

				if (change != null) {
					// TODO do change
				}

				return previous;
			} finally {
				endChange();
			}
		}
	}

	@Override
	public E set(int index, E element) {
		synchronized (changeObservable) {
			try {
				beginChange();

				E previous = super.set(index, element);

				if (change != null) {
					// TODO do change
				}

				return previous;
			} finally {
				endChange();
			}
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		synchronized (changeObservable) {
			try {
				beginChange();

				boolean changed = super.addAll(c);

				if (changed && change != null) {
					// TODO do change
				}

				return changed;
			} finally {
				endChange();
			}
		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		synchronized (changeObservable) {
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
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		synchronized (changeObservable) {
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
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		synchronized (changeObservable) {
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
	}

	@Override
	public Observable<Change<E>> changes() {
		return changeObservable;
	}

	@Override
	public boolean addObserver(Consumer<? super S> observer) {
		synchronized (changeObservable) {
			// TODO not catch partial changes
			return stateObservable.addObserver(observer);
		}
	}

	@Override
	public boolean removeObserver(Consumer<? super S> observer) {
		synchronized (changeObservable) {
			return stateObservable.removeObserver(observer);
		}
	}
}
