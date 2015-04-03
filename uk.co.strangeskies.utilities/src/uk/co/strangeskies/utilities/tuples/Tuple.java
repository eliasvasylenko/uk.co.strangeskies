/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.tuples;

import java.util.NoSuchElementException;

/**
 * A tuple entry, parameterised recursively with type information for each
 * subsequent entry. Currently entries must be non-null.
 * 
 * @author Elias N Vasylenko
 *
 * @param <H>
 * @param <T>
 */
public class Tuple<H, T extends Tuple<?, ?>> {
	private H head;
	private T tail;

	protected Tuple(H head) {
		try {
			if (head == null) {
				throw new NullPointerException();
			}
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(e);
		}
		this.head = head;
	}

	/**
	 * Create a Tuple instance with the given values for its head and tail
	 * entries.
	 * 
	 * @param head
	 * @param tail
	 */
	public Tuple(H head, T tail) {
		try {
			if (head == null) {
				throw new NullPointerException();
			}
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(e);
		}
		this.head = head;
		this.tail = tail;
	}

	/**
	 * Return the value of the head entry.
	 * 
	 * @return Head entry/
	 */
	public H getHead() {
		return head;
	}

	/**
	 * Set the value of the head entry.
	 * 
	 * @param head
	 *          New head value. This parameter should be non-null.
	 * 
	 */
	public void setHead(H head) {
		try {
			if (head == null) {
				throw new NullPointerException();
			}
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(e);
		}

		this.head = head;
	}

	/**
	 * Return the value of the tail entry.
	 * 
	 * @return Tail value, which is itself a tuple.
	 */
	public T getTail() {
		if (tail == null) {
			throw new NoSuchElementException();
		}
		return tail;
	}

	/**
	 * Set the tuple entry which represents the tail of this tuple.
	 * 
	 * @param tail
	 *          A new tuple of the exact expected type of the tail of this tuple.
	 */
	public void setTail(T tail) {
		if (this.tail == null) {
			throw new NoSuchElementException();
		}
		try {
			if (tail == null) {
				throw new NullPointerException();
			}
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(e);
		}

		this.tail = tail;
	}

	/**
	 * Does this tuple have a tail, or is it terminating.
	 * 
	 * @return True if the tuple has a tail, false otherwise.
	 */
	public boolean hasTail() {
		return tail != null;
	}

	/**
	 * The number of entries in this tuple, including the head and every entry in
	 * the tail.
	 * 
	 * @return The number of entries.
	 */
	public int getSize() {
		return tailCounter(1);
	}

	protected int tailCounter(int size) {
		if (hasTail()) {
			return getTail().tailCounter(++size);
		} else {
			return size;
		}
	}

	/**
	 * This method prepends the tuple with another tuple entry with the given
	 * value, resulting in a tuple one extra in size. The receiving tuple remains
	 * unchanged.
	 * 
	 * @param prepend
	 *          The head value of the new tuple.
	 * @return A new tuple instance with the given value as its head, and this
	 *         tuple as its tail.
	 */
	public <P> Tuple<P, Tuple<H, T>> prepend(P prepend) {
		return new Tuple<P, Tuple<H, T>>(prepend, this);
	}

	/**
	 * @param head
	 *          The value to set head with.
	 * @return The tail of the tuple, ready to be chained with another invocation
	 *         of {@link Tuple#next(Object)} such that every value can be set.
	 */
	public T next(H head) {
		setHead(head);

		return tail;
	}

	@Override
	public String toString() {
		return "{ " + toSubStrings() + " }";
	}

	protected String toSubStrings() {
		if (hasTail()) {
			return getHead().toString() + ", " + getTail().toSubStrings();
		} else {
			return getHead().toString();
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Tuple)) {
			return false;
		}

		Tuple<?, ?> otherTuple = (Tuple<?, ?>) other;

		if (getSize() != otherTuple.getSize()) {
			return false;
		}

		if (!getHead().equals(otherTuple.getHead())) {
			return false;
		}

		if (hasTail()) {
			return getTail().equals(otherTuple.getTail());
		}

		return true;
	}
}
