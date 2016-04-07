/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import uk.co.strangeskies.utilities.IdentityProperty;

/**
 * A representation of an unknown instantiation of a type variable or inference
 * variable which is known to satisfy a certain set of upper and lower bonds.
 * 
 * @author Elias N Vasylenko
 */
public class TypeVariableCapture implements TypeVariable<GenericDeclaration> {
	private final static AtomicLong COUNTER = new AtomicLong();

	private final String name;

	private final Type source;

	private Type[] upperBounds;
	private final Type[] lowerBounds;

	private final GenericDeclaration declaration;

	TypeVariableCapture(Type[] upperBounds, Type[] lowerBounds, Type source, GenericDeclaration declaration) {
		this.name = "CAP#" + COUNTER.incrementAndGet();

		this.upperBounds = upperBounds.clone();
		this.lowerBounds = lowerBounds.clone();

		this.source = source;

		this.declaration = declaration;
	}

	private final void validate() {
		if (lowerBounds.length > 0 && !Types.isAssignable(IntersectionType.uncheckedFrom(lowerBounds),
				IntersectionType.uncheckedFrom(upperBounds)))
			throw new TypeException("Bounds on capture '" + this + "' are invalid. (" + Arrays.toString(lowerBounds) + " <: "
					+ Arrays.toString(upperBounds) + ")");
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return The {@link InferenceVariable} or {@link WildcardType} captured by
	 *         this type variable capture.
	 */
	public Type getCapturedType() {
		return source;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getName());

		if (upperBounds.length > 0 && !(upperBounds.length == 1 && upperBounds[0] == null))
			builder.append(" extends ").append(IntersectionType.uncheckedFrom(upperBounds));

		if (lowerBounds.length > 0 && !(lowerBounds.length == 1 && lowerBounds[0] == null))
			builder.append(" super ").append(IntersectionType.uncheckedFrom(lowerBounds));

		return builder.toString();
	}

	/**
	 * Determine whether a given type is a valid instantiation of this
	 * TypeVariableCapture, or in other words, whether it is contained by the
	 * bounds of this capture.
	 * 
	 * @param type
	 *          The potential instantiation to validate.
	 * @return True if the instantiation is valid, false otherwise.
	 */
	public boolean isPossibleInstantiation(Type type) {
		return Types.isAssignable(type, getUpperBounds())
				&& (getLowerBounds().length == 0 || Types.isAssignable(IntersectionType.uncheckedFrom(getLowerBounds()), type));
	}

	/**
	 * @return The upper bounds of this TypeVariableCapture.
	 */
	public Type[] getUpperBounds() {
		return upperBounds.clone();
	}

	/**
	 * @return The lower bounds of this TypeVariableCapture.
	 */
	public Type[] getLowerBounds() {
		return lowerBounds.clone();
	}

	static <T extends Type> void substituteBounds(Map<T, Type> captures) {
		TypeSubstitution substitution = new TypeSubstitution(captures);

		for (T type : captures.keySet()) {
			if (captures.get(type) instanceof TypeVariableCapture) {
				TypeVariableCapture capture = (TypeVariableCapture) captures.get(type);

				for (int i = 0; i < capture.upperBounds.length; i++) {
					capture.upperBounds[i] = substitution.resolve(capture.upperBounds[i]);
				}

				for (int i = 0; i < capture.lowerBounds.length; i++)
					capture.lowerBounds[i] = substitution.resolve(capture.lowerBounds[i]);

				capture.validate();
			} else {
				Type capture = substitution.resolve(captures.get(type));
				if (capture instanceof IntersectionType)
					capture = IntersectionType.from(capture);
				captures.put(type, capture);
			}
		}

		for (T type : captures.keySet()) {
			if (captures.get(type) instanceof TypeVariableCapture) {
				TypeVariableCapture capture = (TypeVariableCapture) captures.get(type);

				Type upperBound = Types.greatestLowerBound(capture.upperBounds);
				if (upperBound instanceof IntersectionType)
					capture.upperBounds = ((IntersectionType) upperBound).getTypes();
				else
					capture.upperBounds = new Type[] { upperBound };

				if (!InferenceVariable.isProperType(capture)) {
					throw new TypeException("This type should be proper: " + capture);
				}
			}
		}
	}

