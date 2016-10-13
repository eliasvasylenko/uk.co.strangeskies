/*
 * Copyright (C) 2016 ${copyright.holder.name} <eliasvasylenko@strangeskies.co.uk>
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

import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.codegen.ExpressionVisitor.ValueExpressionVisitor;

/**
 * An expression for evaluation of
 * 
 * @author Elias N Vasylenko
 */
public interface Expression {
	static <T> ValueExpression<T> nullExpression() {
		return new ValueExpression<T>() {
			@Override
			public void accept(ValueExpressionVisitor<T> visitor) {
				visitor.visitNull();
			}

			@Override
			public TypeToken<T> getType() {
				return TypeToken.overNull();
			}
		};
	}

	static ValueExpression<Void> voidExpression() {
		return new ValueExpression<Void>() {
			@Override
			public void accept(ValueExpressionVisitor<Void> visitor) {
				visitor.visitNull();
			}

			@Override
			public TypeToken<Void> getType() {
				return TypeToken.overType(void.class);
			}
		};
	}

	void accept(ExpressionVisitor visitor);
}
