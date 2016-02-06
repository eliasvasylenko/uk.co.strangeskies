package uk.co.strangeskies.utilities.flowcontrol;

public class Timeout {
	private final Runnable action;
	private int timeoutMilliseconds;
	private final Object lock;

	private boolean frameworkStarted = false;
	private boolean frameworkStopping = false;
	private Thread thread;

	public Timeout(Runnable action, int timeoutSeconds) {
		this(action, timeoutSeconds, null);
	}

	public Timeout(Runnable action, int timeoutSeconds, Object lock) {
		this.action = action;
		this.timeoutMilliseconds = timeoutSeconds;

		if (lock == null)
			this.lock = this;
		else
			this.lock = lock;
	}

	public boolean reset() {
		synchronized (lock) {
			if (thread != null) {
				frameworkStopping = false;
				notifyAll();

				return true;
			} else {
				return false;
			}
		}
	}

	public void set() {
		synchronized (lock) {
			if (!reset()) {
				thread = new Thread(() -> {
					synchronized (lock) {
						do {
							try {
								if (frameworkStarted) {
									frameworkStopping = true;
									wait(timeoutMilliseconds);
								}
							} catch (InterruptedException e) {}
						} while (!frameworkStopping);

						thread = null;
						action.run();
					}
				});
				thread.start();
			}
		}
	}

	public void setTimeoutMilliseconds(int timeoutMilliseconds) {
		this.timeoutMilliseconds = timeoutMilliseconds;
	}
}
