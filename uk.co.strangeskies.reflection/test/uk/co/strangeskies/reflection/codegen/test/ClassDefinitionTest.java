/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
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
package uk.co.strangeskies.reflection.codegen.test;

import static uk.co.strangeskies.reflection.codegen.ClassSignature.classSignature;
import static uk.co.strangeskies.reflection.codegen.VariableSignature.variableSignature;

import java.util.Set;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.codegen.ClassDeclaration;
import uk.co.strangeskies.reflection.codegen.ClassDefinition;
import uk.co.strangeskies.reflection.codegen.ErasedMethodSignature;
import uk.co.strangeskies.reflection.codegen.VariableSignature;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

@SuppressWarnings("javadoc")
public class ClassDefinitionTest {
	private static final String TEST_CLASS_NAME = ClassDefinitionTest.class.getPackage().getName() + ".SelfSet";
	private static final TypeToken<String> STRING_TYPE = new TypeToken<String>() {};

	public interface StringMethod {
		String method(String parameter);
	}

	public interface ObjectMethod {
		String method(Object parameter);
	}

	public interface NumberMethod<N> {
		Number method(N parameter);
	}

	public interface TMethod<T> {
		T method(String parameter);
	}

	public interface NumberMethodSubType extends NumberMethod<String> {}

	public void runnableClassInvocationTest() {
		ClassDefinition<Void, ? extends Runnable> classDefinition = classSignature(TEST_CLASS_NAME)
				.withSuperType(Runnable.class)
				.declare()
				.define();

		Runnable instance = classDefinition.instantiate().cast();

		instance.run();
	}

	@Test
	public void functionClassInvocationTest() {
		ExecutableToken<? super String, String> concatMethod = STRING_TYPE
				.methods()
				.named("concat")
				.resolveOverload(STRING_TYPE)
				.withTargetType(STRING_TYPE);

		VariableSignature<String> parameterSignature = variableSignature("string", STRING_TYPE);

		ClassDeclaration<Void, ? extends Function<String, String>> classDeclaration = classSignature(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<Function<String, String>>() {})
				.declare();

		Function<String, String> instance = classDeclaration
				.define()
				.defineMethod(
						new ErasedMethodSignature("apply", String.class),
						STRING_TYPE,
						d -> d.withBody(
								b -> b.withReturnStatement(
										d.getParameter(parameterSignature).invokeMethod(concatMethod, d.getParameter(parameterSignature)))))
				.instantiate()
				.cast();

		String result = instance.apply("string");

		Assert.assertEquals("stringstring", result);
	}

	@Test(expected = ReflectionException.class)
	public void supertypesWithIncompatibleParameterizationsTest() {
		classSignature(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<Set<String>>() {}, new TypeToken<Iterable<Number>>() {})
				.declare();
	}

	@Test(expected = ReflectionException.class)
	public void inheritedMethodFromParameterizedTypeCollisionTest() {
		classSignature(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<StringMethod>() {}, new TypeToken<NumberMethod<String>>() {})
				.declare();
	}

	@Test(expected = ReflectionException.class)
	public void inheritedMethodCollisionTest() {
		classSignature(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<ObjectMethod>() {}, new TypeToken<NumberMethod<String>>() {})
				.declare();
	}

	@Test(expected = ReflectionException.class)
	public void indirectlyInheritedMethodCollisionTest() {
		classSignature(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<ObjectMethod>() {}, new TypeToken<NumberMethodSubType>() {})
				.declare();
	}

	@Test
	public void simpleOverrideMethodTest() {
		ClassDeclaration<?, ?> declaration = classSignature(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<ObjectMethod>() {})
				.declare();
		declaration.getMethodDeclaration("method", Object.class);
	}

	@Test(expected = ReflectionException.class)
	public void defineWithAbstractMethodTest() {
		classSignature(TEST_CLASS_NAME).withSuperType(new TypeToken<ObjectMethod>() {}).declare().define();
	}
}
