package uk.co.strangeskies.utilities.collection;

import static java.util.Collections.emptyListIterator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import uk.co.strangeskies.utilities.Scoped;
import uk.co.strangeskies.utilities.Self;
import uk.co.strangeskies.utilities.factory.Factory;

/**
 * TODO
 * 
 * @author Elias N Vasylenko
 * @param <S>
 *          the self-bound, as per {@link Self}
 * @param <E>
 *          the element type, as per {@link Collection}
 */
public abstract class ScopedObservableSet<S extends ObservableSet<S, T>, T> extends ObservableSetDecorator<S, T>
		implements Scoped<S> {
	static class ScopedObservableSetImpl<T> extends ScopedObservableSet<ScopedObservableSetImpl<T>, T> {
		private final Factory<Set<T>> componentFactory;

		ScopedObservableSetImpl(Factory<Set<T>> componentFactory) {
			super(componentFactory.create());
			this.componentFactory = componentFactory;
		}

		private ScopedObservableSetImpl(ScopedObservableSetImpl<T> parent, Factory<Set<T>> componentFactory) {
			super(parent, componentFactory.create());

			this.componentFactory = componentFactory;
		}

		@Override
		public ScopedObservableSetImpl<T> nestChildScope() {
			return new ScopedObservableSetImpl<>(this, componentFactory);
		}

		@Override
		public ScopedObservableSetImpl<T> copy() {
			ScopedObservableSetImpl<T> copy = new ScopedObservableSetImpl<>(componentFactory);
			copy.addAll(this);
			return copy;
		}
	}

	private final S parent;

	public ScopedObservableSet(Set<T> component) {
		this(null, component);
	}

	protected ScopedObservableSet(S parent, Set<T> component) {
		super(component);

		this.parent = parent;

		forwardEvents();
	}

	public static <T> ScopedObservableSet<?, T> over(Factory<Set<T>> componentFactory) {
		return new ScopedObservableSetImpl<>(componentFactory);
	}

	private void forwardEvents() {
		if (getParentScope().isPresent()) {
			S parent = getParentScope().get();
			Set<T> silent = silent();

			parent.changes().addObserver(change -> {
				/*
				 * If we add items to the parent which are currently in the child, we
				 * must silently remove them, and modify the change event so that those
				 * additions are not seen from the child scope when we forward it...
				 */
				Set<T> effectivelyAdded = null;
				for (T item : change.added()) {
					if (silent.remove(item)) {
						if (effectivelyAdded == null) {
							effectivelyAdded = new HashSet<>(change.added());
						}
						effectivelyAdded.remove(item);
					}
				}

				Change<T> effectiveChange;
				if (effectivelyAdded == null) {
					effectiveChange = change;
				} else {
					if (effectivelyAdded.isEmpty() && change.removed().isEmpty()) {
						/*
						 * No items were *effectively* added, and none were removed, so we
						 * can drop the event.
						 */
						return;
					} else {
						effectiveChange = wrapChange(change, effectivelyAdded);
					}
				}

				/*
				 * Forward change events
				 */
				changes().fire(effectiveChange);
				fire(getThis());
			});
		}
	}

	private static <T> Change<T> wrapChange(Change<T> change, Set<T> effectivelyAdded) {
		return new Change<T>() {
			@Override
			public Set<T> added() {
				return effectivelyAdded;
			}

			@Override
			public Set<T> removed() {
				return change.removed();
			}
		};
	}

	@Override
	public boolean add(T e) {
		if (getParentScope().map(p -> p.contains(e)).orElse(false))
			return false;

		return super.add(e);
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
		Iterator<T> iterator = ScopedObservableSet.super.iterator();
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
		return super.isEmpty() && getParentScope().map(Collection::isEmpty).orElse(true);
	}

	@Override
	public Optional<S> getParentScope() {
		return Optional.ofNullable(parent);
	}

	@Override
	public void collapseIntoParentScope() {
		getParentScope().get().silent().addAll(this);
		silent().clear();
	}
}
