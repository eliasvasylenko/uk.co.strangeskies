/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.utilities.flowcontrol;

public class Timeout {
	private final Runnable action;
	private int timeoutMilliseconds;
	private final Object lock;

	private boolean ending = false;
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
				ending = false;
				lock.notifyAll();

				return true;
			} else {
				return false;
			}
		}
	}

	public void stop() {
		synchronized (lock) {
			if (thread != null) {
				thread.interrupt();
			}
		}
	}

	public void set() {
		synchronized (lock) {
			if (!reset()) {
				thread = new Thread(() -> {
					synchronized (lock) {
						while (!ending) {
							try {
								ending = true;
								lock.wait(timeoutMilliseconds);
							} catch (InterruptedException e) {
								return;
							}
						}

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
