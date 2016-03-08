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

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public abstract class ObservableListDecorator<S extends ObservableListDecorator<S, E>, E>
		implements ListDecorator<E>, ObservableList<S, E> {
	static class ObservableListDecoratorImpl<C extends List<E>, E>
			extends ObservableListDecorator<ObservableListDecoratorImpl<C, E>, E> {
		private Function<? super C, ? extends C> copy;

		ObservableListDecoratorImpl(C list, Function<? super C, ? extends C> copy) {
			super(list);
			this.copy = copy;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ObservableListDecoratorImpl<C, E> copy() {
			return new ObservableListDecoratorImpl<>(copy.apply((C) getComponent()), copy);
		}
	}

	final class ChangeImpl implements Change<E> {
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
			return addedIndices;
		}

		@Override
		public List<E> addedItems() {
			return new AbstractList<E>() {
				@Override
				public E get(int index) {
					return ObservableListDecorator.this.get(addedIndices[index]);
				}

				@Override
				public int size() {
					return ObservableListDecorator.this.size();
				}
			};
		}
	}

	private final List<E> component;

	private final ObservableImpl<Change<E>> changeObservable = new ObservableImpl<>();
	private final ObservableImpl<S> stateObservable = new ObservableImpl<>();

	private ChangeImpl change = new ChangeImpl();
	boolean firing;
	private int[] addedIndices;
	private int[] removedIndices;
	private List<E> removedItems;

	private int changeDepth = 0;

	public ObservableListDecorator(List<E> component) {
		this.component = component;
	}

	@Override
	public List<E> getComponent() {
		return component;
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
				// TODO make change empty
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

			ListDecorator.super.add(e);

			if (change != null) {
				// TODO do change
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

			ListDecorator.super.add(index, element);

			if (change != null) {
				// TODO do change
			}
		} finally {
			endChange();
		}
	}

	@Override
	public boolean remove(Object o) {
		try {
			beginChange();

			// int index = indexOf(o);
			boolean changed = ListDecorator.super.remove(o);

			if (changed && change != null) {
				// TODO do change
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

			E previous = ListDecorator.super.remove(index);

			if (change != null) {
				// TODO do change
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

			E previous = ListDecorator.super.set(index, element);

			if (change != null) {
				// TODO do change
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

			boolean changed = ListDecorator.super.addAll(c);

			if (changed && change != null) {
				// TODO do change
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

			boolean changed = ListDecorator.super.addAll(index, c);

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

			boolean changed = ListDecorator.super.removeAll(c);

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

			boolean changed = ListDecorator.super.retainAll(c);

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

	@Override
	public String toString() {
		return getComponent().toString();
	}

	@Override
	public int hashCode() {
		return getComponent().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return getComponent().equals(obj);
	}
}
