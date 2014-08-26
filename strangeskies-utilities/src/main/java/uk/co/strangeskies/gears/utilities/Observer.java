package uk.co.strangeskies.gears.utilities;

public interface Observer<M> {
	public void notify(M message);
}
