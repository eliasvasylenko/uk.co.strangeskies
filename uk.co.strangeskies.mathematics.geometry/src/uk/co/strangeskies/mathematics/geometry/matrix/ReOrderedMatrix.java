/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.geometry.
 *
 * uk.co.strangeskies.mathematics.geometry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics.geometry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.geometry.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.geometry.matrix;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Decorator;
import uk.co.strangeskies.utilities.function.TriFunction;

public class ReOrderedMatrix<V extends Value<V>> extends Decorator<Matrix<?, V>>
		implements Matrix<ReOrderedMatrix<V>, V> {
	public ReOrderedMatrix(Matrix<?, V> source) {
		super(source);
	}

	@Override
	public ReOrderedMatrix<V> copy() {
		return new ReOrderedMatrix<>(getComponent().copy());
	}

	@Override
	public int compareTo(Matrix<?, ?> o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ReOrderedMatrix<V> getValue() {
		return this;
	}

	@Override
	public boolean addObserver(Consumer<? super ReOrderedMatrix<V>> observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeObserver(Consumer<? super ReOrderedMatrix<V>> observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Order getOrder() {
		return getComponent().getOrder().getOther();
	}

	@Override
	public Vector<?, V> getMajorVector(int index) {
		return getComponent().getMinorVector(index);
	}

	@Override
	public Vector<?, V> getMinorVector(int index) {
		return getComponent().getMajorVector(index);
	}

	@Override
	public List<V> getData() {
		return getComponent().getData();
	}

	@Override
	public List<List<V>> getData2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMajorSize() {
		return getComponent().getMinorSize();
	}

	@Override
	public int getMinorSize() {
		return getComponent().getMajorSize();
	}

	@Override
	public V getElement(int major, int minor) {
		return getComponent().getElement(minor, major);
	}

	@Override
	public ReOrderedMatrix<V> operateOnData(Function<? super V, ? extends V> operator) {
		getComponent().operateOnData(operator);
		return this;
	}

	@Override
	public ReOrderedMatrix<V> operateOnData(BiFunction<? super V, Integer, ? extends V> operator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> operateOnData2(TriFunction<? super V, Integer, Integer, ? extends V> operator) {
		getComponent().operateOnData2((v, i, j) -> operator.apply(v, j, i));
		return this;
	}

	@Override
	public ReOrderedMatrix<V> transpose() {
		getComponent().transpose();
		return this;
	}

	@Override
	public ReOrderedMatrix<V> multiply(Value<?> value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> multiply(long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> multiply(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> divide(Value<?> value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> divide(long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> divide(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> subtract(Matrix<?, ?> value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> add(Matrix<?, ?> value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> negate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> preMultiply(Matrix<?, ?> value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> multiply(Matrix<?, ?> value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> set(Matrix<?, ?> to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReOrderedMatrix<V> get() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix<?, V> withOrder(Order order) {
		return order == getOrder() ? this : getComponent();
	}

	@Override
	public Vector2<IntValue> getDimensions2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix<?, V> getTransposed() {
		// TODO Auto-generated method stub
		return null;
	}
}
