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
package uk.co.strangeskies.reflection;

public class LiteralExpressionDefinition<T, I> implements ValueExpressionDefinition<T, I> {
	private final T value;
	private final TypeToken<T> type;

	protected LiteralExpressionDefinition(T value, Class<T> type) {
		this.value = value;
		this.type = TypeToken.over(type);
	}

	public static LiteralExpressionDefinition<String, Object> literal(String value) {
		return new LiteralExpressionDefinition<>(value, String.class);
	}

	public static LiteralExpressionDefinition<Integer, Object> over(int value) {
		return new LiteralExpressionDefinition<>(value, Integer.class);
	}

	public static LiteralExpressionDefinition<Float, Object> over(float value) {
		return new LiteralExpressionDefinition<>(value, Float.class);
	}

	public static LiteralExpressionDefinition<Long, Object> over(long value) {
		return new LiteralExpressionDefinition<>(value, Long.class);
	}

	public static LiteralExpressionDefinition<Double, Object> over(double value) {
		return new LiteralExpressionDefinition<>(value, Double.class);
	}

	public static LiteralExpressionDefinition<Byte, Object> over(byte value) {
		return new LiteralExpressionDefinition<>(value, Byte.class);
	}

	public static LiteralExpressionDefinition<Character, Object> over(char value) {
		return new LiteralExpressionDefinition<>(value, Character.class);
	}

	@Override
	public ValueResult<T> evaluate(State state) {
		return () -> value;
	}

	@Override
	public TypeToken<T> getType() {
		return type;
	}
}
