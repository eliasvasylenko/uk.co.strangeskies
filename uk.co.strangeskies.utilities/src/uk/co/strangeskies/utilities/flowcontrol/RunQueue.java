/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.flowcontrol;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * A useful class for certain synchronisation problems. Runs each Runnable
 * passed to it with run(Runnable) or runAndWait(Runnable) in its own thread in
 * the order they are passed. run(Runnable) does not block the calling thread to
 * wait for completion, but runAndWait(Runnable) does.
 * 
 * <p>
 * This works a little like e.g. the swing event queue.
 * 
 * @author Elias N Vasylenko
 * 
 */
public class RunQueue {
	private LinkedBlockingQueue<Runnable> runnables;

	private Thread runThread;

	private boolean killed;
	private boolean dead;

	private long waiting;
	private long releasing;

	private boolean killOnInterrupt;

	/**
	 * Create run queue which does not die when a runnable throws an
	 * {@link InterruptedException}.
	 */
	public RunQueue() {
		this(false);
	}

	/**
	 * Create run queue which may or may not die when a runnable throws an
	 * {@link InterruptedException}.
	 * 
	 * @param killOnInterrupt
	 *          If true, the {@link RunQueue} should die when an
	 *          {@link InterruptedException} is encountered, ignoring any
	 *          remaining queue.
	 */
	public RunQueue(boolean killOnInterrupt) {
		killed = false;
		dead = false;
		this.killOnInterrupt = killOnInterrupt;

		runnables = new LinkedBlockingQueue<Runnable>();
		waiting = 0;
		releasing = 0;

		runThread = new Thread() {
			@Override
			public void run() {
				while (!isKilled()) {
					Runnable runnable = null;
					try {
						runnable = runnables.take();
					} catch (InterruptedException e) {
						killed |= RunQueue.this.killOnInterrupt;
					}

					if (runnable != null) {
						try {
							runnable.run();
						} catch (Exception e) {
							throw e;
						} catch (Error e) {
							throw e;
						}

						synchronized (this) {
							releasing++;
							notifyAll();
						}
					}
				}
				synchronized (runnables) {
					dead = true;
					runnables.notifyAll();
				}
			}
		};
		runThread.start();
	}

	/**
	 * Add a runnable to the back of the queue for execution then return
	 * immediately.
	 * 
	 * @param runnable
	 *          the runnable to add to the queue
	 * @return True if the runnable was added to the queue successfully, false
	 *         otherwise.
	 */
	public synchronized boolean run(Runnable runnable) {
		if (runnable == null || isKilled()) {
			return false;
		}

		waiting++;
		runnables.add(runnable);
		return true;
	}

	/**
	 * Add a runnable to the back of the queue for execution, and block execution
	 * in the invoking thread until done.
	 * 
	 * @param runnable
	 *          the runnable to add to the queue
	 * @return True if the runnable was added to the queue and run successfully,
	 *         false otherwise.
	 * @throws InterruptedException
	 *           Thrown if interrupted when waiting for the runnable to complete.
	 */
	public boolean runAndWait(Runnable runnable) throws InterruptedException {
		long waitingFor;
		synchronized (this) {
			if (runnable == null || isKilled()) {
				return false;
			}

			if (Thread.currentThread() == runThread) {
				runnable.run();
				return true;
			}

			if (!run(runnable)) {
				return false;
			}
			waitingFor = waiting;
		}

		synchronized (runThread) {
			long previousRelease = releasing;
			while (releasing < waitingFor) {
				try {
					runThread.wait();
				} catch (InterruptedException e) {
					if (previousRelease == releasing) {
						throw e;
					}
				}
			}
		}

		return true;
	}

	// TODO could wait forever if queue is killed.
	/**
	 * THe same as {@link #runAndWait(Runnable)}, but ignores interrupts while
	 * waiting for the runnable to be run. Be careful when using this method!
	 * 
	 * @param runnable
	 *          The {@link Runnable} we wish to add to the queue.
	 * @return True the {@link Runnable} was successfully added to the queue,
	 *         false otherwise.
	 */
	public boolean runAndWaitUninterruptible(Runnable runnable) {
		long waitingFor;
		synchronized (this) {
			if (runnable == null || isKilled()) {
				return false;
			}

			if (!run(runnable)) {
				return false;
			}
			waitingFor = waiting;
		}

		synchronized (runThread) {
			while (releasing < waitingFor) {
				try {
					runThread.wait();
				} catch (InterruptedException e) {}
			}
		}

		return true;
	}

