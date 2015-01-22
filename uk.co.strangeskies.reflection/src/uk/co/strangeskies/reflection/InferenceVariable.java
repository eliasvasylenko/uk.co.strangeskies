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

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InferenceVariable implements Type {
	private final static AtomicLong COUNTER = new AtomicLong();

	private final String name;

	public InferenceVariable(String name) {
		this.name = name + "#" + COUNTER.incrementAndGet();
	}

	public InferenceVariable() {
		this("INF");
	}

	@Override
	public String toString() {
		return name;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Set<InferenceVariable> getAllMentionedBy(Type type) {
		return (Set) Types.getAllMentionedBy(type,
				InferenceVariable.class::isInstance);
	}

	public static boolean isProperType(Type type) {
		return getAllMentionedBy(type).isEmpty();
	}

	public static Map<TypeVariable<?>, InferenceVariable> capture(
			Resolver resolver, GenericDeclaration declaration) {
		List<TypeVariable<?>> declarationVariables;
		if (declaration instanceof Class)
			declarationVariables = ParameterizedTypes
					.getAllTypeParameters((Class<?>) declaration);
		else
			declarationVariables = Arrays.asList(declaration.getTypeParameters());

		Map<TypeVariable<?>, InferenceVariable> captures = declarationVariables
				.stream().collect(
						Collectors.toMap(Function.identity(),
								t -> (InferenceVariable) new InferenceVariable(t.getName())));

		TypeSubstitution substitution = new TypeSubstitution(captures::get);
		for (TypeVariable<?> variable : captures.keySet())
			resolver
					.getBounds()
					.incorporate()
					.acceptSubtype(
							captures.get(variable),
							substitution.resolve(IntersectionType.uncheckedFrom(variable
									.getBounds())));

		return captures;
	}

	/*
	 * Let G name a generic type declaration (§8.1.2, §9.1.2) with n type
	 * parameters A1,...,An with corresponding bounds U1,...,Un.
	 */
	public static Type captureConversion(Type type, BoundSet bounds) {
		if (type instanceof ParameterizedType
				&& ParameterizedTypes.getAllTypeArguments((ParameterizedType) type)
						.values().stream().anyMatch(WildcardType.class::isInstance)) {
			ParameterizedType originalType = (ParameterizedType) type;

			/*
			 * There exists a capture conversion from a parameterized type
			 * G<T1,...,Tn> (§4.5) to a parameterized type G<S1,...,Sn>, where, for 1
			 * ≤ i ≤ n :
			 */

			Map<TypeVariable<?>, Type> parameterArguments = ParameterizedTypes
					.getAllTypeArguments(originalType);
			Map<InferenceVariable, Type> capturedArguments = new HashMap<>();
			Map<InferenceVariable, TypeVariable<?>> capturedParameters = new HashMap<>();

			Map<TypeVariable<?>, InferenceVariable> parameterCaptures = ParameterizedTypes
					.getAllTypeParameters(Types.getRawType(originalType))
					.stream()
					.collect(
							Collectors.toMap(Function.identity(),
									t -> (InferenceVariable) new InferenceVariable()));

			for (TypeVariable<?> parameter : parameterCaptures.keySet()) {
				Type argument = parameterArguments.get(parameter);
				InferenceVariable inferenceVariable = parameterCaptures.get(parameter);

				if (argument instanceof WildcardType) {
					WildcardType wildcardArgument = (WildcardType) argument;
					Type upperBound;
					Type lowerBound;

					if (wildcardArgument.getLowerBounds().length > 0) {
						/*
						 * If Ti is a wildcard type argument of the form ? super Bi, then Si
						 * is a fresh type variable whose upper bound is
						 * Ui[A1:=S1,...,An:=Sn] and whose lower bound is Bi.
						 */
						upperBound = IntersectionType.uncheckedFrom(parameter.getBounds());
						lowerBound = IntersectionType.uncheckedFrom(wildcardArgument
								.getLowerBounds());
					} else if (wildcardArgument.getUpperBounds().length > 0) {
						/*
						 * If Ti is a wildcard type argument of the form ? extends Bi, then
						 * Si is a fresh type variable whose upper bound is glb(Bi,
						 * Ui[A1:=S1,...,An:=Sn]) and whose lower bound is the null type.
						 */
						upperBound = IntersectionType.from(IntersectionType
								.uncheckedFrom(wildcardArgument.getUpperBounds()),
								IntersectionType.uncheckedFrom(parameter.getBounds()));
						lowerBound = null;
					} else {
						/*
						 * If Ti is a wildcard type argument (§4.5.1) of the form ?, then Si
						 * is a fresh type variable whose upper bound is
						 * Ui[A1:=S1,...,An:=Sn] and whose lower bound is the null type
						 * (§4.1).
						 */
						upperBound = IntersectionType.from(parameter.getBounds());
						lowerBound = null;
						;
					}

					upperBound = new TypeSubstitution(parameterCaptures::get)
							.resolve(upperBound);
					bounds.incorporate().acceptSubtype(inferenceVariable, upperBound);

					if (lowerBound != null)
						bounds.incorporate().acceptSubtype(lowerBound, inferenceVariable);
				} else {
					/*
					 * Otherwise, Si = Ti.
					 */
					// TODO do this properly...
					bounds.incorporate().acceptEquality(inferenceVariable, parameter);
				}

				capturedArguments.put(inferenceVariable, argument);
				capturedParameters.put(inferenceVariable, parameter);
			}

			type = (ParameterizedType) ParameterizedTypes.from(
					Types.getRawType(originalType), parameterCaptures).getType();
			ParameterizedType capturedType = (ParameterizedType) type;

			CaptureConversion captureConversion = new CaptureConversion() {
				@Override
				public ParameterizedType getOriginalType() {
					return originalType;
				}

				@Override
				public Set<InferenceVariable> getInferenceVariables() {
					return capturedArguments.keySet();
				}

				@Override
				public Type getCapturedArgument(InferenceVariable variable) {
					return capturedArguments.get(variable);
				}

				@Override
				public TypeVariable<?> getCapturedParameter(InferenceVariable variable) {
					return capturedParameters.get(variable);
				}

				@Override
				public ParameterizedType getCapturedType() {
					return capturedType;
				}

				@Override
				public String toString() {
					return new StringBuilder().append(getCapturedType().getTypeName())
							.append(" = capture(").append(getOriginalType().getTypeName())
							.append(")").toString();
				}
			};

			bounds.incorporate().acceptCaptureConversion(captureConversion);

			return captureConversion.getCapturedType();
		} else
			return type;
	}
}
