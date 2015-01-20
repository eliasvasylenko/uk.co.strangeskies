/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.expressions.
 *
 * uk.co.strangeskies.expressions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.expressions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.expressions.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.expression;

import uk.co.strangeskies.utilities.Self;

/**
 * A variable for use in reactive programming. A Variable in this sense is a
 * first class expression, that is to say it is an expression whose value is
 * itself.
 *
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          See {@link Self} for more information. This must be self-bounding as
 *          the value of the expression is the variable itself.
 */
public interface Variable<S extends Variable<S>> extends Expression<S>, Self<S> {
}
