/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.flowcontrol;

import java.util.Collection;
import java.util.Set;

public interface StripedReadWriteLockRelease<K> {
	public Set<K> readLocksHeldByCurrentThread();

	public Set<K> writeLocksHeldByCurrentThread();

	public boolean releaseLocks(Collection<? extends K> readKeys,
			Collection<? extends K> writeKeys);

	public boolean releaseLocks(Collection<? extends K> keys);

	public boolean releaseLock(K key);

	public boolean downgradeLock(K key);

	public boolean isLockHeldByCurrentThread(K key);

	public boolean releaseReadLocks(Collection<? extends K> readKeys);

	public boolean releaseReadLock(K key);

	public boolean isReadLockHeldByCurrentThread(K key);

	public boolean releaseWriteLocks(Collection<? extends K> writeKeys);

	public boolean releaseWriteLock(K key);

	public boolean isWriteLockHeldByCurrentThread(K key);

	public void wait(K key) throws InterruptedException;

	public void wait(K key, long milliseconds) throws InterruptedException;

	public void wait(K key, long milliseconds, int nanoseconds)
			throws InterruptedException;
}
