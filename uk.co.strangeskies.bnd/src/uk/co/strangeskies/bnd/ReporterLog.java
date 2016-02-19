/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.bnd.
 *
 * uk.co.strangeskies.bnd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.bnd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.bnd.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.bnd;

import aQute.service.reporter.Reporter;
import uk.co.strangeskies.utilities.Log;

public class ReporterLog implements Log {
	private final Reporter reporter;

	public ReporterLog(Reporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public void log(Level level, String message) {
		switch (level) {
		case TRACE:
		case DEBUG:
		case INFO:
			reporter.trace("%s", message);
			break;
		case WARN:
			reporter.warning("%s", message);
			break;
		case ERROR:
			reporter.error("%s", message);
			break;
		}
	}
}
