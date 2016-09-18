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

import static uk.co.strangeskies.reflection.ClassDeclaration.declareClass;

import java.util.Set;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.ClassDefinition;
import uk.co.strangeskies.reflection.MethodDeclaration;
import uk.co.strangeskies.reflection.MethodDefinition;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.VariableExpression;

@SuppressWarnings("javadoc")
public class ClassDefinitionTest {
	private static final String TEST_CLASS_NAME = ClassDefinitionTest.class.getPackage().getName() + ".SelfSet";
	private static final TypeToken<String> STRING_TYPE = new TypeToken<String>() {};

	public interface StringMethod {
		String method(String parameter);

		String method(Object parameter);
	}

	public interface NumberMethod<N> {
		Number method(N parameter);
	}

	public interface TMethod<T> {
		T method(String parameter);
	}

	public interface NumberMethodSubType extends NumberMethod<String> {}

	@Test
	public void runnableClassInvocationTest() {
		ClassDefinition<? extends Runnable> classDefinition = declareClass(TEST_CLASS_NAME).withSuperType(Runnable.class)
				.define();

		MethodDefinition<? extends Runnable, ?> runMethod = classDefinition.declareMethod("run").define();

		Runnable instance = classDefinition.instantiate();

		instance.run();
	}

	@Test
	public void functionClassInvocationTest() {
		ClassDefinition<? extends Function<String, String>> classDefinition = declareClass(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<Function<String, String>>() {}).define();

		MethodDeclaration<? extends Function<String, String>, String> applyMethod = classDefinition.declareMethod("apply")
				.withReturnType(STRING_TYPE);
		VariableExpression<String> parameter = applyMethod.addParameter(STRING_TYPE);
		applyMethod.define().body()
				.addExpression(parameter.assign(parameter.invokeMethod(
						STRING_TYPE.resolveMethodOverload("concat", STRING_TYPE).withTargetType(STRING_TYPE), parameter)))
				.addReturnStatement(parameter);

		Function<String, String> instance = classDefinition.instantiate();

		String result = instance.apply("string");

		Assert.assertEquals("stringconcat", result);
	}

	@Test(expected = ReflectionException.class)
	public void invalidSuperTypeTest() {
		ClassDefinition<? extends Set<?>> classDefinition = declareClass(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<Set<String>>() {}, new TypeToken<Set<Number>>() {}).define();
		classDefinition.validate();
	}

	@Test(expected = ReflectionException.class)
	public void inheritedMethodCollisionTest() {
		declareClass(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<StringMethod>() {}, new TypeToken<NumberMethod<String>>() {}).define().validate();
	}

	@Test(expected = ReflectionException.class)
	public void inheritedMethodCollisionAvoidenceTest() {
		declareClass(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<StringMethod>() {}, new TypeToken<NumberMethod<Integer>>() {}).define().validate();
	}

	@Test(expected = ReflectionException.class)
	public void indirectlyInheritedMethodCollisionTest() {
		declareClass(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<StringMethod>() {}, new TypeToken<NumberMethodSubType>() {}).define().validate();
	}
}
