/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,L__   __||  _ `.        / \     |  \   | |  ,-`__`]  ,-`__`]
 *   ( (_`-`   | |   | | ) |       / . \    | . \  | | / .`  `  / .`  `
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `-`.   / /   \ \  | | \ \| || |   | || +--J
 *  \ \__.` /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,-
 *   `.__.-`   L_|   L_|    L_|/_/       \_\L_|   \__|  `-.__.'  `-.__.]
 *                   __    _         _      __      __
 *                 ,`_ `, | |   _   | |  ,-`__`]  ,`_ `,
 *                ( (_`-` | '-.) |  | | / .`  `  ( (_`-`
 *                 `._ `. | +-. <   | || '--.     `._ `.
 *                _   `. \| |  `-`. | || +--J    _   `. \
 *               \ \__.` /| |    \ \| | \ `.__,-\ \__.` /
 *                `.__.-` L_|    L_|L_|  `-.__.] `.__.-`
 *
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi.logging;

import java.util.function.Function;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

/**
 * {@link LogListener} implementation dumping all logs to console
 * 
 * @author Elias N Vasylenko
 */
@Component(immediate = true)
public class LogWrapper implements LogListener {
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	@SuppressWarnings("javadoc")
	public void addLogReader(LogReaderService service) {
		service.addLogListener(this);
	}

	@SuppressWarnings("javadoc")
	public void removeLogReader(LogReaderService service) {
		service.removeLogListener(this);
	}

	@Override
	public void logged(LogEntry entry) {
		String time = formatTime(entry.getTime());
		String level = formatLevel(entry.getLevel());
		String bundle = formatIfPresent(entry.getBundle());
		String service = formatIfPresent(entry.getServiceReference());

		System.out.println(
				"[" + time + level + bundle + service + "] " + entry.getMessage());

		if (entry.getException() != null) {
			entry.getException().printStackTrace();
		}
	}

	private String formatTime(long time) {
		return Long.toString(time);
	}

	private String formatLevel(int level) {
		String levelString;

		switch (level) {
		case LogService.LOG_ERROR:
			levelString = "ERROR  ";
			break;
		case LogService.LOG_WARNING:
			levelString = "WARNING";
			break;
		case LogService.LOG_INFO:
			levelString = "INFO   ";
			break;
		case LogService.LOG_DEBUG:
			levelString = "DEBUG  ";
			break;
		default:
			throw new IllegalArgumentException("Unexpected log level");
		}

		return "; " + levelString;
	}

	private String formatIfPresent(Object object) {
		return formatIfPresent(object, Object::toString);
	}

	private String formatIfPresent(Object object,
			Function<Object, String> format) {
		return object != null ? "; " + format.apply(object) : "";
	}
}
