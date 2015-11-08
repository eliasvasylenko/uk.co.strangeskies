/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * An immutable set of imports of java classes.
 * 
 * @author Elias N Vasylenko
 */
public class Imports {
	private static final Imports EMPTY = new Imports();

	private final Map<String, Class<?>> namedClasses = new HashMap<>();
	private final Set<Package> packages = new HashSet<>();

	private final ClassLoader classLoader;

	private Imports() {
		classLoader = null;
	}

	private Imports(Imports imports) {
		this();

		namedClasses.putAll(imports.namedClasses);
		packages.addAll(imports.packages);
	}

	private Imports(Collection<? extends Class<?>> classes,
			Collection<? extends Package> packages) {
		this(classes, packages, null);
	}

	private Imports(Collection<? extends Class<?>> classes,
			Collection<? extends Package> packages, ClassLoader classLoader) {
		importClasses(classes);
		importPackages(packages);

		this.classLoader = classLoader;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		if (that == null)
			return false;
		if (!Imports.class.isAssignableFrom(that.getClass()))
			return false;
		Imports thatImports = (Imports) that;

		return Objects.equals(namedClasses, thatImports.namedClasses)
				&& Objects.equals(packages, thatImports.packages);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + Objects.hashCode(namedClasses)
				+ prime * Objects.hashCode(packages);
	}

	/**
	 * Derive a new set of imports, including the given class import.
	 * 
	 * @param classImport
	 *          A class import with which to derive a new set of imports.
	 * @return A new set of imports including all those on the receiver, along
	 *         with the given class import.
	 */
	public Imports withImport(Class<?> classImport) {
		return withImports(classImport);
	}

	/**
	 * Derive a new set of imports, including the given class imports.
	 * 
	 * @param classes
	 *          Class imports with which to derive a new set of imports.
	 * @return A new set of imports including all those on the receiver, along
	 *         with the given class imports.
	 */
	public Imports withImports(Class<?>... classes) {
		return withImports(Arrays.asList(classes));
	}

	/**
	 * Derive a new set of imports, including the given class imports.
	 * 
	 * @param classes
	 *          Class imports with which to derive a new set of imports.
	 * @return A new set of imports including all those on the receiver, along
	 *         with the given class imports.
	 */
	public Imports withImports(Collection<? extends Class<?>> classes) {
		Imports imports = new Imports(this);
		imports.importClasses(classes);
		return imports;
	}

	private void importClasses(Collection<? extends Class<?>> classes) {
		for (Class<?> clazz : classes) {
			if (namedClasses.putIfAbsent(clazz.getSimpleName(), clazz) != null) {
				throw new TypeException(
						"Cannot import both '" + namedClasses.get(clazz.getSimpleName())
								+ "' and '" + clazz + "' with the same name.");
			}
		}
	}

	/**
	 * Get all classes directly imported by this set of imports.
	 * 
	 * @return A set of all direct class imports.
	 */
	public Set<Class<?>> getImportedClasses() {
		return new HashSet<>(namedClasses.values());
	}

