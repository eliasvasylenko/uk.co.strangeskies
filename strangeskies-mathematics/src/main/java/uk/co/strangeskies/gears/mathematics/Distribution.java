package uk.co.strangeskies.gears.mathematics;

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
