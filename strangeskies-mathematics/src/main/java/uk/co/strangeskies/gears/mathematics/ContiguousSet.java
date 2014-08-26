package uk.co.strangeskies.gears.mathematics;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;

public class ContiguousSet<T> implements Iterable<T>, Collection<T>,
		NavigableSet<T>, SortedSet<T> {
	private final Range<T> range;
	private final Incrementor<T> incrementor;

	public ContiguousSet(Range<T> range, Incrementor<T> incrementor) {
		this.range = range;
		this.incrementor = incrementor;
	}

	public static <T extends Incrementable<? extends T>> ContiguousSet<T> create(
			Range<T> range) {
		return new ContiguousSet<>(range, new NaturalIncrementor<T>());
	}

	public final Range<T> getRange() {
		return range;
	}

	public final Incrementor<T> getIncrementor() {
		return incrementor;
	}

	public final Comparator<? super T> getComparator() {
		return range.getComparator();
	}

	@Override
	public final Comparator<? super T> comparator() {
		return getComparator();
	}

	@Override
	public T first() {
		T first = range.getFrom();
		if (!range.isFromInclusive()) {
			first = incrementor.getIncremented(first);
		}
		return first;
	}

	@Override
	public T last() {
		T last = range.getTo();
		if (!range.isToInclusive()) {
			last = incrementor.getDecremented(last);
		}
		return last;
	}

	@Override
	public T lower(T e) {
		return null;
	}

	@Override
	public T floor(T e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T ceiling(T e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T higher(T e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T pollFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T pollLast() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			T on = getRange().isFromInclusive() ? getRange().getFrom() : incrementor
					.increment(getRange().getFrom());

			@Override
			public boolean hasNext() {
				return on != null;
			}

			@Override
			public T next() {
				T was = on;
				on = incrementor.increment(on);
				int compare = comparator().compare(on, getRange().getTo());
				if (getRange().isToInclusive() ? compare < 0 : compare <= 0)
					on = null;
				return was;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public NavigableSet<T> descendingSet() {
		return new ContiguousSet<>(getRange().reversed(), getIncrementor()
				.reversed());
	}

	@Override
	public Iterator<T> descendingIterator() {
		return descendingSet().iterator();
	}

	@Override
	public ContiguousSet<T> subSet(T from, boolean fromInclusive, T to,
			boolean toInclusive) {
		T fromRounded = ceiling(from);
		T toRounded = floor(to);
		return new ContiguousSet<>(new Range<T>(fromRounded, toRounded,
				getComparator()).setInclusive(fromInclusive, toInclusive),
				getIncrementor());
	}

	@Override
	public NavigableSet<T> headSet(T toElement, boolean inclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<T> subSet(T fromElement, T toElement) {
		return subSet(fromElement, true, toElement, false);
	}

	@Override
	public SortedSet<T> headSet(T toElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<T> tailSet(T fromElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> A[] toArray(A[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(T e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
}
