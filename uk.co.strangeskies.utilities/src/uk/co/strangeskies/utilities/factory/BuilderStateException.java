/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.utilities.factory;

public class BuilderStateException extends RuntimeException {
	private static final long serialVersionUID = -7915027424255184075L;

	public BuilderStateException(Factory<?> configurator, String message) {
		super(message(configurator, message));
	}

	public BuilderStateException(Factory<?> configurator, String message,
			Throwable cause) {
		super(message(configurator, message), cause);
	}

	private static String message(Factory<?> configurator, String message) {
		return "Builder '" + configurator + "' excepetion: " + message;
	}
}
