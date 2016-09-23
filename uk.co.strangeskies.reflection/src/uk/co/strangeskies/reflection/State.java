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

/**
 * @author Elias N Vasylenko
 */
public interface State {
	static State create() {
		return new StateImpl(null, null);
	}

	static State createOver(ReflectiveInstance<?> receiver) {
		return new StateImpl(receiver, null);
	}

	default State enclose() {
		return new StateImpl(null, this);
	}

	default State encloseOver(ReflectiveInstance<?> receiver) {
		return new StateImpl(receiver, this);
	}

	<I> I getEnclosingInstance(ClassDefinition<I> parentScope);

	<T> void declareLocal(LocalValueExpression<T> variable, T initialValue);

	<T> T getEnclosedLocal(LocalValueExpression<T> variable);

	<T> void setEnclosedLocal(LocalVariableExpression<T> variable, T value);

	void returnValue(Object value);

	void returnVoid();

	boolean isReturned();

	Object getReturnValue();
}
