package uk.co.strangeskies.gears.utilities.flowcontrol;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A useful class for certain synchronisation problems. Runs each Runnable
 * passed to it with run(Runnable) or runAndWait(Runnable) in its own thread in
 * the order they are passed. run(Runnable) does not block the calling thread to
 * wait for completion, but runAndWait(Runnable) does.
 * 
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
	 * create run queue
	 */
	public RunQueue() {
		this(false);
	}

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
	 * add a runnable to the back of the queue for execution and continue
	 * execution in calling thread
	 * 
	 * @param runnable
	 *          the runnable to add to the queue
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
	 * add a runnable to the back of the queue for execution and block execution
	 * in calling thread until done
	 * 
	 * @param runnable
	 *          the runnable to add to the queue
	 */
	public Boolean runAndWait(Runnable runnable)
			throws RunQueueInterruptedException {
		if (runnable == null || isKilled()) {
			return false;
		}

		if (Thread.currentThread() == runThread) {
			System.out.println("   !!! from runqueue runthread...");
			runnable.run();
			return true;
		}

		long waitingFor;
		synchronized (this) {
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
						throw new RunQueueInterruptedException(e, waitingFor);
					}
				}
			}
		}

		return true;
	}

	/**
	 * same as run and wait, but ignores interrupts while waiting for the runnable
	 * to be run. Be careful when using this method!
	 * 
	 * @param runnable
	 * @return
	 */
	public boolean runAndWaitUninterruptible(Runnable runnable) {
		if (runnable == null || isKilled()) {
			return false;
		}

		long waitingFor;
		synchronized (this) {
			if (!run(runnable)) {
				return false;
			}
			waitingFor = waiting;
		}

		synchronized (runThread) {
			while (releasing < waitingFor) {
				try {
					runThread.wait();
				} catch (InterruptedException e) {
				}
			}
		}

		return true;
	}

	public void waitFor(long waitingFor, boolean interruptible)
			throws RunQueueInterruptedException {
		synchronized (runThread) {
			while (releasing < waitingFor) {
				try {
					runThread.wait();
				} catch (InterruptedException e) {
					if (interruptible) {
						throw new RunQueueInterruptedException(e, waitingFor);
					}
				}
			}
		}
	}

	public void waitFor(long waitingFor) throws RunQueueInterruptedException {
		waitFor(waitingFor, true);
	}

	public void waitForUninterruptible(long waitingFor) {
		try {
			waitFor(waitingFor, false);
		} catch (RunQueueInterruptedException e) {
		}
	}

	public synchronized long getQueuePosition() {
		return waiting;
	}

	public void waitForCurrent(boolean interruptible)
			throws RunQueueInterruptedException {
		waitFor(getQueuePosition(), interruptible);
	}

	public void waitForCurrent() throws RunQueueInterruptedException {
		waitForCurrent(true);
	}

	public void waitForCurrentUninterruptible() {
		try {
			waitForCurrent(false);
		} catch (RunQueueInterruptedException e) {
		}
	}

	public void waitForEmpty(boolean interruptible)
			throws RunQueueInterruptedException {
		synchronized (runThread) {
			while (releasing < getQueuePosition()) {
				waitForCurrent(interruptible);
			}
		}
	}

	public void waitForEmpty() throws RunQueueInterruptedException {
		waitForEmpty(true);
	}

	public void waitForEmptyUninterruptible() {
		try {
			waitForEmpty(false);
		} catch (RunQueueInterruptedException e) {
		}
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
	 * @return remaining unexecuted runnables
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
	 * @return
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
				} catch (InterruptedException e) {
				}
			}
		}
		return runnables;
	}

	protected synchronized boolean isKilled() {
		return killed;
	}

	public static void dumpAllThreads() {
		for (Map.Entry<Thread, StackTraceElement[]> stackTrace : Thread
				.getAllStackTraces().entrySet()) {
			System.out.println(stackTrace.getKey());
			for (StackTraceElement stackTraceElement : stackTrace.getValue()) {
				System.out.println("  " + stackTraceElement);
			}
		}
	}
}
