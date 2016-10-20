package uk.co.strangeskies.utilities;

/**
 * An observer over one or more {@link Observable} instances.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of message to observe
 */
public interface Observer<T> {
	/**
	 * The method which will receive notification from an {@link Observable}.
	 * 
	 * @param message
	 *          the message object instance
	 */
	void notify(T message);
}
