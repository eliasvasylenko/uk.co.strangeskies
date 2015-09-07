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
package uk.co.strangeskies.utilities.factory;

/**
 * Indicates that {@link Configurator#create()} has been invoked on a
 * {@link Configurator} which has already produced its result, or that the
 * {@link Configurator} has been adjusted in a way which is incompatible with
 * its prior state.
 *
 * @author Elias N Vasylenko
 */
public class InvalidBuildStateException extends BuilderStateException {
	private static final long serialVersionUID = -84782003263925409L;

	private static final String MESSAGE = "Build state is invalid";

	/**
	 * @param configurator
	 *          The configurator with which the problem occurred.
	 */
	public InvalidBuildStateException(Factory<?> configurator) {
		super(configurator, MESSAGE);
	}

	/**
	 * @param configurator
	 *          The configurator with which the problem occurred.
	 */
	public InvalidBuildStateException(Factory<?> configurator, String message) {
		super(configurator, MESSAGE + ": " + message);
	}

	/**
	 * @param configurator
	 *          The configurator with which the problem occurred.
	 * @param cause
	 *          The cause of the problem.
	 */
	public InvalidBuildStateException(Factory<?> configurator, Throwable cause) {
		super(configurator, MESSAGE, cause);
	}

	public InvalidBuildStateException(Factory<?> configurator, String message,
			Throwable cause) {
		super(configurator, MESSAGE + ": " + message, cause);
	}
}
