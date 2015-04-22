/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.utilities.Decorator;

public class LimitedReadWriteLockReleaseWrapper<L> extends
		Decorator<StripedReadWriteLock<L>> implements
		StripedReadWriteLockRelease<L> {
	private final Set<L> readDependencies;
	private final Set<L> writeDependencies;

	public LimitedReadWriteLockReleaseWrapper(StripedReadWriteLock<L> locks,
			Collection<? extends L> readDependencies,
			Collection<? extends L> writeDependencies) throws InterruptedException {
		super(locks);

		this.readDependencies = new HashSet<>(readDependencies);
		this.writeDependencies = new HashSet<>(writeDependencies);

		getComponent().obtainLocks(this.readDependencies, this.writeDependencies);
	}

	@Override
	public Set<L> readLocksHeldByCurrentThread() {
		synchronized (readDependencies) {
			return new HashSet<>(readDependencies);
		}
	}

	@Override
	public Set<L> writeLocksHeldByCurrentThread() {
		synchronized (writeDependencies) {
			return new HashSet<>(writeDependencies);
		}
	}

	public void release() throws InterruptedException {
		synchronized (readDependencies) {
			synchronized (writeDependencies) {
				releaseLocks(new ArrayList<>(readDependencies), new ArrayList<>(
						writeDependencies));
			}
		}
	}

	@Override
	public boolean releaseLocks(Collection<? extends L> keys) {
		keys = new ArrayList<>(keys);

		for (L key : keys)
			if (!isLockHeldByCurrentThread(key))
				throw new IllegalStateException();

		synchronized (readDependencies) {
			synchronized (writeDependencies) {
				for (L key : keys) {
					readDependencies.remove(key);
					writeDependencies.remove(key);
				}
			}
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

		synchronized (readDependencies) {
			synchronized (writeDependencies) {
				for (L key : readKeys)
					readDependencies.remove(key);
				for (L key : writeKeys)
					writeDependencies.remove(key);
			}
		}

		return getComponent().releaseLocks(readKeys, writeKeys);
	}

	@Override
	public boolean releaseLock(L key) {
		if (!isReadLockHeldByCurrentThread(key))
			throw new IllegalStateException();
		if (!isWriteLockHeldByCurrentThread(key))
			throw new IllegalStateException();

		synchronized (readDependencies) {
			synchronized (writeDependencies) {
				if (!readDependencies.remove(key) & !writeDependencies.remove(key))
					throw new IllegalStateException();
			}
		}

		return getComponent().releaseLock(key);
	}

	@Override
	public boolean downgradeLock(L key) {
		if (!isReadLockHeldByCurrentThread(key))
			throw new IllegalStateException();
		if (!isWriteLockHeldByCurrentThread(key))
			throw new IllegalStateException();

		synchronized (readDependencies) {
			synchronized (writeDependencies) {
				if (!writeDependencies.remove(key))
					throw new IllegalStateException();
			}
		}

		return getComponent().downgradeLock(key);
	}

	@Override
	public boolean isLockHeldByCurrentThread(L key) {
		synchronized (readDependencies) {
			synchronized (writeDependencies) {
				return isReadLockHeldByCurrentThread(key)
						|| isWriteLockHeldByCurrentThread(key);
			}
		}
	}

	@Override
	public boolean releaseReadLocks(Collection<? extends L> readKeys) {
		for (L key : readKeys)
			if (!isReadLockHeldByCurrentThread(key))
				throw new IllegalStateException();

		synchronized (readDependencies) {
			for (L key : readKeys)
				readDependencies.remove(key);
		}

		return getComponent().releaseReadLocks(readKeys);
	}

	@Override
	public boolean releaseReadLock(L key) {
		if (!isReadLockHeldByCurrentThread(key))
			throw new IllegalStateException();

		synchronized (readDependencies) {
			if (!readDependencies.remove(key))
				throw new IllegalStateException();
		}

		return getComponent().releaseReadLock(key);
	}

	@Override
	public boolean isReadLockHeldByCurrentThread(L key) {
		synchronized (readDependencies) {
			return readDependencies.contains(key);
		}
	}

	@Override
	public boolean releaseWriteLocks(Collection<? extends L> writeKeys) {
		for (L key : writeKeys)
			if (!isWriteLockHeldByCurrentThread(key))
				throw new IllegalStateException();

		synchronized (writeDependencies) {
			for (L key : writeKeys)
				writeDependencies.remove(key);
		}

		return getComponent().releaseWriteLocks(writeKeys);
	}

	@Override
	public boolean releaseWriteLock(L key) {
		if (!isReadLockHeldByCurrentThread(key))
			throw new IllegalStateException();

		synchronized (writeDependencies) {
			if (!writeDependencies.remove(key))
				throw new IllegalStateException();
		}
		return getComponent().releaseWriteLock(key);
	}

	@Override
	public boolean isWriteLockHeldByCurrentThread(L key) {
		synchronized (writeDependencies) {
			return writeDependencies.contains(key);
		}
	}
}
