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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Set;

/**
 * A {@link CaptureConversion} is a special sort of bound which can be contained
 * within a {@link BoundSet}. It represents a capture conversion, as the process
 * is described in the Java 8 language specification.
 * <p>
 * The captures made by this capture conversion are not yet fully instantiated,
 * meaning that the types of the capturing variables, and the bounds on those
 * types, may involve {@link InferenceVariable}s.
 * 
 * @author Elias N Vasylenko
 */
public interface CaptureConversion {
	/**
	 * @return The original type which has been captured.
	 */
	public ParameterizedType getOriginalType();

	/**
	 * @return A {@link ParameterizedType} whose arguments are the same as those
	 *         in the {@link #getOriginalType() original type}, or in the case of
	 *         {@link WildcardTypes}, the {@link InferenceVariable}s which capture
	 *         those arguments.
	 */
	public ParameterizedType getCaptureType();

	/**
	 * @return The set of inference variables created through this capture
	 *         conversion operation.
	 */
	public Set<InferenceVariable> getInferenceVariables();

	/**
	 * @param variable
	 *          An inference variable which may represent a capture which is part
	 *          of this capture conversion.
	 * @return The argument of the {@link #getOriginalType() original type}
	 *         captured by a given {@link InferenceVariable}.
	 */
	public Type getCapturedArgument(InferenceVariable variable);

	/**
	 * @param variable
	 *          An inference variable which may represent a capture which is part
	 *          of this capture conversion.
	 * @return The parameter of the {@link #getOriginalType() original type}
	 *         captured by a given {@link InferenceVariable}.
	 */
	public TypeVariable<?> getCapturedParameter(InferenceVariable variable);
}
