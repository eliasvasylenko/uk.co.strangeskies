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

import java.util.Collection;

/**
 * The purpose of a striped lock is generally to avoid deadlock in systems with
 * many different locks. This is achieved by requiring {@link Thread}s to
 * acquire all the locks they will need in one go, then releasing them when
 * possible, meaning that a thread will never be waiting for a lock when it
 * already has one.
 * 
 * <p>
 * This class does not enforce that a {@link Thread} does not try to obtain more
 * locks when it already has some, this is expected to be dealt with in user
 * code, though it would be trivial to implement a subclass providing this
 * functionality. Instead the {@link StripedReadWriteLockRelease} interface is
 * provided such that once locks are acquired, processes can be given the means
 * to release and query the state of those locks without leaking the ability to
 * acquire more.
 * 
 * <p>
 * The three overloads of {@link #obtainLocks(Collection, Collection)} will
 * block until every requested lock is obtained, without allowing deadlocks to
 * occur between multiple blocking threads.
 *
 * @author Elias N Vasylenko
 * @param <K>
 */
public interface StripedReadWriteLock<K> extends StripedReadWriteLockRelease<K> {
	/**
	 * Multiple concurrent calls from different threads do not lock. Blocks until
	 * all locks can be obtained (or until interruption).
	 * 
	 * @param readKeys
	 *          Keys for read locks to be obtained
	 * @param writeKeys
	 *          Keys for write locks to be obtained
	 * @throws InterruptedException
	 */
	public void obtainLocks(Collection<K> readKeys, Collection<K> writeKeys)
			throws InterruptedException;

	/**
	 * Multiple concurrent calls from different threads do not lock. Blocks until
	 * all locks can be obtained (or until interruption).
	 * 
	 * @param readKeys
	 *          Keys for read locks to be obtained
	 * @throws InterruptedException
	 */
	public void obtainReadLocks(Collection<K> readKeys)
			throws InterruptedException;

	/**
	 * Multiple concurrent calls from different threads do not lock. Blocks until
	 * all locks can be obtained (or until interruption).
	 * 
	 * @param writeKeys
	 *          Keys for write locks to be obtained
	 * @throws InterruptedException
	 */
	public void obtainWriteLocks(Collection<K> writeKeys)
			throws InterruptedException;

	public void wait(K key) throws InterruptedException;

	public void wait(K key, long milliseconds) throws InterruptedException;

	public void wait(K key, long milliseconds, int nanoseconds)
			throws InterruptedException;
}
