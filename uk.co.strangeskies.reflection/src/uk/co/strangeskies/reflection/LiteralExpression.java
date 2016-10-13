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
package uk.co.strangeskies.reflection;

import uk.co.strangeskies.reflection.ExpressionVisitor.ValueExpressionVisitor;

public class LiteralExpression<T> implements ValueExpression<T> {
	private final T value;
	private final TypeToken<T> type;

	protected LiteralExpression(T value, Class<T> type) {
		this.value = value;
		this.type = TypeToken.over(type);
	}

	@Override
	public void accept(ValueExpressionVisitor<T> visitor) {
		visitor.visitLiteral(value);
	}

	@Override
	public TypeToken<T> getType() {
		return type;
	}

	public static ValueExpression<String> literal(String value) {
		return new LiteralExpression<>(value, String.class);
	}

	public static ValueExpression<Integer> literal(int value) {
		return new LiteralExpression<>(value, Integer.class);
	}

	public static ValueExpression<Float> literal(float value) {
		return new LiteralExpression<>(value, Float.class);
	}

	public static ValueExpression<Long> literal(long value) {
		return new LiteralExpression<>(value, Long.class);
	}

	public static ValueExpression<Double> literal(double value) {
		return new LiteralExpression<>(value, Double.class);
	}

	public static ValueExpression<Byte> literal(byte value) {
		return new LiteralExpression<>(value, Byte.class);
	}

	public static ValueExpression<Character> literal(char value) {
		return new LiteralExpression<>(value, Character.class);
	}
}
