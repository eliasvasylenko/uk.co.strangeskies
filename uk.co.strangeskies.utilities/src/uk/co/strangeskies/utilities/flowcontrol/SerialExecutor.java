package uk.co.strangeskies.utilities.flowcontrol;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

public class SerialExecutor implements Executor {
	private final Queue<Runnable> tasks = new ArrayDeque<>();
	private final Executor executor;
	private Runnable active;

	public SerialExecutor(Executor executor) {
		this.executor = executor;
	}

	@Override
	public synchronized void execute(final Runnable r) {
		tasks.offer(() -> {
			try {
				r.run();
			} finally {
				scheduleNext();
			}
		});
		if (active == null) {
			scheduleNext();
		}
	}

	protected synchronized void scheduleNext() {
		if ((active = tasks.poll()) != null) {
			executor.execute(active);
		}
	}
}
