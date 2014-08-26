package uk.co.strangeskies.gears.utilities.flowcontrol;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.gears.utilities.Decorator;

public class LimitedReadWriteLockReleaseWrapper<L> extends
		Decorator<StripedReadWriteLockRelease<L>> implements
		StripedReadWriteLockRelease<L> {
	private final Set<L> readDependencies;
	private final Set<L> writeDependencies;

	public LimitedReadWriteLockReleaseWrapper(StripedReadWriteLock<L> locks,
			Collection<? extends L> readDependencies,
			Collection<? extends L> writeDependencies) {
		super(locks);

		this.readDependencies = new HashSet<>(readDependencies);
		this.writeDependencies = new HashSet<>(writeDependencies);
	}

	public void obtain() throws InterruptedException {
		((StripedReadWriteLock<L>) getComponent()).obtainLocks(readDependencies,
				writeDependencies);
	}

	public void release() throws InterruptedException {
		releaseLocks(readDependencies, writeDependencies);
	}

	@Override
	public boolean releaseLocks(Collection<? extends L> keys) {
		for (L key : keys)
			if (!isLockHeldByCurrentThread(key))
				throw new IllegalStateException();
		for (L key : keys) {
			readDependencies.remove(key);
			writeDependencies.remove(key);
		}

		return getComponent().releaseLocks(keys);
	}

	@Override
	public boolean releaseLocks(Collection<? extends L> readKeys,
			Collection<? extends L> writeKeys) {
		for (L key : readKeys)
			if (!isReadLockHeldByCurrentThread(key))
				throw new IllegalStateException();
		for (L key : writeKeys)
			if (!isWriteLockHeldByCurrentThread(key))
				throw new IllegalStateException();
		for (L key : readKeys)
			readDependencies.remove(key);
		for (L key : writeKeys)
			writeDependencies.remove(key);

		return getComponent().releaseLocks(readKeys, writeKeys);
	}

	@Override
	public boolean releaseLock(L key) {
		if (!readDependencies.remove(key) & !writeDependencies.remove(key))
			throw new IllegalStateException();

		return getComponent().releaseLock(key);
	}

	@Override
	public boolean downgradeLock(L key) {
		if (!writeDependencies.remove(key))
			throw new IllegalStateException();
		return getComponent().downgradeLock(key);
	}

	@Override
	public boolean isLockHeldByCurrentThread(L key) {
		return isReadLockHeldByCurrentThread(key)
				|| isWriteLockHeldByCurrentThread(key);
	}

	@Override
	public boolean releaseReadLocks(Collection<? extends L> readKeys) {
		for (L key : readKeys)
			if (!isReadLockHeldByCurrentThread(key))
				throw new IllegalStateException();
		for (L key : readKeys)
			readDependencies.remove(key);
		return getComponent().releaseReadLocks(readKeys);
	}

	@Override
	public boolean releaseReadLock(L key) {
		if (!readDependencies.remove(key))
			throw new IllegalStateException();
		return getComponent().releaseReadLock(key);
	}

	@Override
	public boolean isReadLockHeldByCurrentThread(L key) {
		return readDependencies.contains(key);
	}

	@Override
	public boolean releaseWriteLocks(Collection<? extends L> writeKeys) {
		for (L key : writeKeys)
			if (!isWriteLockHeldByCurrentThread(key))
				throw new IllegalStateException();
		for (L key : writeKeys)
			writeDependencies.remove(key);
		return getComponent().releaseWriteLocks(writeKeys);
	}

	@Override
	public boolean releaseWriteLock(L key) {
		if (!writeDependencies.remove(key))
			throw new IllegalStateException();
		return getComponent().releaseWriteLock(key);
	}

	@Override
	public boolean isWriteLockHeldByCurrentThread(L key) {
		return writeDependencies.contains(key);
	}

	@Override
	public void wait(L key) throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void wait(L key, long milliseconds) throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void wait(L key, long milliseconds, int nanoseconds)
			throws InterruptedException {
		throw new UnsupportedOperationException();
	}
}
