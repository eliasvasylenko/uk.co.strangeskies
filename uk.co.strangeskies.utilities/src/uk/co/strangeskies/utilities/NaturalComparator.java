package uk.co.strangeskies.utilities;

import java.util.Comparator;

import org.checkerframework.checker.igj.qual.I;

@I
public class NaturalComparator<T extends Comparable<? super T>> implements
		Comparator<T> {
	@Override
	public final int compare(T o1, T o2) {
		return o1.compareTo(o2);
	}
}
