/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.osgi.frameworkwrapper.impl;

import java.util.jar.Manifest;

import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapperFactory;

/**
 * A simple {@link FrameworkWrapperFactory} implementation returning an instance
 * of {@link FrameworkWrapperImpl}.
 * 
 * @author Elias N Vasylenko
 */
public class FrameworkWrapperFactoryImpl implements FrameworkWrapperFactory {
	@Override
	public FrameworkWrapper getFrameworkWrapper(ClassLoader classLoader, Manifest manifest) {
		return new FrameworkWrapperImpl(classLoader, manifest);
	}
}
