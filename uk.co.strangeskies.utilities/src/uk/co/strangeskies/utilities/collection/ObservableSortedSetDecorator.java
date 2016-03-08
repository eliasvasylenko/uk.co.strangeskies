package uk.co.strangeskies.utilities.collection;

import java.util.SortedSet;
import java.util.function.Function;

public abstract class ObservableSortedSetDecorator<S extends ObservableSortedSetDecorator<S, E>, E>
		extends ObservableSetDecorator<S, E> implements SortedSetDecorator<E>, ObservableSortedSet<S, E> {
	static class ObservableSortedSetDecoratorImpl<C extends SortedSet<E>, E>
			extends ObservableSortedSetDecorator<ObservableSortedSetDecoratorImpl<C, E>, E> {
		private Function<? super C, ? extends C> copy;

		ObservableSortedSetDecoratorImpl(C set, Function<? super C, ? extends C> copy) {
			super(set);
			this.copy = copy;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ObservableSortedSetDecoratorImpl<C, E> copy() {
			return new ObservableSortedSetDecoratorImpl<>(copy.apply((C) getComponent()), copy);
		}
	}

	protected ObservableSortedSetDecorator(SortedSet<E> component) {
		super(component);
	}

	@Override
	public SortedSet<E> getComponent() {
		return (SortedSet<E>) super.getComponent();
	}
}
