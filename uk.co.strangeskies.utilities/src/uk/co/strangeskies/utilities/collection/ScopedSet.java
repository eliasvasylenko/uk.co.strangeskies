package uk.co.strangeskies.utilities.collection;

import static java.util.Collections.emptyListIterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import uk.co.strangeskies.utilities.Scoped;
import uk.co.strangeskies.utilities.factory.Factory;

public abstract class ScopedSet<S extends ScopedSet<S, T>, T> implements SetDecorator<T>, Scoped<S> {
	static class ScopedSetImpl<T> extends ScopedSet<ScopedSetImpl<T>, T> {
		private final Factory<Set<T>> componentFactory;

		ScopedSetImpl(Factory<Set<T>> componentFactory) {
			super(componentFactory.create());
			this.componentFactory = componentFactory;
		}

		private ScopedSetImpl(ScopedSetImpl<T> parent, Factory<Set<T>> componentFactory) {
			super(parent, componentFactory.create());
			this.componentFactory = componentFactory;
		}

		@Override
		public ScopedSetImpl<T> nestChildScope() {
			return new ScopedSetImpl<>(this, componentFactory);
		}

		@Override
		public ScopedSetImpl<T> copy() {
			ScopedSetImpl<T> copy = new ScopedSetImpl<>(componentFactory);
			copy.addAll(this);
			return copy;
		}
	}

	private final S parent;
	private final Set<T> component;

	public ScopedSet(Set<T> component) {
		this(null, component);
	}

	protected ScopedSet(S parent, Set<T> component) {
		this.parent = parent;
		this.component = component;
	}

	@Override
	public Set<T> getComponent() {
		return component;
	}

	public static <T> ScopedSet<?, T> over(Factory<Set<T>> componentFactory) {
		return new ScopedSetImpl<>(componentFactory);
	}

	@Override
	public boolean add(T e) {
		if (getParentScope().map(p -> p.contains(e)).orElse(false))
			return false;

		return SetDecorator.super.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean changed = false;

		for (T e : c) {
			changed = add(e) || changed;
		}

		return changed;
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> iterator = SetDecorator.super.iterator();
		Iterator<T> parentIterator = getParentScope().map(Collection::iterator).orElse(emptyListIterator());

		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext() || parentIterator.hasNext();
			}

			@Override
			public T next() {
				return iterator.hasNext() ? iterator.next() : parentIterator.next();
			}
		};
	}

	@Override
	public boolean isEmpty() {
		return SetDecorator.super.isEmpty() && getParentScope().map(Collection::isEmpty).orElse(true);
	}

	@Override
	public Optional<S> getParentScope() {
		return Optional.ofNullable(parent);
	}

	@Override
	public void collapseIntoParentScope() {
		getParentScope().get().addAll(this);
		clear();
	}
}
