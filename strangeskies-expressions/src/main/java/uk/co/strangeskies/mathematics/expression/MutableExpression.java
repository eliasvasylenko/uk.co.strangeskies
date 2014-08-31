package uk.co.strangeskies.mathematics.expression;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import uk.co.strangeskies.utilities.IdentityComparator;
import uk.co.strangeskies.utilities.Observer;

public abstract class MutableExpression<T> implements Expression<T> {
	private final Set<Observer<? super Expression<T>>> observers;
	private final ReadWriteLock lock;

	public MutableExpression() {
		observers = new TreeSet<Observer<? super Expression<T>>>(
				new IdentityComparator<>());
		lock = new ReentrantReadWriteLock();
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
		getLock().writeLock().lock();
		for (Observer<? super Expression<T>> observer : observers)
			observer.notify(null);
		getLock().writeLock().unlock();
	}

	@Override
	public ReadWriteLock getLock() {
		return lock;
	}
}
