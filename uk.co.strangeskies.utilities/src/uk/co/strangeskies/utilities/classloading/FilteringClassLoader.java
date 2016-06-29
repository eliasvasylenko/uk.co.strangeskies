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
package uk.co.strangeskies.utilities.classloading;

import static java.util.Arrays.stream;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class FilteringClassLoader extends ClassLoader {
	private static final ClassLoader SYSTEM_CLASS_LOADER = ClassLoader.getSystemClassLoader();
	private static final Set<Package> SYSTEM_PACKAGES = new HashSet<>();

	static {
		try {
			Method getPackages = ClassLoader.class.getDeclaredMethod("getPackages");
			getPackages.setAccessible(true);
			Package[] systemPackages = (Package[]) getPackages.invoke(SYSTEM_CLASS_LOADER);

			stream(systemPackages).forEach(SYSTEM_PACKAGES::add);
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	private final Predicate<Class<?>> classFilter;
	private final Predicate<Package> packageFilter;
	private final Predicate<String> resourceFilter;

	public FilteringClassLoader(ClassLoader parent, Predicate<Class<?>> classFilter) {
		this(parent, classFilter, p -> true, r -> true);
	}

	public FilteringClassLoader(ClassLoader parent, Predicate<Class<?>> classFilter, Predicate<Package> packageFilter,
			Predicate<String> resourceFilter) {
		super(parent);
		this.classFilter = classFilter;
		this.packageFilter = packageFilter;
		this.resourceFilter = resourceFilter;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			return SYSTEM_CLASS_LOADER.loadClass(name);
		} catch (ClassNotFoundException e) {}

		Class<?> clazz = super.loadClass(name, false);
		if (resolve) {
			resolveClass(clazz);
		}

		if (!classFilter.test(clazz) || !packageFilter.test(clazz.getPackage())) {
			throw new ClassNotFoundException(name + " not found.");
		}

		return clazz;
	}

	@Override
	protected Package getPackage(String name) {
		Package p = super.getPackage(name);
		if (p == null || !packageFilter.test(p)) {
			return null;
		}
		return p;
	}

	@Override
	protected Package[] getPackages() {
		List<Package> packages = new ArrayList<>();
		for (Package p : super.getPackages()) {
			if (packageFilter.test(p)) {
				packages.add(p);
			}
		}
		return packages.toArray(new Package[0]);
	}

	@Override
	public URL getResource(String name) {
		if (resourceFilter.test(name)) {
			return super.getResource(name);
		}
		return SYSTEM_CLASS_LOADER.getResource(name);
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		if (resourceFilter.test(name)) {
			return super.getResources(name);
		}
		return SYSTEM_CLASS_LOADER.getResources(name);
	}
}
