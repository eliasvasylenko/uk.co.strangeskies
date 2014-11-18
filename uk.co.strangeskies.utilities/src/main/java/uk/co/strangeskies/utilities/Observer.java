package uk.co.strangeskies.utilities;

public interface Observer<M> {
	public void notify(M message);
}
