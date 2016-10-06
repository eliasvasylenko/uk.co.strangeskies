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

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import uk.co.strangeskies.reflection.test.Container.InnerExtendsInner;
import uk.co.strangeskies.reflection.test.Container.InnerExtendsNested;
import uk.co.strangeskies.reflection.test.Container.InnerExtendsOuter;
import uk.co.strangeskies.reflection.test.Container.Nested;
import uk.co.strangeskies.reflection.test.Container.NestedExtendsNested;
import uk.co.strangeskies.reflection.test.Container.NestedExtendsOuter;
import uk.co.strangeskies.reflection.test.annotations.Plain;

class Outer<T> {}

class OuterExtendsOuter extends Outer<@Plain Object> {}

class OuterExtendsNested extends Nested<@Plain Object> {}

class Container {
	public class Inner<T> {}

	public class InnerExtendsOuter extends Outer<@Plain Object> {}

	public class InnerExtendsNested extends Nested<@Plain Object> {}

	public class InnerExtendsInner extends Inner<@Plain Object> {}

	public static class Nested<T> {}

	public static class NestedExtendsOuter extends Outer<@Plain Object> {}

	public static class NestedExtendsNested extends Nested<@Plain Object> {}
}

/**
 * Test for bug JDK-8146167 in javac.
 * 
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
@RunWith(Parameterized.class)
public class JavacSupertypeAnnotationsTest {

	@Parameters(name = "{0}")
	public static Object[][] testClasses() {
		class StaticInner<T> {}

		class StaticInnerExtendsStaticInner extends StaticInner<@Plain Object> {}

		return new Object[][] {

				/* extends outer pass: */

				{ "outer extends outer", OuterExtendsOuter.class },

				{ "inner extends outer", InnerExtendsOuter.class },

				{ "nested extends outer", NestedExtendsOuter.class },

				{ "anonymous extends outer", new Outer<@Plain Object>() {}.getClass() },

				/* extends nested FAIL: */

				{ "outer extends nested", OuterExtendsNested.class },

				{ "inner extends nested", InnerExtendsNested.class },

				{ "nested extends nested", NestedExtendsNested.class },

				{ "anonymous extends nested", new Nested<@Plain Object>() {}.getClass() },

				/* extends static inner FAIL: */

				{ "static inner extends static inner", StaticInnerExtendsStaticInner.class },

				{ "anonymous extends static inner", new StaticInner<@Plain Object>() {}.getClass() },

				/* extends inner pass: */

				{ "inner extends inner", InnerExtendsInner.class },

				{ "anonymous extends inner", new Container().new Inner<@Plain Object>() {}.getClass() },

		};
	}

	private final Class<?> testClass;

	public JavacSupertypeAnnotationsTest(String testName, Class<?> testClass) {
		this.testClass = testClass;
	}

	@Test
	public void supertypeParameterAnnotationPresenceTest() {
		AnnotatedParameterizedType superType = (AnnotatedParameterizedType) testClass.getAnnotatedSuperclass();

		AnnotatedType superTypeParameter = superType.getAnnotatedActualTypeArguments()[0];

		assertNotNull(superTypeParameter.getAnnotation(Plain.class));
	}
}
