package uk.co.strangeskies.gears.utilities;

import java.util.Comparator;

public class ReverseComparator<T> implements Comparator<T> {
	private final Comparator<T> comparator;

	public ReverseComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	@Override
	public int compare(T arg0, T arg1) {
		return comparator.compare(arg1, arg0);
	}
}
