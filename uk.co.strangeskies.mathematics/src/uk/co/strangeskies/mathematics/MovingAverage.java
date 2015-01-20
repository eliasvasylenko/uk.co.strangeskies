/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics;

public class MovingAverage extends Distribution {
	private boolean full;
	private int size;

	public MovingAverage(int size) {
		this.size = size;
	}

	@Override
	public void addValue(double value) {
		getValues().add(new Double(value));

		setSum(getSum() + value);
		if (full) {
			setSum(getSum() - getValues().remove(0));
		}
		setAverage(getSum() / getCount());

		calculateMeanAbsoluteDeviation();

		if (!full && getCount() == size) {
			full = true;
		}
	}

	@Override
	public void clear() {
		super.clear();
		full = false;
	}

	public void clear(int size) {
		super.clear();
		full = false;
		this.size = size;
	}
}
