/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.osgi.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi.frameworkwrapper;

import java.util.jar.Manifest;

import uk.co.strangeskies.text.manifest.ManifestUtilities;

/**
 * A simple interface for constructing a framework wrapper from a given manifest
 * and classloader.
 * 
 * @author Elias N Vasylenko
 */
public interface FrameworkWrapperFactory {
	/**
	 * @param classContext
	 *          the class context from which to derive class loader and manifest
	 * @return a new {@link FrameworkWrapper} instance
	 */
	default FrameworkWrapper getFrameworkWrapper(Class<?> classContext) {
		return getFrameworkWrapper(classContext.getClassLoader(), ManifestUtilities.getManifest(classContext));
	}

	/**
	 * @param classLoader
	 *          the class loader on which to host the framework
	 * @param manifest
	 *          the manifest containing the
	 *          {@link FrameworkWrapper#EMBEDDED_FRAMEWORK} and
	 *          {@link FrameworkWrapper#EMBEDDED_RUNPATH} attributes
	 * @return a new {@link FrameworkWrapper} instance
	 */
	FrameworkWrapper getFrameworkWrapper(ClassLoader classLoader, Manifest manifest);
}
