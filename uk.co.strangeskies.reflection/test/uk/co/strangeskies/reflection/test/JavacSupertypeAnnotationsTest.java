/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.test;

import static org.junit.Assert.assertNotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

abstract class SuperType2<T> {}

/**
 * Test for bug JDK-8146167 in javac.
 * 
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
@RunWith(Parameterized.class)
public class JavacSupertypeAnnotationsTest {
	public static abstract class SuperType<T> {}

	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface TestAnnotation {}

	public static class TestClass extends SuperType<@TestAnnotation Object> {}

	@Parameters
	public static Iterable<?> testClasses() {
		return Arrays.asList(

				TestClass.class,

				new SuperType<@TestAnnotation Object>() {}.getClass(),

				new SuperType<@TestAnnotation List<?>>() {}.getClass(),

				new ArrayList<@TestAnnotation Object>() {}.getClass(),

				new SuperType2<@TestAnnotation Object>() {}.getClass());
	}

	private final Class<?> testClass;

	public JavacSupertypeAnnotationsTest(Class<?> testClass) {
		this.testClass = testClass;
	}

	@Test
	public void testAnnotations() {
		AnnotatedParameterizedType superType = (AnnotatedParameterizedType) testClass.getAnnotatedSuperclass();

		AnnotatedType superTypeParameter = superType.getAnnotatedActualTypeArguments()[0];

		assertNotNull(superTypeParameter.getAnnotation(TestAnnotation.class));
	}
}
