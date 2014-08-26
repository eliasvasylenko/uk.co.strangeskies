package uk.co.strangeskies.gears.utilities;

import java.util.Comparator;

public class NaturalComparator<T extends Comparable<? super T>> implements
		Comparator<T> {
	@Override
	public final int compare(T o1, T o2) {
		return o1.compareTo(o2);
	}
}
