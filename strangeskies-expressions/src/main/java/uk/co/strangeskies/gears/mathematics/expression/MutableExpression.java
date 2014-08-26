package uk.co.strangeskies.gears.mathematics.expression;

import java.util.Set;
import java.util.TreeSet;

import uk.co.strangeskies.gears.utilities.IdentityComparator;
import uk.co.strangeskies.gears.utilities.Observer;

public abstract class MutableExpression<T> implements Expression<T> {
	private final Set<Observer<? super Expression<T>>> observers;

	public MutableExpression() {
		observers = new TreeSet<Observer<? super Expression<T>>>(
				new IdentityComparator<>());
	}

	@Override
	public final boolean addObserver(Observer<? super Expression<T>> observer) {
		return observers.add(observer);
	}

	@Override
	public final boolean removeObserver(Observer<? super Expression<T>> observer) {
		return observers.remove(observer);
	}

	@Override
	public final void clearObservers() {
		observers.clear();
	}

	protected final void postUpdate() {
		for (Observer<? super Expression<T>> observer : observers) {
			observer.notify(null);
		}
	}
}
