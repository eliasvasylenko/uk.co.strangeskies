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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
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

	private CaptureConversion(ParameterizedType originalType,
			Map<TypeVariable<?>, InferenceVariable> parameterCaptures) {
		this.originalType = originalType;

		captureType = (ParameterizedType) ParameterizedTypes
				.from(Types.getRawType(originalType), parameterCaptures::get).getType();

		Map<TypeVariable<?>, Type> parameterArguments = ParameterizedTypes
				.getAllTypeArgumentsMap(originalType);

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
				ParameterizedTypes.getAllTypeParameters(Types.getRawType(originalType))
						.stream().collect(Collectors.toMap(Function.identity(),
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

	/**
	 * Substitute any mentions of the inference variables present as keys in the
	 * given map with their associated values in the map.
	 * 
	 * @param inferenceVariableSubstitutions
	 *          A mapping from inference variables which may be present in this
	 *          capture conversion, to the inference variables they should be
	 *          substituted with.
	 * @return A new {@link CaptureConversion} instance which is equal to the
	 *         receiving instance but for the substitutions made.
	 */
	public CaptureConversion withInferenceVariableSubstitution(
			Map<InferenceVariable, InferenceVariable> inferenceVariableSubstitutions) {
		ParameterizedType newType = (ParameterizedType) new TypeSubstitution(
				inferenceVariableSubstitutions).resolve(getOriginalType());

		Map<TypeVariable<?>, InferenceVariable> newCaptures = ParameterizedTypes
				.getAllTypeArgumentsMap(getCaptureType()).entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> {
					InferenceVariable substitution = inferenceVariableSubstitutions
							.get(e.getValue());
					if (substitution == null)
						substitution = (InferenceVariable) e.getValue();
					return substitution;
				}));

		return new CaptureConversion(newType, newCaptures);
	}

	/**
	 * Find all inference variables mentioned by this capture conversion.
	 * 
	 * @return A set containing all inference variables mentioned on either side
	 *         of this capture conversion with respect to the given bound set.
	 */
	public Set<InferenceVariable> getInferenceVariablesMentioned() {
		Set<InferenceVariable> allMentioned = new HashSet<>(
				getInferenceVariables());
		for (Type captured : ParameterizedTypes
				.getAllTypeArgumentsMap(getOriginalType()).values())
			allMentioned.addAll(InferenceVariable.getMentionedBy(captured));

		return allMentioned;
	}
}
