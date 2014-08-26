package uk.co.strangeskies.gears.mathematics;

import java.util.Comparator;

import uk.co.strangeskies.gears.utilities.Copyable;
import uk.co.strangeskies.gears.utilities.NaturalComparator;
import uk.co.strangeskies.gears.utilities.Property;
import uk.co.strangeskies.gears.utilities.Self;

public class Range<T> implements Self<Range<T>>, Copyable<Range<T>> {
	private T from;
	private T to;

	private boolean fromInclusive;
	private boolean toInclusive;

	private final Comparator<? super T> comparator;

	protected Range(T from, T to, Comparator<? super T> comparator) {
		this.from = from;
		this.to = to;

		this.fromInclusive = true;
		this.toInclusive = true;

		this.comparator = comparator;
	}

	public static <T> Range<T> create(T from, T to,
			Comparator<? super T> comparator) {
		return new Range<T>(from, to, comparator);
	}

	public static <T extends Comparable<? super T>> Range<T> create(T from, T to) {
		return new Range<T>(from, to, new NaturalComparator<T>());
	}

	public static Range<Integer> parse(String range) {
		String[] splitRange = range.split("..");
		if (splitRange.length != 2)
			throw new IllegalArgumentException();
		try {
			Integer from = Integer.parseInt(splitRange[0]);
			Integer to = Integer.parseInt(splitRange[1]);
			return create(from, to);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String compose(Range<Integer> range) {
		String from = range.getFrom() == null ? ""
				: (range.isFromInclusive() ? range.getFrom().toString() : Integer
						.toString(range.getFrom() + 1));

		String to = range.getTo() == null ? "" : (range.isToInclusive() ? range
				.getTo().toString() : Integer.toString(range.getTo() - 1));

		return from + ".." + to;
	}

	public T getFrom() {
		return from;
	}

	public T getTo() {
		return to;
	}

	public Range<T> setFrom(T from) {
		this.from = from;

		return this;
	}

	public Range<T> setTo(T to) {
		this.to = to;

		return this;
	}

	public boolean isFromInclusive() {
		return fromInclusive;
	}

	public boolean isToInclusive() {
		return toInclusive;
	}

	public Range<T> setFromInclusive(boolean fromInclusive) {
		this.fromInclusive = fromInclusive;

		return this;
	}

	public Range<T> setToInclusive(boolean toInclusive) {
		this.toInclusive = toInclusive;

		return this;
	}

	public Range<T> setInclusive(boolean fromInclusive, boolean toInclusive) {
		return setFromInclusive(fromInclusive).setToInclusive(toInclusive);
	}

	public Comparator<? super T> getComparator() {
		return comparator;
	}

	public boolean isEmpty() {
		return !fromInclusive && !toInclusive && from.equals(to);
	}

	public boolean contains(final T value) {
		return contains(new Comparable<T>() {
			@Override
			public int compareTo(T other) {
				return comparator.compare(value, other);
			}
		});
	}

	public boolean touches(final T value) {
		return touches(new Comparable<T>() {
			@Override
			public int compareTo(T other) {
				return comparator.compare(value, other);
			}
		});
	}

	public boolean contains(Comparable<? super T> value) {
		int fromCompare = value.compareTo(from);
		int toCompare = value.compareTo(to);

		if (fromInclusive) {
			if (toInclusive) {
				return fromCompare >= 0 && toCompare <= 0;
			} else {
				return fromCompare >= 0 && toCompare < 0;
			}
		} else {
			if (toInclusive) {
				return fromCompare > 0 && toCompare <= 0;
			} else {
				return fromCompare > 0 && toCompare < 0;
			}
		}
	}

	public boolean touches(Comparable<? super T> value) {
		return value.compareTo(from) >= 0 && value.compareTo(to) <= 0;
	}

	public boolean contains(Range<T> range) {
		if (range.isFromInclusive()) {
			if (!contains(range.getFrom()))
				return false;
		} else {
			if (!touches(range.getFrom()))
				return false;
		}
		if (range.isToInclusive()) {
			if (!contains(range.getTo()))
				return false;
		} else {
			if (!touches(range.getTo()))
				return false;
		}
		return true;
	}

	public boolean isValueBelow(T value) {
		if (fromInclusive) {
			return comparator.compare(value, from) < 0;
		} else {
			return comparator.compare(value, from) <= 0;
		}
	}

	public boolean isValueAbove(T value) {
		if (toInclusive) {
			return comparator.compare(value, to) > 0;
		} else {
			return comparator.compare(value, to) >= 0;
		}
	}

	public boolean isValueBelow(Comparable<? super T> value) {
		if (fromInclusive) {
			return value.compareTo(from) < 0;
		} else {
			return value.compareTo(from) <= 0;
		}
	}

	public boolean isValueAbove(Comparable<? super T> value) {
		if (toInclusive) {
			return value.compareTo(to) > 0;
		} else {
			return value.compareTo(to) >= 0;
		}
	}

	public T getConfined(T value) {
		if (isEmpty()) {
			return null;
		}
		if (contains(value)) {
			return value;
		}
		if (comparator.compare(from, value) > 0) {
			return from;
		}
		return to;
	}

	public <M extends Property<? extends T, ? super T>> M confine(M value) {
		if (isEmpty()) {
			return null;
		}
		if (isValueAbove(value.get())) {
			value.set(to);
		} else if (isValueBelow(value.get())) {
			value.set(from);
		}
		return value;
	}

	public <M extends Property<? extends Comparable<? super T>, ? super T>> M confineComparable(
			M value) {
		if (isEmpty()) {
			return null;
		}
		if (isValueAbove(value.get())) {
			value.set(to);
		} else if (isValueBelow(value.get())) {
			value.set(from);
		}
		return value;
	}

	public Range<T> getExtendedThrough(Range<? extends T> other) {
		return copy().extendThrough(other);
	}

	public Range<T> extendThrough(Range<? extends T> other) {
		int compareFrom = comparator.compare(other.getFrom(), getFrom());
		if (compareFrom == 0) {
			setFromInclusive(isFromInclusive() || other.isFromInclusive());
		} else if (compareFrom < 0) {
			setFrom(other.getFrom());
			setFromInclusive(other.isFromInclusive());
		}

		int compareTo = comparator.compare(other.getTo(), getTo());
		if (compareTo == 0) {
			setToInclusive(isToInclusive() || other.isToInclusive());
		} else if (compareTo > 0) {
			setTo(other.getTo());
			setToInclusive(other.isToInclusive());
		}

		return getThis();
	}

	public Range<T> getExtendedThrough(T other, boolean inclusive) {
		return copy().extendThrough(other, inclusive);
	}

	public Range<T> extendThrough(T other, boolean inclusive) {
		int compareFrom = comparator.compare(other, getFrom());
		int compareTo = comparator.compare(other, getTo());

		if (inclusive) {
			if (compareFrom == 0) {
				setFromInclusive(true);
			}
			if (compareTo == 0) {
				setToInclusive(true);
			}
		}

		if (compareFrom < 0) {
			setFrom(other);
			setFromInclusive(inclusive);
		} else if (compareTo > 0) {
			setTo(other);
			setToInclusive(inclusive);
		}

		return getThis();
	}

	@Override
	public Range<T> copy() {
		return new Range<>(from, to, comparator).setInclusive(isFromInclusive(),
				isToInclusive());
	}

	public Range<T> reversed() {
		return new Range<T>(getTo(), getFrom(), getComparator().reversed());
	}
}