	/**
	 * Capture new type variable instantiations over any wildcard arguments of the
	 * given generic array type.
	 * 
	 * @param type
	 *          The generic array type whose arguments we wish to capture.
	 * @return A new parameterized type of the same class as the passed type,
	 *         parameterized with the captures of the original arguments.
	 */
	public static GenericArrayType captureWildcardArguments(GenericArrayType type) {
		Type component = Types.getInnerComponentType(type);

		if (component instanceof ParameterizedType)
			component = captureWildcardArguments((GenericArrayType) component);

		component = type = (GenericArrayType) ArrayTypes.fromComponentType(component, Types.getArrayDimensions(type));

		return type;
	}

	/**
	 * Capture new type variable instantiations over any wildcard arguments of the
	 * given parameterized type.
	 * 
	 * @param type
	 *          The parameterized type whose arguments we wish to capture.
	 * @return A new parameterized type of the same class as the passed type,
	 *         parameterized with the captures of the original arguments.
	 */
	public static ParameterizedType captureWildcardArguments(ParameterizedType type) {
		Map<TypeVariable<?>, Type> arguments = ParameterizedTypes.getAllTypeArgumentsMap(type);
		Map<TypeVariable<?>, Type> captures = new HashMap<>();

		TypeVariable<?>[] parameters = new TypeVariable<?>[arguments.size()];
		GenericDeclaration declaration = createGenericDeclarationOver(parameters);

		boolean containsWildcards = false;

		int i = 0;
		for (TypeVariable<?> parameter : arguments.keySet()) {
			Type argument = arguments.get(parameter);
			Type capture;

			if (argument instanceof WildcardType) {
				containsWildcards = true;
				capture = captureWildcard(declaration, parameter, (WildcardType) argument, false);
			} else
				capture = argument;

			parameters[i++] = parameter;
			captures.put(parameter, capture);
		}

		ParameterizedType capture;
		if (containsWildcards) {
			substituteBounds(captures);
			capture = (ParameterizedType) ParameterizedTypes.uncheckedFrom(Types.getRawType(type), captures::get);
		} else {
			capture = type;
		}

		return capture;
	}

	/**
	 * Capture new type variable instantiation over a given wildcard type.
	 * 
	 * @param typeVariable
	 *          The type variable the new capture of the given wildcard is
	 *          intended to represent an instantiation of.
	 * @param type
	 *          The parameterized type whose arguments we wish to capture.
	 * @return A new parameterized type of the same class as the passed type,
	 *         parameterized with the captures of the original arguments.
	 */
	public static TypeVariableCapture captureWildcard(TypeVariable<?> typeVariable, WildcardType type) {
		return captureWildcard(createGenericDeclarationOver(typeVariable), typeVariable, type, true);
	}

	/**
	 * Capture new type variable instantiation over a given wildcard type.
	 * 
	 * @param type
	 *          The parameterized type whose arguments we wish to capture.
	 * @return A new parameterized type of the same class as the passed type,
	 *         parameterized with the captures of the original arguments.
	 */
	public static TypeVariableCapture captureWildcard(WildcardType type) {
		IdentityProperty<GenericDeclaration> genericDeclaration = new IdentityProperty<>();

		TypeVariable<GenericDeclaration> typeVariable = new TypeVariable<GenericDeclaration>() {
			@Override
			public <T extends Annotation> T getAnnotation(Class<T> arg0) {
				return null;
			}

			@Override
			public Annotation[] getAnnotations() {
				return new Annotation[0];
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return new Annotation[0];
			}

			@Override
			public AnnotatedType[] getAnnotatedBounds() {
				return new AnnotatedType[0];
			}

			@Override
			public Type[] getBounds() {
				return new Type[0];
			}

			@Override
			public GenericDeclaration getGenericDeclaration() {
				return genericDeclaration.get();
			}

			@Override
			public String getName() {
				return "?";
			}
		};

		genericDeclaration.set(createGenericDeclarationOver(typeVariable));

		return captureWildcard(genericDeclaration.get(), typeVariable, type, true);
	}

