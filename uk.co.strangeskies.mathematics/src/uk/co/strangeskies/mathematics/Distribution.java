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

import java.util.ArrayList;
import java.util.List;

public class Distribution {
	private final List<Double> values;
	private double average;
	private double meanAbsoluteDeviation;
	private double sum;

	public Distribution() {
		values = new ArrayList<Double>();
	}

	protected void setAverage(double average) {
		this.average = average;
	}

	public double getAverage() {
		return average;
	}

	protected void setSum(double sum) {
		this.sum = sum;
	}

	public double getSum() {
		return sum;
	}

	protected void calculateMeanAbsoluteDeviation() {
		double meanAbsoluteDeviation = 0;
		for (Double existingValue : values) {
			meanAbsoluteDeviation += Math.abs(existingValue - average);
		}
		meanAbsoluteDeviation /= values.size();
		setMeanAbsoluteDeviation(meanAbsoluteDeviation);
	}

	protected void setMeanAbsoluteDeviation(double meanAbsoluteDeviation) {
		this.meanAbsoluteDeviation = meanAbsoluteDeviation;
	}

	public double getMeanAbsoluteDeviation() {
		return meanAbsoluteDeviation;
	}

	protected List<Double> getValues() {
		return values;
	}

	public int getCount() {
		return values.size();
	}

	public void addValue(double value) {
		getValues().add(new Double(value));

		setSum(getSum() + value);
		setAverage(getSum() / getCount());

		calculateMeanAbsoluteDeviation();
	}

	public void clear() {
		values.clear();
		average = 0;
		meanAbsoluteDeviation = 0;
		sum = 0;
	}
}