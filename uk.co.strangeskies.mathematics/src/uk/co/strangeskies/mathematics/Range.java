/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import uk.co.strangeskies.utility.Property;

public class Range<T> {
  private final T from;
  private final T to;

  private final boolean fromInclusive;
  private final boolean toInclusive;

  private final Comparator<? super T> comparator;

  protected Range(
      T from,
      boolean fromInclusive,
      T to,
      boolean toInclusive,
      Comparator<? super T> comparator) {
    this.from = from;
    this.to = to;

    this.fromInclusive = fromInclusive;
    this.toInclusive = toInclusive;

    this.comparator = comparator;
  }

  public static <T> Range<T> between(T from, T to, Comparator<? super T> comparator) {
    return new Range<T>(from, true, to, true, comparator);
  }

  public static <T extends Comparable<? super T>> Range<T> between(T from, T to) {
    return between(from, to, Comparable::compareTo);
  }

  public static <T> Range<T> over(Collection<? extends T> over, Comparator<? super T> comparator) {
    if (over.isEmpty())
      throw new IllegalArgumentException("No elements given to range over");

    return new Range<T>(
        over.stream().min(comparator).get(),
        true,
        over.stream().max(comparator).get(),
        true,
        comparator);
  }

  public static <T extends Comparable<? super T>> Range<T> over(Collection<? extends T> over) {
    return over(over, Comparable::compareTo);
  }

  public static Range<Integer> parse(String range) {
    String[] splitRange = range.split("\\.\\.", -1);
    if (splitRange.length != 2)
      throw new IllegalArgumentException();
    try {
      Integer from = splitRange[0].equals("") ? null : Integer.parseInt(splitRange[0]);
      Integer to = splitRange[1].equals("") ? null : Integer.parseInt(splitRange[1]);
      return between(from, to);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static String compose(Range<Integer> range) {
    String from = range.getFrom() == null
        ? ""
        : (range.isFromInclusive()
            ? range.getFrom().toString()
            : Integer.toString(range.getFrom() + 1));

    String to = range.getTo() == null
        ? ""
        : (range.isToInclusive() ? range.getTo().toString() : Integer.toString(range.getTo() - 1));

    return from + ".." + to;
  }

  @Override
  public String toString() {
    return (fromInclusive ? "[" : "(") + (from != null ? from : "") + "," + (to != null ? to : "")
        + (toInclusive ? "]" : ")");
  }

  public T getFrom() {
    return from;
  }

  public T getTo() {
    return to;
  }

  public Range<T> withFrom(T from) {
    return new Range<T>(from, fromInclusive, to, toInclusive, comparator);
  }

  public Range<T> withTo(T to) {
    return new Range<T>(from, fromInclusive, to, toInclusive, comparator);
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

  public Range<T> withFromInclusive(boolean fromInclusive) {
    return new Range<T>(from, fromInclusive, to, toInclusive, comparator);
  }

  public Range<T> withToInclusive(boolean toInclusive) {
    return new Range<T>(from, fromInclusive, to, toInclusive, comparator);
  }

  public Range<T> withBoundsInclusive(boolean fromInclusive, boolean toInclusive) {
    return new Range<T>(from, fromInclusive, to, toInclusive, comparator);
  }

  public Range<T> withBoundsInclusive(boolean inclusive) {
    return new Range<T>(from, inclusive, to, inclusive, comparator);
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
  public <M extends Property<T>> M confine(M value) {
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
    T from;
    boolean fromInclusive;

    int compareFrom = comparator.compare(other.getFrom(), getFrom());
    if (compareFrom == 0) {
      from = getFrom();
      fromInclusive = isFromInclusive() || other.isFromInclusive();
    } else if (compareFrom < 0) {
      from = other.getFrom();
      fromInclusive = other.isFromInclusive();
    } else {
      from = getFrom();
      fromInclusive = isFromInclusive();
    }

    T to;
    boolean toInclusive;

    int compareTo = comparator.compare(other.getTo(), getTo());
    if (compareTo == 0) {
      to = getTo();
      toInclusive = isToInclusive() || other.isToInclusive();
    } else if (compareTo > 0) {
      to = other.getTo();
      toInclusive = other.isToInclusive();
    } else {
      to = getTo();
      toInclusive = isToInclusive();
    }

    return new Range<>(from, fromInclusive, to, toInclusive, comparator);
  }

  public Range<T> getExtendedThrough(T other, boolean inclusive) {
    T from;
    boolean fromInclusive;
    T to;
    boolean toInclusive;

    int compareFrom = comparator.compare(other, getFrom());
    int compareTo = comparator.compare(other, getTo());

    if (compareFrom < 0) {
      from = other;
      fromInclusive = inclusive;
      to = getTo();
      toInclusive = isToInclusive();

    } else if (compareTo > 0) {
      from = getFrom();
      fromInclusive = isFromInclusive();
      to = other;
      toInclusive = inclusive;

    } else {
      fromInclusive = (inclusive && compareFrom == 0) || isFromInclusive();
      toInclusive = (inclusive && compareTo == 0) || isToInclusive();

      return new Range<T>(getFrom(), fromInclusive, getTo(), toInclusive, comparator);
    }

    return new Range<T>(from, fromInclusive, to, toInclusive, comparator);
  }

  public Range<T> getIntersectionWith(Range<? extends T> other) {
    T newFrom = getFrom();
    boolean newFromInclusive = isFromInclusive();

    int compareFrom = comparator.compare(other.getFrom(), getFrom());
    if (compareFrom == 0) {
      newFromInclusive = isFromInclusive() && other.isFromInclusive();
    } else if (compareFrom > 0) {
      newFrom = other.getFrom();
      newFromInclusive = other.isFromInclusive();
    }

    T newTo = getTo();
    boolean newToInclusive = isToInclusive();

    int compareTo = comparator.compare(other.getTo(), getTo());
    if (compareTo == 0) {
      newToInclusive = isToInclusive() || other.isToInclusive();
    } else if (compareTo < 0) {
      newTo = other.getTo();
      newToInclusive = other.isToInclusive();
    }

    if (comparator.compare(newFrom, newTo) > 0)
      throw new IllegalArgumentException(
          "Ranges '" + this + "' and '" + other + "' do not intersect");

    return new Range<T>(newFrom, newFromInclusive, newTo, newToInclusive, comparator);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Range))
      return false;
    if (obj == this)
      return true;

    Range<?> otherRange = (Range<?>) obj;

    return Objects.equals(from, otherRange.from) && Objects.equals(to, otherRange.to)
        && Objects.equals(toInclusive, otherRange.toInclusive)
        && Objects.equals(fromInclusive, otherRange.fromInclusive);
  }

  @Override
  public int hashCode() {
    return Objects.hash(from) ^ Objects.hash(to);
  }
}
