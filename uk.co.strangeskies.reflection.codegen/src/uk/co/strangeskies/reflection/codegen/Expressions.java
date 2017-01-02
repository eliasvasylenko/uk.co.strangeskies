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
package uk.co.strangeskies.reflection.codegen;

import static uk.co.strangeskies.reflection.codegen.ClassSignature.classSignature;
import static uk.co.strangeskies.reflection.codegen.InvocationExpression.invokeStatic;

import java.util.concurrent.atomic.AtomicLong;

import uk.co.strangeskies.reflection.codegen.ExpressionVisitor.ValueExpressionVisitor;
import uk.co.strangeskies.reflection.token.TypeToken;

public class Expressions {
	private Expressions() {}

	private static final ValueExpression<Object> NULL_EXPRESSION = new ValueExpression<Object>() {
		@Override
		public void accept(ValueExpressionVisitor<Object> visitor) {
			visitor.visitNull();
		}

		@Override
		public TypeToken<Object> getType() {
			return TypeToken.overNull();
		}
	};

	private static final AtomicLong TYPE_TOKEN_EXPRESSION_COUNT = new AtomicLong(0);

	public static <T> ValueExpression<? extends TypeToken<T>> typeTokenExpression(TypeToken<T> type) {
		ClassDefinition<Void, ? extends TypeToken<T>> typeTokenClass = classSignature(
				"TypeTokenExpression$" + TYPE_TOKEN_EXPRESSION_COUNT.incrementAndGet())
						.withSuperType(type.getThisTypeToken())
						.defineSingleton();

		return invokeStatic(typeTokenClass.getDeclaration().getConstructorDeclaration().asToken());
	}

	@SuppressWarnings("unchecked")
	public static <T> ValueExpression<T> nullExpression() {
		return (ValueExpression<T>) NULL_EXPRESSION;
	}
}
