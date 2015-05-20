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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
public class CaptureConversion {
	private final ParameterizedType originalType;
	private final ParameterizedType captureType;

	private final Map<InferenceVariable, TypeVariable<?>> capturedParameters = new HashMap<>();
	private final Map<InferenceVariable, Type> capturedArguments = new HashMap<>();

	/**
	 * Create a capture conversion over a given {@link ParameterizedType}.
	 * Arguments will be substituted with new {@link InferenceVariable}s, such
	 * that a new type is described which represents the result of capture
	 * conversion on the given type.
	 * 
	 * @param originalType
	 *          The type to capture.
	 */
	CaptureConversion(ParameterizedType originalType,
			Map<TypeVariable<?>, InferenceVariable> parameterCaptures) {
		this.originalType = originalType;

		captureType = (ParameterizedType) ParameterizedTypes.from(
				Types.getRawType(originalType), parameterCaptures).getType();

		Map<TypeVariable<?>, Type> parameterArguments = ParameterizedTypes
				.getAllTypeArguments(originalType);

		for (TypeVariable<?> parameter : parameterCaptures.keySet()) {
			Type argument = parameterArguments.get(parameter);
			InferenceVariable inferenceVariable = parameterCaptures.get(parameter);

			capturedArguments.put(inferenceVariable, argument);
			capturedParameters.put(inferenceVariable, parameter);
		}
	}

	/**
	 * Create a capture conversion over a given {@link ParameterizedType}.
	 * Arguments will be substituted with new {@link InferenceVariable}s, such
	 * that a new type is described which represents the result of capture
	 * conversion on the given type.
	 * 
	 * @param originalType
	 *          The type to capture.
	 */
	public CaptureConversion(ParameterizedType originalType) {
		this(originalType,
				ParameterizedTypes
						.getAllTypeParameters(Types.getRawType(originalType))
						.stream()
						.collect(
								Collectors.toMap(Function.identity(),
										t -> new InferenceVariable())));
	}

	@Override
	public String toString() {
		return new StringBuilder().append(getCaptureType().getTypeName())
				.append(" = capture(").append(getOriginalType().getTypeName())
				.append(")").toString();
	}

	/**
	 * @return The original type which has been captured.
	 */
	public ParameterizedType getOriginalType() {
		return originalType;
	}

	/**
	 * @return A {@link ParameterizedType} whose arguments are the same as those
	 *         in the {@link #getOriginalType() original type}, or in the case of
	 *         {@link WildcardTypes}, the {@link InferenceVariable}s which capture
	 *         those arguments.
	 */
	public ParameterizedType getCaptureType() {
		return captureType;
	}

	/**
	 * @return The set of inference variables created through this capture
	 *         conversion operation.
	 */
	public Set<InferenceVariable> getInferenceVariables() {
		return capturedParameters.keySet();
	}

	/**
	 * @param variable
	 *          An inference variable which may represent a capture which is part
	 *          of this capture conversion.
	 * @return The argument of the {@link #getOriginalType() original type}
	 *         captured by a given {@link InferenceVariable}.
	 */
	public Type getCapturedArgument(InferenceVariable variable) {
		return capturedArguments.get(variable);
	}

	/**
	 * @param variable
	 *          An inference variable which may represent a capture which is part
	 *          of this capture conversion.
	 * @return The parameter of the {@link #getOriginalType() original type}
	 *         captured by a given {@link InferenceVariable}.
	 */
	public TypeVariable<?> getCapturedParameter(InferenceVariable variable) {
		return capturedParameters.get(variable);
	}
}
