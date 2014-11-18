package uk.co.strangeskies.mathematics.expression;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ImmutableReadWriteLock implements Lock {
	private class ImmutableCondition implements Condition {
		@Override
		public void signalAll() {
		}

		@Override
		public void signal() {
		}

		@Override
		public boolean awaitUntil(Date deadline) {
			return true;
		}

		@Override
		public void awaitUninterruptibly() {
		}

		@Override
		public long awaitNanos(long nanosTimeout) {
			return 0;
		}

		@Override
		public boolean await(long time, TimeUnit unit) {
			return false;
		}

		@Override
		public void await() {
		}
	}

	@Override
	public void unlock() {
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) {
		return true;
	}

	@Override
	public boolean tryLock() {
		return true;
	}

	@Override
	public Condition newCondition() {
		return new ImmutableCondition();
	}

	@Override
	public void lockInterruptibly() {
	}

	@Override
	public void lock() {
	}
}