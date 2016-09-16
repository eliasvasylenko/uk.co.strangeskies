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

import static uk.co.strangeskies.reflection.LiteralExpression.literal;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.FieldMember;
import uk.co.strangeskies.reflection.InstanceScope;
import uk.co.strangeskies.reflection.InvocableMember;
import uk.co.strangeskies.reflection.Procedure;
import uk.co.strangeskies.reflection.ProcedureDefinition;
import uk.co.strangeskies.reflection.State;
import uk.co.strangeskies.reflection.StaticScope;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.VariableExpression;

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

	private static final TypeToken<String> STRING_TYPE = new TypeToken<String>() {};

	private static final TypeToken<TestClass> TEST_CLASS_TYPE = new TypeToken<TestClass>() {};
	private static final String TEST_FIELD_NAME = "field";
	@SuppressWarnings("unchecked")
	private static final FieldMember<TestClass, String> TEST_FIELD = (FieldMember<TestClass, String>) TEST_CLASS_TYPE
			.resolveField(TEST_FIELD_NAME);

	private static final String TEST_SET_METHOD_NAME = "setMethod";
	@SuppressWarnings("unchecked")
	private static final InvocableMember<TestClass, ?> TEST_SET_METHOD = (InvocableMember<TestClass, Void>) TEST_CLASS_TYPE
			.resolveMethodOverload(TEST_SET_METHOD_NAME, STRING_TYPE);

	@Test
	public void thisAssignmentTest() {
		TestClass testInstance = new TestClass();

		InstanceScope<TestClass> scope = InstanceScope.over(TEST_CLASS_TYPE);
		State state = scope.initializeState(testInstance);

		scope.receiver().accessField(TEST_FIELD).assign(literal("value")).evaluate(state);

		Assert.assertEquals("value", testInstance.field);
	}

	@Test
	public void localAssignmentTest() {
		StaticScope scope = StaticScope.create();
		VariableExpression<String> local = scope.defineVariable(STRING_TYPE);

		State state = scope.initializeState();

		local.assign(literal("value")).evaluate(state);

		Assert.assertEquals("value", local.evaluate(state).get());
	}

	@Test
	public void localVariableStatePersistenceTest() {
		ProcedureDefinition<String> builder = ProcedureDefinition.define(STRING_TYPE);

		VariableExpression<String> local = builder.body().declareVariable(STRING_TYPE);

		builder.body() //
				.addExpression(local.assign(literal("value"))) //
				.addExpression(local.assign(local.invokeMethod(
						STRING_TYPE.resolveMethodOverload("concat", STRING_TYPE).withTargetType(STRING_TYPE), literal("concat")))) //
				.addReturnStatement(local);

		Procedure<String> procedure = builder.instantiate();

		String result = procedure.execute("concat");

		Assert.assertEquals("valueconcat", result);
	}

	@Test
	public void thisMethodInvocationTest() {
		TestClass testInstance = new TestClass();

		InstanceScope<TestClass> scope = InstanceScope.over(TEST_CLASS_TYPE);
		State state = scope.initializeState(testInstance);

		scope.receiver().invokeMethod(TEST_SET_METHOD, literal("value")).evaluate(state);

		Assert.assertEquals("value", testInstance.field);
	}
}
