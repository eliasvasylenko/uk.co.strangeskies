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

import static uk.co.strangeskies.reflection.codegen.ClassSignature.classSignature;
import static uk.co.strangeskies.reflection.codegen.block.InvocationExpression.invokeStatic;

import java.util.concurrent.atomic.AtomicLong;

import uk.co.strangeskies.reflection.codegen.ClassDefinition;
import uk.co.strangeskies.reflection.codegen.ClassRegister;
import uk.co.strangeskies.reflection.token.TypeToken;

public class Expressions {
	private Expressions() {}

	private static final AtomicLong TYPE_TOKEN_EXPRESSION_COUNT = new AtomicLong(0);

	public static <T> ValueExpression<? extends TypeToken<T>> typeTokenExpression(TypeToken<T> type) {
		ClassDefinition<Void, ? extends TypeToken<T>> typeTokenClass = new ClassRegister()
				.withClassSignature(
						classSignature()
								.simpleName("TypeTokenExpression$" + TYPE_TOKEN_EXPRESSION_COUNT.incrementAndGet())
								.extending(type.getThisTypeToken()));

		return invokeStatic(typeTokenClass.getDeclaration().getConstructorDeclaration());
	}

	public static <T> ValueExpression<T> nullLiteral() {
		return (ValueExpression<T>) v -> v.visitNull();
	}

	private static <T> ValueExpression<T> literalImpl(T value) {
		return v -> v.visitLiteral(value);
	}

	public static ValueExpression<String> literal(String value) {
		return literalImpl(value);
	}

	public static ValueExpression<Integer> literal(int value) {
		return literalImpl(value);
	}

	public static ValueExpression<Float> literal(float value) {
		return literalImpl(value);
	}

	public static ValueExpression<Long> literal(long value) {
		return literalImpl(value);
	}

	public static ValueExpression<Double> literal(double value) {
		return literalImpl(value);
	}

	public static ValueExpression<Byte> literal(byte value) {
		return literalImpl(value);
	}

	public static ValueExpression<Character> literal(char value) {
		return literalImpl(value);
	}

	public static <T> ValueExpression<Class<T>> literal(Class<T> value) {
		return literalImpl(value);
	}
}
