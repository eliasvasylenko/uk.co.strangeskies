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
import java.util.Set;

/**
 *
 * @author Elias N Vasylenko
 */
public class Imports {
	private static final Imports EMPTY = new Imports();

	private final Map<String, Class<?>> namedClasses = new HashMap<>();
	private final Set<Package> packages = new HashSet<>();

	public Imports() {}

	public Imports(Imports imports) {
		namedClasses.putAll(imports.namedClasses);
		packages.addAll(imports.packages);
	}

	public Imports(Collection<? extends Class<?>> classes,
			Collection<? extends Package> packages) {
		importClasses(classes);
		importPackages(packages);
	}

	public Imports withImports(Class<?> classImport) {
		return withImports(classImport);
	}

	public Imports withImports(Class<?>... classes) {
		return withImports(Arrays.asList(classes));
	}

	public Imports withImports(Collection<? extends Class<?>> classes) {
		Imports imports = new Imports(this);
		imports.importClasses(classes);
		return imports;
	}

	private void importClasses(Collection<? extends Class<?>> classes) {
		for (Class<?> clazz : classes) {
			if (namedClasses.putIfAbsent(clazz.getSimpleName(), clazz) != null) {
				throw new TypeException("Cannot import both '"
						+ namedClasses.get(clazz.getSimpleName()) + "' and '" + clazz
						+ "' with the same name.");
			}
		}
	}

	public Set<Class<?>> getImportedClasses() {
		return new HashSet<>(namedClasses.values());
	}

	public Class<?> getNamedClass(String name) {
		return namedClasses.get(name);
	}

	public String getClassName(Class<?> clazz) {
		if (namedClasses.containsValue(clazz)
				|| packages.contains(clazz.getPackage()))
			return clazz.getSimpleName();
		else
			return clazz.getCanonicalName();
	}

	public Imports withPackageImports(Package packageImport) {
		return withPackageImports(packageImport);
	}

	public Imports withPackageImports(Package... packages) {
		return withPackageImports(Arrays.asList(packages));
	}

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

	public Set<Package> getImportedPackages() {
		return new HashSet<>(packages);
	}

	public static Imports empty() {
		return EMPTY;
	}

	public boolean isImported(Class<?> clazz) {
		return packages.contains(clazz.getPackage())
				|| namedClasses.containsValue(clazz);
	}
}