	private void waitFor(long waitingFor, boolean interruptible)
			throws InterruptedException {
		synchronized (runThread) {
			while (releasing < waitingFor) {
				try {
					runThread.wait();
				} catch (InterruptedException e) {
					if (interruptible) {
						throw e;
					}
				}
			}
		}
	}

	/**
	 * Find to which position in the queue execution has reached.
	 * 
	 * @return The number of {@link Runnable}s which have already been completed.
	 */
	public synchronized long getQueuePosition() {
		return waiting;
	}

	/**
	 * Wait for the currently running {@link Runnable}.
	 * 
	 * @param interruptible
	 *          True if the invoking thread should stop waiting when interrupted,
	 *          false otherwise.
	 * @throws InterruptedException
	 *           If the thread is interrupted whilst waiting.
	 */
	public void waitForCurrent(boolean interruptible) throws InterruptedException {
		waitFor(getQueuePosition(), interruptible);
	}

	/**
	 * Wait for the currently running {@link Runnable}. As
	 * {@link #waitForCurrent(boolean)} with the argument {@code true} provided to
	 * the single parameter.
	 * 
	 * @throws InterruptedException
	 *           If the thread is interrupted whilst waiting.
	 */
	public void waitForCurrent() throws InterruptedException {
		waitForCurrent(true);
	}

	/**
	 * Wait for the currently running {@link Runnable}. As
	 * {@link #waitForCurrent(boolean)} with the argument {@code false} provided
	 * to the single parameter.
	 */
	public void waitForCurrentUninterruptible() {
		try {
			waitForCurrent(false);
		} catch (InterruptedException e) {}
	}

	/**
	 * Wait until all items in the queue have completed.
	 * 
	 * @param interruptible
	 *          True if the invoking thread should stop waiting when interrupted,
	 *          false otherwise.
	 * @throws InterruptedException
	 *           If the thread is interrupted whilst waiting.
	 */
	public void waitForEmpty(boolean interruptible) throws InterruptedException {
		synchronized (runThread) {
			while (releasing < getQueuePosition()) {
				waitForCurrent(interruptible);
			}
		}
	}

	/**
	 * Wait until all items in the queue have completed. As
	 * {@link #waitForEmpty(boolean)} with the argument {@code true} provided to
	 * the single parameter.
	 * 
	 * @throws InterruptedException
	 *           If the thread is interrupted whilst waiting.
	 */
	public void waitForEmpty() throws InterruptedException {
		waitForEmpty(true);
	}

	/**
	 * Wait until all items in the queue have completed. As
	 * {@link #waitForEmpty(boolean)} with the argument {@code false} provided to
	 * the single parameter.
	 */
	public void waitForEmptyUninterruptible() {
		try {
			waitForEmpty(false);
		} catch (InterruptedException e) {}
	}

	/**
	 * finishes executing queue then dies
	 * 
	 * returns instantly
	 */
	public synchronized void dispose() {
		new Thread() {
			@Override
			public void run() {
				disposeAndWait();
			}
		}.start();
	}

	/**
	 * same as dispose() but blocks until completed execution
	 */
	protected void disposeAndWait() {
		killAndWait();

		Runnable runnable;
		while ((runnable = runnables.poll()) != null) {
			runnable.run();

			synchronized (runThread) {
				releasing++;
				runThread.notifyAll();
			}
		}
	}

	protected synchronized boolean isDisposed() {
		return killed;
	}

	/**
	 * finish executing current if there is one, then die
	 * 
	 * returns instantly
	 * 
	 * @return The remaining {@link Runnable}s left unexecuted.
	 */
	public synchronized LinkedBlockingQueue<Runnable> kill() {
		new Thread() {
			@Override
			public void run() {
				killAndWait();
			}
		}.start();
		return runnables;
	}

	/**
	 * same as kill() but blocks until finished execution
	 * 
	 * @return The remaining {@link Runnable}s left unexecuted.
	 */
	public LinkedBlockingQueue<Runnable> killAndWait() {
		synchronized (this) {
			killed = true;
			runThread.interrupt();
		}

		synchronized (runnables) {
			while (!dead) {
				try {
					runnables.wait();
				} catch (InterruptedException e) {}
			}
		}
		return runnables;
	}

	protected synchronized boolean isKilled() {
		return killed;
	}
}
