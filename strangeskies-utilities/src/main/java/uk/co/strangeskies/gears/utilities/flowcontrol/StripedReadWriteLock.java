package uk.co.strangeskies.gears.utilities.flowcontrol;

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