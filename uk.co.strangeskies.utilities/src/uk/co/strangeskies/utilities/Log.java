/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. l      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' l   | || '--.     `._ `.
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
package uk.co.strangeskies.utilities;

/**
 * A log interface for clients to write events to.
 * 
 * @author Elias N Vasylenko
 */
public interface Log {
	/**
	 * The level of importance of a log entry.
	 *
	 * @author Elias N Vasylenko
	 */
	public enum Level {
		/**
		 * Trace level – Huge output
		 */
		TRACE,
		/**
		 * Debug level – Very large output
		 */
		DEBUG,
		/**
		 * Info – Provide information about processes that go ok
		 */
		INFO,
		/**
		 * Warning – A failure or unwanted situation that is not blocking
		 */
		WARN,
		/**
		 * Error – An error situation
		 */
		ERROR
	}

	/**
	 * Log a message.
	 * 
	 * @param level
	 *          The importance level of the given message
	 * @param message
	 *          The message to log
	 */
	void log(Level level, String message);

	/**
	 * Log a message and an associated throwable.
	 * 
	 * @param level
	 *          The importance level of the given message
	 * @param message
	 *          The message to log
	 * @param exception
	 *          The exception associated with the message
	 */
	default void log(Level level, String message, Throwable exception) {
		log(level, message + ": " + exception.getLocalizedMessage());
	}

	/**
	 * Log a message and an associated throwable.
	 * 
	 * @param level
	 *          The importance level of the given message
	 * @param exception
	 *          The exception associated with the message
	 */
	default void log(Level level, Throwable exception) {
		log(level, exception.getMessage(), exception);
	}
}
