/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.Self;

public class Range<T> implements Self<Range<T>> {
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

	public static <T> Range<T> between(T from, T to,
			Comparator<? super T> comparator) {
		return new Range<T>(from, to, comparator);
	}

	public static <T extends Comparable<? super T>> Range<T> between(T from,
			T to) {
		return between(from, to, Comparable::compareTo);
	}

	public static <T> Range<T> over(Collection<? extends T> over,
			Comparator<? super T> comparator) {
		if (over.isEmpty())
			throw new IllegalArgumentException("No elements given to range over");

		return new Range<T>(over.stream().min(comparator).get(),
				over.stream().max(comparator).get(), comparator);
	}

	public static <T extends Comparable<? super T>> Range<T> over(
			Collection<? extends T> over) {
		return over(over, Comparable::compareTo);
	}

	public static Range<Integer> parse(String range) {
		String[] splitRange = range.split("\\.\\.", -1);
		if (splitRange.length != 2)
			throw new IllegalArgumentException();
		try {
			Integer from = splitRange[0].equals("") ? null
					: Integer.parseInt(splitRange[0]);
			Integer to = splitRange[1].equals("") ? null
					: Integer.parseInt(splitRange[1]);
			return between(from, to);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String compose(Range<Integer> range) {
		String from = range.getFrom() == null ? ""
				: (range.isFromInclusive() ? range.getFrom().toString()
						: Integer.toString(range.getFrom() + 1));

		String to = range.getTo() == null ? ""
				: (range.isToInclusive() ? range.getTo().toString()
						: Integer.toString(range.getTo() - 1));

		return from + ".." + to;
	}

	@Override
	public String toString() {
		return (fromInclusive ? "[" : "(") + (from != null ? from : "") + ","
				+ (to != null ? to : "") + (toInclusive ? "]" : ")");
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

	public boolean isFromUnbounded() {
		return from == null;
	}

	public boolean isToUnbounded() {
		return to == null;
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
		int fromCompare = from == null ? 1 : value.compareTo(from);
		int toCompare = to == null ? -1 : value.compareTo(to);

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
		if (getFrom() != null)
			if (range.getFrom() == null) {
				return false;
			} else if (range.isFromInclusive()) {
				if (!contains(range.getFrom()))
					return false;
			} else {
				if (!touches(range.getFrom()))
					return false;
			}

		if (getTo() != null)
			if (range.getTo() == null) {
				return false;
			} else if (range.isToInclusive()) {
				if (!contains(range.getTo()))
					return false;
			} else {
				if (!touches(range.getTo()))
					return false;
			}

		return true;
	}

	public boolean isValueBelow(T value) {
		if (from == null)
			return false;
		if (fromInclusive) {
			return comparator.compare(value, from) < 0;
		} else {
			return comparator.compare(value, from) <= 0;
		}
	}

	public boolean isValueAbove(T value) {
		if (to == null)
			return false;
		if (toInclusive) {
			return comparator.compare(value, to) > 0;
		} else {
			return comparator.compare(value, to) >= 0;
		}
	}

	public boolean isValueBelow(Comparable<? super T> value) {
		if (from == null)
			return false;
		if (fromInclusive) {
			return value.compareTo(from) < 0;
		} else {
			return value.compareTo(from) <= 0;
		}
	}

	public boolean isValueAbove(Comparable<? super T> value) {
		if (to == null)
			return false;
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

	/*
	 * TODO file compiler report with JDK 1.8.0_20... shouldn't need U here.
	 */
	public <U extends T, M extends Property<U, ? super T>> M confine(M value) {
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

	public Range<T> getIntersectedWith(Range<? extends T> other) {
		try {
			return copy().intersectWith(other);
		} catch (Exception e) {
			return null;
		}
	}

	public Range<T> intersectWith(Range<? extends T> other) {
		T newFrom = getFrom();
		boolean newFromInclusive = isFromInclusive();

		int compareFrom = comparator.compare(other.getFrom(), getFrom());
		if (compareFrom == 0) {
			setFromInclusive(isFromInclusive() && other.isFromInclusive());
		} else if (compareFrom > 0) {
			newFrom = other.getFrom();
			newFromInclusive = other.isFromInclusive();
		}

		T newTo = getTo();
		boolean newToInclusive = isToInclusive();

		int compareTo = comparator.compare(other.getTo(), getTo());
		if (compareTo == 0) {
			setToInclusive(isToInclusive() || other.isToInclusive());
		} else if (compareTo < 0) {
			newTo = other.getTo();
			newToInclusive = other.isToInclusive();
		}

		if (comparator.compare(newFrom, newTo) > 0)
			throw new IllegalArgumentException(
					"Ranges '" + this + "' and '" + other + "' do not intersect");

		return getThis().setFrom(newFrom).setTo(newTo)
				.setFromInclusive(newFromInclusive).setToInclusive(newToInclusive);
	}

	@Override
	public Range<T> copy() {
		return new Range<>(from, to, comparator).setInclusive(isFromInclusive(),
				isToInclusive());
	}

	public Range<T> reversed() {
		return new Range<T>(getTo(), getFrom(), getComparator().reversed());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Range))
			return false;
		if (obj == this)
			return true;

		Range<?> otherRange = (Range<?>) obj;

		return Objects.equals(from, otherRange.from)
				&& Objects.equals(to, otherRange.to)
				&& Objects.equals(toInclusive, otherRange.toInclusive)
				&& Objects.equals(fromInclusive, otherRange.fromInclusive);
	}

	@Override
	public int hashCode() {
		return Objects.hash(from) ^ Objects.hash(to);
	}
}