	private static TypeVariableCapture captureWildcard(GenericDeclaration declaration, TypeVariable<?> typeVariable,
			WildcardType type, boolean validate) {
		Type upperBound;

		List<Type> aggregation = new ArrayList<>(typeVariable.getBounds().length + type.getUpperBounds().length);
		for (int i = 0; i < typeVariable.getBounds().length; i++)
			aggregation.add(typeVariable.getBounds()[i]);
		for (int i = 0; i < type.getUpperBounds().length; i++)
			aggregation.add(type.getUpperBounds()[i]);

		if (validate)
			upperBound = IntersectionType.from(aggregation);
		else
			upperBound = IntersectionType.uncheckedFrom(aggregation);

		Type[] upperBounds;
		if (upperBound instanceof IntersectionType)
			upperBounds = ((IntersectionType) upperBound).getTypes();
		else
			upperBounds = new Type[] { upperBound };

		TypeVariableCapture capture = new TypeVariableCapture(upperBounds, type.getLowerBounds(), type, declaration);

		if (validate)
			capture.validate();

		return capture;
	}

	/**
	 * Capture fresh type variables as valid stand-in instantiations for a set of
	 * inference variables.
	 * 
	 * @param types
	 *          The inference variables to capture.
	 * @param bounds
	 *          The context from which to determine the current bounds on the
	 *          given inference variables, and to incorporate new bounds into.
	 * @return A mapping from the inference variables passes to their new
	 *         captures.
	 */
	public static Map<InferenceVariable, Type> captureInferenceVariables(Collection<? extends InferenceVariable> types,
			BoundSet bounds) {
		TypeVariable<?>[] parameters = new TypeVariable<?>[types.size()];
		GenericDeclaration declaration = createGenericDeclarationOver(parameters);

		TypeSubstitution properTypeSubstitutuion = properTypeSubstitution(types, bounds);

		int count = 0;

		Map<InferenceVariable, Type> typeVariableCaptures = new HashMap<>();
		for (InferenceVariable inferenceVariable : types) {
			Optional<Type> existingMatch = bounds.getBoundsOn(inferenceVariable).getEqualities().stream()
					.filter(typeVariableCaptures::containsKey).findAny();

			if (existingMatch.isPresent()) {
				/*
				 * We have already captured an inference variable equal with this one
				 */
				typeVariableCaptures.put(inferenceVariable, typeVariableCaptures.get(existingMatch.get()));
			} else {
				Set<Type> equalitySet;
				try {
					equalitySet = bounds.getBoundsOn(inferenceVariable).getEqualities().stream()
							.filter(t -> !(t instanceof InferenceVariable)).map(t -> {
								try {
									return properTypeSubstitutuion.resolve(t);
								} catch (TypeException e) {
									throw new TypeException("Equality '" + t + "' on type '" + inferenceVariable
											+ "' cannot be made proper in '" + bounds + "'", e);
								}
							}).collect(Collectors.toSet());
				} catch (TypeException e) {
					equalitySet = Collections.emptySet();
				}

				if (!equalitySet.isEmpty()) {
					typeVariableCaptures.put(inferenceVariable, IntersectionType.from(equalitySet));
				} else {
					/*
					 * For all i (1 ≤ i ≤ n), if αi has one or more proper lower bounds
					 * L1, ..., Lk, then let the lower bound of Yi be lub(L1, ..., Lk); if
					 * not, then Yi has no lower bound.
					 */
					Set<Type> lowerBoundSet = bounds.getBoundsOn(inferenceVariable).getProperLowerBounds();

					Type[] lowerBounds;
					if (lowerBoundSet.isEmpty())
						lowerBounds = new Type[0];
					else {
						Type lub = Types.leastUpperBound(lowerBoundSet);
						lowerBounds = (lub instanceof IntersectionType) ? ((IntersectionType) lub).getTypes() : new Type[] { lub };
					}

					/*
					 * For all i (1 ≤ i ≤ n), where αi has upper bounds U1, ..., Uk, let
					 * the upper bound of Yi be glb(U1 θ, ..., Uk θ), where θ is the
					 * substitution [α1:=Y1, ..., αn:=Yn].
					 */

					Set<Type> upperBoundSet = bounds.getBoundsOn(inferenceVariable).getUpperBounds().stream().map(t -> {
						try {
							return properTypeSubstitutuion.resolve(t);
						} catch (TypeException e) {
							throw new TypeException("Upper bound '" + t + "' on type '" + inferenceVariable
									+ "' cannot be made proper in '" + bounds + "'", e);
						}
					}).collect(Collectors.toSet());

					/*
					 * no need to be checked properly here, as we do this later in
					 * #substituteBounds
					 */
					IntersectionType glb = IntersectionType.uncheckedFrom(upperBoundSet);
					Type[] upperBounds = glb.getTypes();

					/*
					 * If the type variables Y1, ..., Yn do not have well-formed bounds
					 * (that is, a lower bound is not a subtype of an upper bound, or an
					 * intersection type is inconsistent), then resolution fails.
					 */
					TypeVariableCapture capture = new TypeVariableCapture(upperBounds, lowerBounds, inferenceVariable,
							declaration);

					parameters[count++] = capture;

					typeVariableCaptures.put(inferenceVariable, capture);
				}
			}
		}

		bounds.assertConsistent();

		substituteBounds(typeVariableCaptures);

		for (Map.Entry<InferenceVariable, Type> inferenceVariable : typeVariableCaptures.entrySet()) {
			try {
				bounds.incorporate().equality(inferenceVariable.getKey(), inferenceVariable.getValue());
			} catch (TypeException e) {
				throw new TypeException("Cannot instantiate inference variable '" + inferenceVariable.getKey()
						+ "' with capture '" + inferenceVariable.getValue() + "' in bound set '" + bounds + "'", e);
			}
		}

		return typeVariableCaptures;
	}

