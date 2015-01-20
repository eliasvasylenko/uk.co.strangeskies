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
}