	/**
	 * Resolve the class object of the given name, allowing full package
	 * qualification to be omitted for included classes.
	 * 
	 * @param name
	 *          The name for which we wish to find the class object.
	 * @return A class object satisfying the given name according to this set of
	 *         imports.
	 */
	public Class<?> getNamedClass(String name) {
		if (classLoader != null) {
			try {
				return getNamedClass(name, classLoader);
			} catch (IllegalArgumentException e) {}
		}

		try {
			return getNamedClass(name, null);
		} catch (IllegalArgumentException e) {}

		return getNamedClass(name, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Resolve the class object of the given name, allowing full package
	 * qualification to be omitted for included classes.
	 * 
	 * @param name
	 *          The name for which we wish to find the class object.
	 * @param classLoader
	 *          The class loader with which to attempt to load the class of the
	 *          given name. If null, the class loader of the calling class will be
	 *          used.
	 * @return A class object satisfying the given name according to this set of
	 *         imports.
	 */
	public Class<?> getNamedClass(String name, ClassLoader classLoader) {
		Optional<Class<?>> primitive = Types.getPrimitives().stream()
				.filter(p -> p.getName().equals(name)).findAny();
		if (primitive.isPresent())
			return primitive.get();

		Class<?> namedClass = namedClasses.get(name);

		if (namedClass == null) {
			try {
				if (classLoader == null) {
					namedClass = Class.forName(name);
				} else {
					namedClass = Class.forName(name, true, classLoader);
				}
			} catch (ClassNotFoundException e) {
				int lastDot;
				String transformedName = name;
				while ((lastDot = transformedName.lastIndexOf('.')) >= 0) {
					transformedName = new StringBuilder(transformedName)
							.replace(lastDot, lastDot + 1, "$").toString();

					try {
						if (classLoader == null) {
							namedClass = Class.forName(transformedName);
						} else {
							namedClass = Class.forName(transformedName, true, classLoader);
						}

						return namedClass;
					} catch (ClassNotFoundException f) {}
				}

				if (namedClass == null)
					throw new IllegalArgumentException("Cannot load class '" + name + "'",
							e);
			}
		}

		return namedClass;
	}

	/**
	 * Resolve the name of a class object, allowing full package qualification to
	 * be omitted for included classes.
	 * 
	 * @param clazz
	 *          The class of which we wish to find the name.
	 * @return A name for the given class object according to this set of imports.
	 */
	public String getClassName(Class<?> clazz) {
		if (namedClasses.containsValue(clazz)
				|| packages.contains(clazz.getPackage()))
			return clazz.getSimpleName();
		else if (clazz.getCanonicalName() != null)
			return clazz.getCanonicalName();
		else
			return clazz.getName();
	}

	/**
	 * Derive a new set of imports, including the given package import.
	 * 
	 * @param packageImport
	 *          A package import with which to derive a new set of imports.
	 * @return A new set of imports including all those on the receiver, along
	 *         with the given package import.
	 */
	public Imports withPackageImport(Package packageImport) {
		return withPackageImports(packageImport);
	}

	/**
	 * Derive a new set of imports, including the given package imports.
	 * 
	 * @param packages
	 *          Package imports with which to derive a new set of imports.
	 * @return A new set of imports including all those on the receiver, along
	 *         with the given package imports.
	 */
	public Imports withPackageImports(Package... packages) {
		return withPackageImports(Arrays.asList(packages));
	}

	/**
	 * Derive a new set of imports, including the given package imports.
	 * 
	 * @param packages
	 *          Package imports with which to derive a new set of imports.
	 * @return A new set of imports including all those on the receiver, along
	 *         with the given package imports.
	 */
	public Imports withPackageImports(Collection<? extends Package> packages) {
		Imports imports = new Imports(this);
		imports.importPackages(packages);
		return imports;
	}

	private void importPackages(Collection<? extends Package> packages) {
		for (Package packageImport : packages) {
			this.packages.add(packageImport);
		}
	}

	/**
	 * Get all packages imported by this set of imports.
	 * 
	 * @return A set of all package imports.
	 */
	public Set<Package> getImportedPackages() {
		return new HashSet<>(packages);
	}

	/**
	 * @return An empty {@link Imports} instance.
	 */
	public static Imports empty() {
		return EMPTY;
	}

	/**
	 * Determine whether a class is imported by this {@link Imports} instance.
	 * 
	 * @param clazz
	 *          The class whose status we wish to determine.
	 * @return True if the class is imported by this {@link Imports} instance,
	 *         either directly, or indirectly through a package import, and false
	 *         otherwise.
	 */
	public boolean isImported(Class<?> clazz) {
		return packages.contains(clazz.getPackage())
				|| namedClasses.containsValue(clazz);
	}
}
