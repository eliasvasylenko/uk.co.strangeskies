package uk.co.strangeskies.gears.mathematics;

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
