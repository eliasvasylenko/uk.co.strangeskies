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
package uk.co.strangeskies.utilities.tuple;


/**
 * A three tuple.
 * 
 * @author Elias N Vasylenko
 *
 * @param <A>
 *          The type of the first item.
 * @param <B>
 *          The type of the second item.
 * @param <C>
 *          The type of the third, and last, item.
 */
public class Triple<A, B, C> extends Tuple<A, Pair<B, C>> {
	/**
	 * Initialise a triple with the given three values.
	 * 
	 * @param a
	 *          The first item.
	 * @param b
	 *          The second item.
	 * @param c
	 *          The third, and last, item.
	 */
	public Triple(A a, B b, C c) {
		super(a, new Pair<>(b, c));
	}

	/**
	 * @return The head value.
	 */
	public A get0() {
		return getHead();
	}

	/**
	 * @return The second value.
	 */
	public B get1() {
		return getTail().getHead();
	}

	/**
	 * @return The third value.
	 */
	public C get2() {
		return getTail().getTail().getHead();
	}
}