	private static TypeSubstitution properTypeSubstitution(Collection<? extends InferenceVariable> types,
			BoundSet bounds) {
		return new TypeSubstitution().where(InferenceVariable.class::isInstance, i -> {
			/*
			 * The intent of this substitution is to replace all instances of
			 * inference variables with proper forms where possible.
			 * 
			 * Otherwise, if the inference variable is not contained within the set to
			 * be captured we search for a non-proper equality which only mentions
			 * inference variables which *are* in the set to be captured.
			 * 
			 * The wider purpose of this is to try to ensure that inference variables
			 * in the upper bound may be substituted with captures wherever possible,
			 * such that the bound is ultimately proper.
			 * 
			 * TODO may need to rethink approach to cases like the recent compiler-dev
			 * issue
			 */
			if (bounds.getBoundsOn((InferenceVariable) i).getInstantiation().isPresent()) {
				i = bounds.getBoundsOn((InferenceVariable) i).getInstantiation().get();
			} else if (!types.contains(i)) {
				Set<Type> equalities = bounds.getBoundsOn((InferenceVariable) i).getEqualities();

				Type replacement = null;
				for (Type equality : equalities) {
					if (types.contains(equality)) {
						replacement = equality;
						break;
					}
				}

				if (replacement == null) {
					for (Type equality : equalities) {
						Set<InferenceVariable> mentioned = InferenceVariable.getMentionedBy(equality);
						mentioned.removeAll(types);
						if (mentioned.isEmpty()) {
							replacement = equality;
							break;
						}
					}
				}

				if (replacement == null) {
					throw new TypeException("Could not find appropriate substitution for '" + i + "'");
				}
			}

			return i;
		});
	}

	static GenericDeclaration createGenericDeclarationOver(TypeVariable<?>... captures) {
		return new GenericDeclaration() {
			@Override
			public Annotation[] getDeclaredAnnotations() {
				return new Annotation[0];
			}

			@Override
			public Annotation[] getAnnotations() {
				return new Annotation[0];
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> paramClass) {
				return null;
			}

			@Override
			public TypeVariable<?>[] getTypeParameters() {
				return captures.clone();
			}
		};
	}

	@Override
	public <U extends Annotation> U getAnnotation(Class<U> arg0) {
		return null;
	}

	@Override
	public Annotation[] getAnnotations() {
		return new Annotation[0];
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return new Annotation[0];
	}

	@Override
	public Type[] getBounds() {
		return getUpperBounds();
	}

	@Override
	public GenericDeclaration getGenericDeclaration() {
		return declaration;
	}

	@Override
	public AnnotatedType[] getAnnotatedBounds() {
		AnnotatedType[] annotatedTypes = new AnnotatedType[getBounds().length];
		for (int i = 0; i < getBounds().length; i++) {
			Type bound = getBounds()[i];
			annotatedTypes[i] = new AnnotatedType() {
				@Override
				public Annotation[] getDeclaredAnnotations() {
					return new Annotation[0];
				}

				@Override
				public Annotation[] getAnnotations() {
					return new Annotation[0];
				}

				@Override
				public <T extends Annotation> T getAnnotation(Class<T> paramClass) {
					return null;
				}

				@Override
				public Type getType() {
					return bound;
				}
			};
		}
		return annotatedTypes;
	}
}
