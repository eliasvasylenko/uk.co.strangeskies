/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.osgi.
 *
 * uk.co.strangeskies.utilities.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.osgi.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.osgi.logging;

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
public class ConsoleLog implements LogListener {
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
		entry.getException().printStackTrace();
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
