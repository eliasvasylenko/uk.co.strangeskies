/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,L__   __||  _ `.        / \     |  \   | |  ,-`__`]  ,-`__`]
 *   ( (_`-`   | |   | | ) |       / . \    | . \  | | / .`  `  / .`  `
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `-`.   / /   \ \  | | \ \| || |   | || +--J
 *  \ \__.` /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,-
 *   `.__.-`   L_|   L_|    L_|/_/       \_\L_|   \__|  `-.__.'  `-.__.]
 *                   __    _         _      __      __
 *                 ,`_ `, | |   _   | |  ,-`__`]  ,`_ `,
 *                ( (_`-` | '-.) |  | | / .`  `  ( (_`-`
 *                 `._ `. | +-. <   | || '--.     `._ `.
 *                _   `. \| |  `-`. | || +--J    _   `. \
 *               \ \__.` /| |    \ \| | \ `.__,-\ \__.` /
 *                `.__.-` L_|    L_|L_|  `-.__.] `.__.-`
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

import static uk.co.strangeskies.reflection.FieldMember.resolveField;

import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.reflection.ExpressionVisitor.ValueExpressionVisitor;

public interface ValueExpression<T> extends Expression {
	@Override
	default void accept(ExpressionVisitor visitor) {
		accept(visitor.value(getType()));
	}

	void accept(ValueExpressionVisitor<T> visitor);

	TypeToken<T> getType();

	default <R> FieldExpression<? super T, R> accessField(FieldMember<? super T, R> field) {
		return new FieldExpression<>(this, field);
	}

	default FieldExpression<? super T, ?> accessResolvedField(String fieldName) {
		return accessField(resolveField(getType(), fieldName));
	}

	default <R> MethodExpression<? super T, R> invokeMethod(InvocableMember<? super T, R> invocable,
			ValueExpression<?>... arguments) {
		return invokeMethod(invocable, Arrays.asList(arguments));
	}

	default <R> MethodExpression<? super T, R> invokeMethod(InvocableMember<? super T, R> invocable,
			List<ValueExpression<?>> arguments) {
		return new MethodExpression<>(this, invocable, arguments);
	}

	default <R> MethodExpression<? super T, R> invokeResolvedMethod(String invocableName,
			ValueExpression<?>... arguments) {
		return invokeResolvedMethod(invocableName, Arrays.asList(arguments));
	}

	default <R> MethodExpression<? super T, R> invokeResolvedMethod(String invocableName,
			List<ValueExpression<?>> arguments) {
		/*
		 * TODO resolve method overload
		 */
		return null; /*- new MethodExpression<>(this, InvocableMember.resolveMethodOverload(getType(), invocableName, arguments),
									arguments);*/
	}
}
