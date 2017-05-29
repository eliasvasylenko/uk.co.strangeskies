/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen.block;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import uk.co.strangeskies.reflection.codegen.ParameterSignature;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface InstructionVisitor {
	<U> ValueExpressionVisitor<U> value(TypeToken<U> expression);

	interface ValueExpressionVisitor<U> {
		VariableExpressionVisitor<U> variable();

		void visitAssignment(Instruction target, Instruction value);

		void visitLiteral(U value);

		void visitNull();

		void visitReceiver(Class<U> classDeclaration);

		void visitMethod(Method method);

		void visitLocal(ParameterSignature<? extends U> local);
	}

	interface VariableExpressionVisitor<U> {
		void visitStaticField(Field field);

		<O> void visitField(Instruction value, Field field);

		void visitLocal(ParameterSignature<U> local);
	}

	void visitReturn();

	<T> void visitReturn(Instruction expression);

	void visitExpression(Instruction expression);

	<T> void visitDeclaration(LocalVariable<T> variable);

	<T> void visitDeclaration(LocalVariable<T> variable, ValueExpression<? extends T> initializer);
}
