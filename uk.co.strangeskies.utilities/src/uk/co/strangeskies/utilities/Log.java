package uk.co.strangeskies.utilities;

public interface Log {
	public enum Level {
		/**
		 * Trace level – Huge output
		 */
		TRACE,
		/**
		 * Debug level – Very large output
		 */
		DEBUG,
		/**
		 * Info – Provide information about processes that go ok
		 */
		INFO,
		/**
		 * Warning – A failure or unwanted situation that is not blocking
		 */
		WARN,
		/**
		 * Error – An error situation
		 */
		ERROR
	}

	void log(Level level, String message);

	default void log(Level level, String message, Throwable exception) {
		log(level, message + ": " + exception.getLocalizedMessage());
	}
}
