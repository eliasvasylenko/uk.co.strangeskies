package uk.co.strangeskies.gears.mathematics.expression.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import uk.co.strangeskies.gears.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.utilities.Observer;

public class ExpressionArrayList<E extends Expression<?>> extends ArrayList<E>
		implements ExpressionList<ExpressionArrayList<E>, E>,
		CopyDecouplingExpression<ExpressionArrayList<E>> {
	private static final long serialVersionUID = 1L;

	private boolean evaluated = true;

	private final Observer<Expression<?>> dependencyObserver;

	private final Set<Observer<? super Expression<ExpressionArrayList<E>>>> observers;

	public ExpressionArrayList() {
		dependencyObserver = new Observer<Expression<?>>() {
			@Override
			public void notify(Expression<?> message) {
				update();
			}
		};

		observers = new TreeSet<>();
	}

	public ExpressionArrayList(Collection<E> expressions) {
		this();

		addAll(expressions);
	}

	protected final void update() {
		if (evaluated) {
			evaluated = false;
			postUpdate();
		}
	}

	protected final void postUpdate() {
		for (Observer<? super Expression<ExpressionArrayList<E>>> observer : observers) {
			observer.notify(null);
		}
	}

	@Override
	public final boolean add(E expression) {
		boolean added = super.add(expression);

		if (added) {
			expression.addObserver(dependencyObserver);

			update();
		}

		return added;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean remove(Object expression) {
		boolean removed = super.remove(expression);

		if (removed) {
			((E) expression).removeObserver(dependencyObserver);

			update();
		}

		return removed;
	}

	@Override
	public final boolean addAll(Collection<? extends E> expressions) {
		boolean changed = false;

		for (E expression : expressions) {
			if (super.add(expression)) {
				expression.addObserver(dependencyObserver);
				changed = true;
			}
		}

		if (changed) {
			update();
		}

		return changed;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean removeAll(Collection<?> expressions) {
		boolean changed = false;

		for (Object expression : expressions) {
			if (super.remove(expression)) {
				((E) expression).removeObserver(dependencyObserver);
				changed = true;
			}
		}

		if (changed) {
			update();
		}

		return changed;
	}

	@Override
	public final void clear() {
		clear(true);
	}

	protected final void clear(boolean update) {
		if (!isEmpty()) {
			for (E expression : this) {
				expression.removeObserver(dependencyObserver);
			}

			super.clear();

			if (update) {
				update();
			}
		}
	}

	@Override
	public final void set(Collection<? extends E> expressions) {
		clear(false);

		for (E expression : expressions) {
			if (super.add(expression)) {
				expression.addObserver(dependencyObserver);
			}
		}

		update();
	}

	@Override
	public final boolean retainAll(Collection<?> c) {
		TreeSet<E> toRemove = new TreeSet<>();

		toRemove.addAll(this);
		toRemove.removeAll(c);

		return removeAll(toRemove);
	}

	@Override
	public final Collection<E> getUnmodifiableView() {
		return Collections.unmodifiableList(this);
	}

	@Override
	public final ExpressionArrayList<E> getValue() {
		evaluated = true;
		return this;
	}

	@Override
	public final boolean addObserver(
			Observer<? super Expression<ExpressionArrayList<E>>> observer) {
		return observers.add(observer);
	}

	@Override
	public final boolean removeObserver(
			Observer<? super Expression<ExpressionArrayList<E>>> observer) {
		return observers.remove(observer);
	}

	@Override
	public final void clearObservers() {
		observers.clear();
	}

	@Override
	public final ExpressionArrayList<E> copy() {
		return new ExpressionArrayList<>(this);
	}

	@Override
	public final void add(int index, E expression) {
		super.add(index, expression);

		expression.addObserver(dependencyObserver);

		update();
	}

	@Override
	public final boolean addAll(int index, Collection<? extends E> expressions) {
		for (E expression : expressions) {
			add(index++, expression);
			expression.addObserver(dependencyObserver);
		}

		update();

		return !expressions.isEmpty();
	}

	@Override
	public final E remove(int index) {
		E removed = super.remove(index);

		removed.removeObserver(dependencyObserver);

		update();

		return removed;
	}

	@Override
	public final E set(int index, E expression) {
		E removed = super.remove(index);
		super.add(index, expression);

		removed.removeObserver(dependencyObserver);
		expression.addObserver(dependencyObserver);

		update();

		return removed;
	}
}
