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
package uk.co.strangeskies.reflection.test.utilities;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class AnnotationToken {
	private String stringRepresentation;
	private Set<Package> packages;

	protected AnnotationToken(String stringRepresentation) {
		this(stringRepresentation, new Class<?>[0]);
	}

	protected AnnotationToken(String stringRepresentation,
			Class<?>... importedClassPackages) {
		this.stringRepresentation = stringRepresentation;

		// Make sure current classloader is aware of all annotation packages
		getAnnotations();

		packages = new HashSet<>();
		for (Class<?> importedPackage : importedClassPackages)
			packages.add(importedPackage.getPackage());
	}

	public String getStringRepresentation() {
		return stringRepresentation;
	}

	public Annotation[] getAnnotations() {
		return getClass().getAnnotatedSuperclass().getAnnotations();
	}

	public Set<Package> getPackages() {
		return new HashSet<>(packages);
	}
}
