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
 * The null tuple entry, used for terminating a tuple.
 * 
 * @author Elias N Vasylenko
 *
 */
public final class EmptyTuple extends Tuple<Void, Tuple<?, ?>> {
	private static EmptyTuple NULL_TUPLE = new EmptyTuple();

	private EmptyTuple() {
		super(null, null);
	}

	/**
	 * @return Return an instance of the empty tuple.
	 */
	public static EmptyTuple get() {
		return NULL_TUPLE;
	}

	@Override
	public boolean hasTail() {
		return false;
	}

	@Override
	public int getSize() {
		return 0;
	}
}
