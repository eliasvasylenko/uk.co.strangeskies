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
	 * @return The capture conversion with this inference variable on its left
	 *         side, if one exists.
	 */
	public CaptureConversion getCaptureConversion();

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
	 * @return The instantiation on the described inference variable, if present.
	 */
	public abstract Optional<Type> getInstantiation();

	/**
	 * @return All inference variables related to this one through bounds. This
	 *         set includes the inference variable itself.
	 */
	public abstract Set<InferenceVariable> getRemainingDependencies();

	/**
	 * @return All inference variables related to this one through bounds. This
	 *         set includes the inference variable itself.
	 */
	public abstract Set<InferenceVariable> getRelated();

}
