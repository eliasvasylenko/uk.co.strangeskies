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

	protected void beginChange() {
		synchronized (changeObservable) {
			if (changeDepth++ == 0) {
				if (changeObservable.getObserverCount() > 0) {
					change = new ChangeImpl();
				} else {
					change = null;
				}
			}
		}
	}

	protected void endChange() {
		synchronized (changeObservable) {
			if (--changeDepth == 0) {
				changeObservable.fire(change);
			}
		}
	}

	protected void endAllChanges() {
		synchronized (changeObservable) {
			changeDepth = 0;
			changeObservable.fire(change);
		}
	}

	@Override
	public boolean add(E e) {
		synchronized (changeObservable) {
			super.add(e);

			if (change != null) {
				// TODO do change
			}
			return true;
		}
	}

	@Override
	public void add(int index, E element) {
		synchronized (changeObservable) {
			super.add(index, element);

			if (change != null) {
				// TODO do change
			}
		}
	}

	@Override
	public boolean remove(Object o) {
		synchronized (changeObservable) {
			if (super.remove(o) && change != null) {
				// TODO do change
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public E remove(int index) {
		synchronized (changeObservable) {
			E previous = super.remove(index);

			if (change != null) {
				// TODO do change
			}

			return previous;
		}
	}

	@Override
	public E set(int index, E element) {
		synchronized (changeObservable) {
			E previous = super.set(index, element);

			if (change != null) {
				// TODO do change
			}

			return previous;
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		synchronized (changeObservable) {
			if (super.addAll(c) && change != null) {
				// TODO do change
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		synchronized (changeObservable) {
			if (super.addAll(index, c) && change != null) {
				// TODO do change
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		synchronized (changeObservable) {
			if (super.removeAll(c) && change != null) {
				// TODO do change
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		synchronized (changeObservable) {
			if (super.retainAll(c) && change != null) {
				// TODO do change
				return true;
			} else {
				return false;
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
