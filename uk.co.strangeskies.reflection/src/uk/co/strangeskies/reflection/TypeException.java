/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;

/**
 * An exception relating to reflective operations over the Java {@link Type}
 * system.
 * 
 * @author Elias N Vasylenko
 */
public class TypeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 *          A text description of the cause of the problem.
	 * @param cause
	 *          Causing exception.
	 */
	public TypeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 *          A text description of the cause of the problem.
	 */
	public TypeException(String message) {
		super(message);
	}
}
