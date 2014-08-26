package uk.co.strangeskies.gears.mathematics.expression.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import uk.co.strangeskies.gears.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.utilities.IdentityComparator;
import uk.co.strangeskies.gears.utilities.Observer;

public class ExpressionTreeSet<E extends Expression<?>> extends TreeSet<E>
		implements SortedExpressionSet<ExpressionTreeSet<E>, E>,
		CopyDecouplingExpression<ExpressionTreeSet<E>> {
	private static final long serialVersionUID = 1L;

	private boolean evaluated = true;

	private Observer<Expression<?>> dependencyObserver;

	private Set<Observer<? super Expression<ExpressionTreeSet<E>>>> observers;

	public ExpressionTreeSet(Comparator<? super E> comparator) {
		super(comparator);

		dependencyObserver = new Observer<Expression<?>>() {
			@Override
			public void notify(Expression<?> message) {
				update();
			}
		};

		observers = new TreeSet<>(new IdentityComparator<>());
	}

	public ExpressionTreeSet() {
		this((Comparator<E>) null);
	}

	@SafeVarargs
	public ExpressionTreeSet(E... expressions) {
		this();

		addAll(Arrays.asList(expressions));
	}

	public ExpressionTreeSet(Collection<E> expressions) {
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
		for (Observer<? super Expression<ExpressionTreeSet<E>>> observer : observers) {
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
		retainAll(expressions);
		addAll(expressions);
	}

	@Override
	public final boolean retainAll(Collection<?> expressions) {
		TreeSet<E> toRemove = new TreeSet<>();

		for (E expression : this) {
			if (!expressions.contains(expression)) {
				toRemove.add(expression);
			}
		}

		return removeAll(toRemove);
	}

	@Override
	public final Collection<E> getUnmodifiableView() {
		return Collections.unmodifiableSet(this);
	}

	@Override
	public final ExpressionTreeSet<E> getValue() {
		evaluated = true;
		return this;
	}

	@Override
	public final void clearObservers() {
		observers.clear();
	}

	@Override
	public final ExpressionTreeSet<E> copy() {
		return new ExpressionTreeSet<>(this);
	}

	@Override
	public boolean addObserver(
			Observer<? super Expression<ExpressionTreeSet<E>>> observer) {
		return observers.add(observer);
	}

	@Override
	public boolean removeObserver(
			Observer<? super Expression<ExpressionTreeSet<E>>> observer) {
		return observers.remove(observer);
	}
}
