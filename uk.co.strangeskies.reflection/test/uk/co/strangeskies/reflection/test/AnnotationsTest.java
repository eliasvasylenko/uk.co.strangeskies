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
package uk.co.strangeskies.reflection.test;

import java.lang.annotation.Annotation;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.test.annotations.Plain;
import uk.co.strangeskies.reflection.test.utilities.AnnotationToken;

/**
 * Tests for {@link Annotations} class.
 * 
 * @author Elias N Vasylenko
 */
@RunWith(Theories.class)
public class AnnotationsTest {
	@DataPoint
	public static AnnotationToken PLAIN = new @Plain AnnotationToken(
			"@uk.co.strangeskies.reflection.test.annotations.Plain") {};

	@DataPoint
	public static AnnotationToken PLAIN_IMPORTED = new @Plain AnnotationToken(
			"@Plain", "uk.co.strangeskies.reflection.test.annotations") {};

	@Theory
	public void toStringWithoutPackageImport(AnnotationToken token) {
		Assume.assumeTrue("Assuming no package imports", token.getPackages()
				.isEmpty());
		Annotation annotation = assumeSingleAnnotation(token);

		Assert.assertEquals(token.getStringRepresentation(),
				Annotations.toString(annotation));
	}

	@Theory
	public void toStringWithPackageImport(AnnotationToken token) {
		Assume.assumeFalse("Assuming package imports", token.getPackages()
				.isEmpty());
		Annotation annotation = assumeSingleAnnotation(token);

		Imports imports = Imports.empty();
		for (Package packageImport : token.getPackages())
			imports = imports.withPackageImport(packageImport);

		Assert.assertEquals(token.getStringRepresentation(),
				Annotations.toString(annotation, imports));
	}

	private Annotation assumeSingleAnnotation(AnnotationToken token) {
		Assume.assumeThat("Assuming a single annotation",
				token.getAnnotations().length, Matchers.is(1));
		return token.getAnnotations()[0];
	}
}
