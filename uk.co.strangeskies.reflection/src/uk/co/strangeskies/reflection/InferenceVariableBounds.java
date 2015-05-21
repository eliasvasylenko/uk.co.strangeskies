/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

/**
 * This object describes the bounds present on a particular inference variable
 * within the context of a particular bound set.
 * 
 * @author Elias N Vasylenko
 */
public interface InferenceVariableBounds {

	/**
	 * @return The inference variable the bounds described by this object apply
	 *         to.
	 */
	public abstract InferenceVariable getInferenceVariable();

	/**
	 * @return All equality bounds on the described inference variable.
	 */
	public abstract Set<Type> getEqualities();

	/**
	 * @return All upper bounds on the described inference variable.
	 */
	public abstract Set<Type> getUpperBounds();

	/**
	 * @return All lower bounds on the described inference variable.
	 */
	public abstract Set<Type> getLowerBounds();

	/**
	 * @return All proper upper bounds on the described inference variable.
	 */
	public abstract Set<Type> getProperUpperBounds();

	/**
	 * @return All proper lower bounds on the described inference variable.
	 */
	public abstract Set<Type> getProperLowerBounds();

	/**
	 * @return All instantiations on the described inference variable.
	 */
	public abstract Optional<Type> getInstantiation();

	/**
	 * @return All inference variables related to this one through bounds. This
	 *         set includes the inference variable itself.
	 */
	public abstract Set<InferenceVariable> getDependencies();

	/**
	 * @return All inference variables related to this one through bounds. This
	 *         set includes the inference variable itself.
	 */
	public abstract Set<InferenceVariable> getRemainingDependencies();

}
