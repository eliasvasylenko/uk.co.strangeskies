package uk.co.strangeskies.gears.utilities.tuples;

import java.util.NoSuchElementException;

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

	public H getHead() {
		return head;
	}

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

	public T getTail() {
		if (tail == null) {
			throw new NoSuchElementException();
		}
		return tail;
	}

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

	public boolean hasTail() {
		return tail != null;
	}

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

	public <P> Tuple<P, Tuple<H, T>> prepend(P prepend) {
		return new Tuple<P, Tuple<H, T>>(prepend, this);
	}

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
