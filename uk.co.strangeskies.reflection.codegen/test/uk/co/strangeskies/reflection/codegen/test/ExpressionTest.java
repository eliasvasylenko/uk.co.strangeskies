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

import static uk.co.strangeskies.reflection.codegen.LiteralExpression.literal;
import static uk.co.strangeskies.reflection.codegen.VariableSignature.variableSignature;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.codegen.Block;
import uk.co.strangeskies.reflection.codegen.StatementExecutor;
import uk.co.strangeskies.reflection.codegen.VariableSignature;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.FieldToken;
import uk.co.strangeskies.reflection.token.TypeToken;

@SuppressWarnings("javadoc")
public class ExpressionTest {
	public class TestClass {
		public String field;

		public void setMethod(String value) {
			field = value;
		}

		public String getMethod() {
			return field;
		}
	}

	private static final TypeToken<Object> OBJECT_TYPE = new TypeToken<Object>() {};
	private static final TypeToken<String> STRING_TYPE = new TypeToken<String>() {};

	private static final TypeToken<TestClass> TEST_CLASS_TYPE = new TypeToken<TestClass>() {};
	private static final String TEST_FIELD_NAME = "field";
	private static final FieldToken<TestClass, String> TEST_FIELD = TEST_CLASS_TYPE
			.fields()
			.named(TEST_FIELD_NAME)
			.resolveAccessible()
			.withType(STRING_TYPE);

	private static final String TEST_SET_METHOD_NAME = "setMethod";
	private static final ExecutableToken<? super TestClass, String> TEST_SET_METHOD = TEST_CLASS_TYPE
			.methods()
			.named(TEST_SET_METHOD_NAME)
			.resolveOverload(STRING_TYPE)
			.withTargetType(STRING_TYPE);

	@Test
	public void fieldAssignmentTest() {
		StatementExecutor state = new StatementExecutor();
		Block<Void> block = new Block<>();
		TestClass instance = new TestClass();

		VariableSignature<TestClass> variable = variableSignature("testClass", TEST_CLASS_TYPE);
		state.declareLocal(variable);
		state.setEnclosedLocal(variable, instance);
		block.withExpression(variable.accessField(TEST_FIELD).assign(literal("value")));

		state.executeBlock(block);
		Assert.assertEquals("value", instance.getMethod());
	}

	@Test
	public void localAssignmentTest() {
		StatementExecutor state = new StatementExecutor();
		Block<Void> block = new Block<>();

		VariableSignature<String> local = variableSignature("string", STRING_TYPE);
		state.declareLocal(local.getSignature());
		VariableSignature<Object> result = variableSignature(OBJECT_TYPE);
		state.declareLocal(result.getSignature());

		block.withExpression(local.assign(literal("value")));
		block.withExpression(result.assign(local));

		state.executeBlock(block);
		Assert.assertEquals("value", state.getEnclosedLocal(result.getSignature()));
	}

	@Test
	public void localMethodInvocationTest() {
		StatementExecutor state = new StatementExecutor();
		Block<Void> block = new Block<>();
		TestClass instance = new TestClass();

		VariableSignature<TestClass> variable = variableSignature("testClass", TEST_CLASS_TYPE);
		state.declareLocal(variable.getSignature());
		state.setEnclosedLocal(variable.getSignature(), instance);

		block.withExpression(variable.invokeMethod(TEST_SET_METHOD, literal("value")));
		state.executeBlock(block);

		Assert.assertEquals(instance, state.getEnclosedLocal(variable.getSignature()));
		Assert.assertEquals("value", instance.field);
	}
}
