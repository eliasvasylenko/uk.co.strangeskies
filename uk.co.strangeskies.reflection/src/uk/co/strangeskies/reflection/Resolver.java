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

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Self;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

/**
 * <p>
 * A resolver maintains a set of <em>inference variables</em>, and
 * <em>inference variable bounds</em>, by way of a {@link BoundSet} instance. It
 * also provides a number of methods to facilitate interaction with these
 * inference variables and bounds, and track from which
 * {@link GenericDeclaration} context they were incorporated or inferred.
 * </p>
 * 
 * <p>
 * This makes for a very flexible and powerful type inference and manipulation
 * engine, though for typical use-cases it may be recommended to use the more
 * limited, but more type safe facilities provided by the {@link TypeToken} and
 * {@link Invokable} classes.
 * </p>
 * 
 * @author Elias N Vasylenko
 */
public class Resolver {
	private class RemainingDependencies
			extends
			MultiHashMap<InferenceVariable, InferenceVariable, Set<InferenceVariable>> {
		private static final long serialVersionUID = 1L;

		public RemainingDependencies() {
			super(HashSet::new);
		}

		public RemainingDependencies(
				MultiMap<? extends InferenceVariable, ? extends InferenceVariable, ? extends Set<InferenceVariable>> that) {
			super(HashSet::new, that);
		}

		private void addRemainingDependency(InferenceVariable variable,
				InferenceVariable dependency) {
			/*
			 * An inference variable α depends on the resolution of an inference
			 * variable β if there exists an inference variable γ such that α depends
			 * on the resolution of γ and γ depends on the resolution of β.
			 */
			if (add(variable, dependency)) {
				for (InferenceVariable transientDependency : get(dependency))
					addRemainingDependency(variable, transientDependency);

				entrySet()
						.stream()
						.filter(e -> e.getValue().contains(variable))
						.map(Entry::getKey)
						.forEach(
								transientDependent -> addRemainingDependency(
										transientDependent, variable));
			}
		}
	}

	private BoundSet bounds;

	/*
	 * We maintain a set of generic declarations which have already been
	 * incorporated into the resolver such that inference variables have been
	 * captured over the type variables where appropriate - and in the case of
	 * Classes, such that bounds on inference variables may be implied for other
	 * classes through enclosing, subtype, and supertype relations.
	 */
	private final Set<GenericDeclaration> capturedDeclarations;
	/*
	 * The extra indirection here, rather than just a Map<TypeVariable<?>,
	 * InferenceVariable> by itself, is because we store TypeVariables for
	 * containing types, meaning otherwise we may have unexpected collisions if we
	 * incorporate two types with different parameterizations of the same
	 * containing type.
	 */
	private final Map<GenericDeclaration, Map<TypeVariable<?>, InferenceVariable>> capturedTypeVariables;

	/**
	 * Create a new resolver over the given bound set.
	 * 
	 * @param bounds
	 */
	public Resolver(BoundSet bounds) {
		this.bounds = bounds;

		capturedDeclarations = new HashSet<>();
		capturedTypeVariables = new HashMap<>();
	}

	/**
	 * Create a new resolver over a new bound set.
	 */
	public Resolver() {
		this(new BoundSet());
	}

	/**
	 * Create a copy of a given resolver, over a copy of that resolver's bound
	 * set.
	 * 
	 * @param that
	 */
	public Resolver(Resolver that) {
		this(new BoundSet(that.bounds));

		capturedDeclarations.addAll(that.capturedDeclarations);
		capturedTypeVariables.putAll(that.capturedTypeVariables);
	}

	/**
	 * @return The bound set backing this resolver.
	 */
	public BoundSet getBounds() {
		return bounds;
	}

	private RemainingDependencies recalculateRemainingDependencies() {
		RemainingDependencies remainingDependencies = new RemainingDependencies();

		Set<InferenceVariable> leftOfCapture = new HashSet<>();

		Set<InferenceVariable> remainingInferenceVariables = new HashSet<>(
				getInferenceVariables());
		remainingInferenceVariables.removeAll(bounds.getInstantiatedVariables());

		/*
		 * An inference variable α depends on the resolution of itself.
		 */
		for (InferenceVariable inferenceVariable : remainingInferenceVariables)
			remainingDependencies.addRemainingDependency(inferenceVariable,
					inferenceVariable);

		/*
		 * An inference variable α appearing on the left-hand side of a bound of the
		 * form G<..., α, ...> = capture(G<...>) depends on the resolution of every
		 * other inference variable mentioned in this bound (on both sides of the =
		 * sign).
		 */
		bounds
				.getCaptureConversions()
				.forEach(
						c -> {
							for (InferenceVariable variable : c.getInferenceVariables()) {
								if (remainingInferenceVariables.contains(variable)) {
									for (InferenceVariable dependency : c.getInferenceVariables())
										if (remainingInferenceVariables.contains(dependency))
											remainingDependencies.addRemainingDependency(variable,
													dependency);
									for (InferenceVariable v : c.getInferenceVariables())
										for (InferenceVariable dependency : bounds
												.getInferenceVariablesMentionedBy(c
														.getCapturedArgument(v)))
											if (remainingInferenceVariables.contains(dependency))
												remainingDependencies.addRemainingDependency(variable,
														dependency);
								}
							}

							leftOfCapture.addAll(c.getInferenceVariables());
						});

		/*
		 * Given a bound of one of the following forms, where T is either an
		 * inference variable β or a type that mentions β:
		 */
		for (InferenceVariable a : bounds.getInferenceVariables()) {
			InferenceVariableBounds aData = bounds.getBoundsOn(a);

			Stream
					.concat(
							/*
							 * α = T, T = α
							 */
							aData.getEqualities().stream(),
							/*
							 * α <: T, T <: α
							 */
							Stream.concat(aData.getLowerBounds().stream(), aData
									.getUpperBounds().stream())
					/*
					 * If α appears on the left-hand side of another bound of the form
					 * G<..., α, ...> = capture(G<...>), then β depends on the resolution
					 * of α. Otherwise, α depends on the resolution of β.
					 */
					).map(bounds::getInferenceVariablesMentionedBy)
					.flatMap(Collection::stream).forEach(b -> {
						if (leftOfCapture.contains(a)) {
							if (remainingInferenceVariables.contains(b))
								remainingDependencies.addRemainingDependency(b, a);
						} else {
							if (remainingInferenceVariables.contains(b))
								remainingDependencies.addRemainingDependency(a, b);
						}
					});
		}

		return remainingDependencies;
	}

	/**
	 * Each type variable within the given {@link GenericDeclaration}, and each
	 * enclosing declaration thereof, is incorporated into the backing
	 * {@link BoundSet} as per
	 * {@link InferenceVariable#capture(BoundSet, GenericDeclaration)}. Each new
	 * {@link InferenceVariable} created in this process is registered in this
	 * {@link Resolver} under the given declaration, even those of enclosing
	 * {@link GenericDeclaration}s.
	 * 
	 * @param declaration
	 */
	public void incorporateTypeParameters(GenericDeclaration declaration) {
		if (capturedDeclarations.add(declaration)) {
			Map<TypeVariable<?>, InferenceVariable> declarationCaptures = new HashMap<>();
			capturedTypeVariables.put(declaration, declarationCaptures);

			Map<TypeVariable<?>, InferenceVariable> newInferenceVariables = InferenceVariable
					.capture(getBounds(), declaration);

			for (TypeVariable<?> typeVariable : newInferenceVariables.keySet()) {
				InferenceVariable inferenceVariable = newInferenceVariables
						.get(typeVariable);

				declarationCaptures.put(typeVariable, inferenceVariable);
			}
		}
	}

	/**
	 * The given type is incorporated into the resolver, in a fashion dictated by
	 * the class of that type, as follows:
	 * 
	 * <ul>
	 * <li>{@link Class} as per
	 * {@link #incorporateTypeParameters(GenericDeclaration)}.</li>
	 * 
	 * <li>{@link ParameterizedType} as per
	 * {@link #captureTypeArguments(ParameterizedType)}.</li>
	 * 
	 * <li>{@link GenericArrayType} as per {@link #incorporateType(Type)} invoked
	 * for it's component type.</li>
	 * 
	 * <li>{@link IntersectionType} as per {@link #incorporateType(Type)} invoked
	 * for each member.</li>
	 * 
	 * <li>{@link WildcardType} as per
	 * {@link #incorporateWildcardType(WildcardType)}.</li>
	 * </ul>
	 * 
	 * @param types
	 */
	public void incorporateType(Type types) {
		new TypeVisitor() {
			@Override
			protected void visitClass(Class<?> t) {
				incorporateTypeParameters(t);
			}

			@Override
			protected void visitParameterizedType(ParameterizedType type) {
				captureTypeArguments(type);
			}

			@Override
			protected void visitGenericArrayType(GenericArrayType type) {
				visit(type.getGenericComponentType());
			}

			@Override
			protected void visitIntersectionType(IntersectionType type) {
				visit(type.getTypes());
			}

			@Override
			protected void visitTypeVariable(TypeVariable<?> type) {}

			@Override
			protected void visitWildcardType(WildcardType type) {
				incorporateWildcardType(type);
			}
		}.visit(types);
	}

	/**
	 * Incorporate a new inference variable for a given wildcard type, and add the
	 * bounds of the wildcard as bounds to the inference variable, as per the
	 * functionality of {@link #addLowerBound(Type, Type)} and
	 * {@link #addUpperBound(Type, Type)}.
	 * 
	 * @param type
	 *          The wildcard type to capture as a bounded inference variable.
	 * @return The new inference variable created to satisfy the given wildcard.
	 */
	public InferenceVariable incorporateWildcardType(WildcardType type) {
		InferenceVariable w = bounds.addInferenceVariable();

		for (Type lowerBound : type.getLowerBounds())
			addLowerBound(w, lowerBound);

		for (Type upperBound : type.getUpperBounds())
			addUpperBound(w, upperBound);

		return w;
	}

	/**
	 * Incorporate a new lower bound into the bound set. If the lower bound is a
	 * parameterized type, or if it is a wildcard or intersection type containing
	 * parameterized types, those parameters which are themselves wildcards will
	 * be replaced by inference variables.
	 * 
	 * @param type
	 *          The type to add the bounding to.
	 * @param lowerBound
	 *          The bounds to add to the given type.
	 */
	public void addLowerBound(Type type, Type lowerBound) {
		for (Type bound : Types.getUpperBounds(lowerBound)) {
			Class<?> rawType = Types.getRawType(bound);
			incorporateTypeParameters(rawType);

			Map<TypeVariable<?>, InferenceVariable> inferenceVariables = getInferenceVariables(rawType);
			ConstraintFormula.reduce(Kind.SUBTYPE,
					ParameterizedTypes.uncheckedFrom(rawType, inferenceVariables), type,
					bounds);

			if (!inferenceVariables.isEmpty()) {
				Map<TypeVariable<?>, Type> arguments = ParameterizedTypes
						.getAllTypeArguments((ParameterizedType) bound);

				for (TypeVariable<?> typeVariable : inferenceVariables.keySet())
					ConstraintFormula.reduce(Kind.CONTAINMENT,
							arguments.get(typeVariable),
							inferenceVariables.get(typeVariable), bounds);
			}
		}
	}

	/**
	 * Incorporate a new upper bound into the bound set. If the upper bound is a
	 * parameterized type, or if it is a wildcard or intersection type containing
	 * parameterized types, those type arguments which are themselves wildcards
	 * will be replaced by inference variables.
	 * 
	 * @param type
	 *          The type to add the bounding to.
	 * @param upperBound
	 *          The bounds to add to the given type.
	 */
	public void addUpperBound(Type type, Type upperBound) {
		addConstraint(Kind.SUBTYPE, type, upperBound);
	}

	/**
	 * Incorporate a new loose compatibility with an upper bound into the bound
	 * set. If the upper bound is a parameterized type, or if it is a wildcard or
	 * intersection type containing parameterized types, those type arguments
	 * which are themselves wildcards will be replaced by inference variables.
	 * 
	 * @param type
	 *          The type to add the bounding to.
	 * @param upperBound
	 *          The bounds to add to the given type.
	 */
	public void addLooseCompatibility(Type type, Type upperBound) {
		addConstraint(Kind.LOOSE_COMPATIBILILTY, type, upperBound);
	}

	private void addConstraint(Kind kind, Type type, Type upperBound) {
		for (Type bound : Types.getLowerBounds(upperBound)) {
			Class<?> rawType = Types.getRawType(bound);
			incorporateTypeParameters(rawType);

			Map<TypeVariable<?>, InferenceVariable> inferenceVariables = getInferenceVariables(rawType);
			ConstraintFormula
					.reduce(kind, type,
							ParameterizedTypes.uncheckedFrom(rawType, inferenceVariables),
							bounds);

			if (bound instanceof ParameterizedType) {
				inferTypeArguments((ParameterizedType) bound);
			}
		}
	}

	/**
	 * Add inference variables for the type parameters of the given type to the
	 * resolver, then incorporate containment constraints based on the arguments
	 * of the given type.
	 * 
	 * @param type
	 * @return A parameterized type derived from the given type, with inference
	 *         variables in place of wildcards where appropriate.
	 */
	public ParameterizedType inferTypeArguments(ParameterizedType type) {
		Class<?> rawType = Types.getRawType(type);

		Map<TypeVariable<?>, InferenceVariable> inferenceVariables = getInferenceVariables(rawType);
		Map<TypeVariable<?>, Type> arguments = ParameterizedTypes
				.getAllTypeArguments(type);

		for (TypeVariable<?> typeVariable : inferenceVariables.keySet())
			ConstraintFormula.reduce(Kind.CONTAINMENT,
					inferenceVariables.get(typeVariable), arguments.get(typeVariable),
					getBounds());

		return (ParameterizedType) resolveType(ParameterizedTypes.uncheckedFrom(
				rawType, inferenceVariables));
	}

	public Class<?> getRawType(Type type) {
		if (getBounds().getInferenceVariables().contains(type))
			return Types.getRawType(IntersectionType.uncheckedFrom(getBounds()
					.getBoundsOn((InferenceVariable) type).getUpperBounds()));
		else
			return Types.getRawType(resolveType(type));
	}

	public ParameterizedType captureTypeArguments(ParameterizedType type) {
		Class<?> rawType = Types.getRawType(type);
		incorporateTypeParameters(rawType);

		type = TypeVariableCapture.captureWildcardArguments(type);

		for (Map.Entry<TypeVariable<?>, Type> typeArgument : ParameterizedTypes
				.getAllTypeArguments(type).entrySet())
			ConstraintFormula.reduce(Kind.EQUALITY, capturedTypeVariables
					.get(rawType).get(typeArgument.getKey()), typeArgument.getValue(),
					bounds);

		return type;
	}

	public void incorporateInstantiation(TypeVariable<?> variable,
			Type instantiation) {
		incorporateTypeParameters(variable.getGenericDeclaration());
		ConstraintFormula.reduce(Kind.EQUALITY, getInferenceVariable(variable),
				instantiation, bounds);
	}

	public Map<InferenceVariable, Type> infer(GenericDeclaration context) {
		return infer(getInferenceVariables(context).values());
	}

	public Map<InferenceVariable, Type> infer() {
		infer(getInferenceVariables());
		return bounds
				.getInstantiatedVariables()
				.stream()
				.collect(
						Collectors.toMap(Function.identity(), i -> bounds.getBoundsOn(i)
								.getInstantiation().get()));
	}

	public Type infer(InferenceVariable variable) {
		return infer(Arrays.asList(variable)).get(variable);
	}

	public Map<InferenceVariable, Type> infer(InferenceVariable... variables) {
		return infer(Arrays.asList(variables));
	}

	public Map<InferenceVariable, Type> infer(
			Collection<? extends InferenceVariable> variables) {
		Map<InferenceVariable, Type> instantiations = new HashMap<>();

		Set<InferenceVariable> remainingVariables = new HashSet<>(variables);
		do {
			RemainingDependencies remainingDependencies = recalculateRemainingDependencies();

			/*
			 * Given a set of inference variables to resolve, let V be the union of
			 * this set and all variables upon which the resolution of at least one
			 * variable in this set depends.
			 */
			resolveIndependentSet(
					variables
							.stream()
							.filter(
									v -> !bounds.getBoundsOn(v).getInstantiation().isPresent())
							.map(remainingDependencies::get).flatMap(Set::stream)
							.collect(Collectors.toSet()), remainingDependencies);

			for (InferenceVariable variable : new HashSet<>(remainingVariables)) {
				Optional<Type> instantiation = bounds.getBoundsOn(variable)
						.getInstantiation();
				if (instantiation.isPresent()) {
					instantiations.put(variable, instantiation.get());
					remainingVariables.remove(variable);
				}
			}
		} while (!remainingVariables.isEmpty());

		return instantiations;
	}

	private void resolveIndependentSet(Set<InferenceVariable> variables,
			RemainingDependencies remainingDependencies) {
		/*
		 * If every variable in V has an instantiation, then resolution succeeds and
		 * this procedure terminates.
		 */
		while (variables != null && !variables.isEmpty()) {
			/*
			 * Otherwise, let { α1, ..., αn } be a non-empty subset of uninstantiated
			 * variables in V such that i) for all i (1 ≤ i ≤ n), if αi depends on the
			 * resolution of a variable β, then either β has an instantiation or there
			 * is some j such that β = αj; and ii) there exists no non-empty proper
			 * subset of { α1, ..., αn } with this property. Resolution proceeds by
			 * generating an instantiation for each of α1, ..., αn based on the bounds
			 * in the bound set:
			 */
			Set<InferenceVariable> minimalSet = new HashSet<>(variables);
			int minimalSetSize = variables.size();
			for (InferenceVariable variable : variables)
				if (remainingDependencies.get(variable).size() < minimalSetSize)
					minimalSetSize = (minimalSet = remainingDependencies.get(variable))
							.size();

			resolveMinimalIndepdendentSet(minimalSet);

			variables.removeAll(bounds.getInstantiatedVariables());
			if (!variables.isEmpty()) {
				remainingDependencies = recalculateRemainingDependencies();
				variables.removeAll(bounds.getInstantiatedVariables());
			}
		}
	}

	public void resolveTypeHierarchy(Type subtype, Class<?> superclass) {
		Class<?> subclass = Types.getRawType(subtype);

		if (!superclass.isAssignableFrom(subclass))
			throw new IllegalArgumentException("Type '" + subtype
					+ "' is not a valid subtype of '" + superclass + "'.");

		Class<?> finalSubclass2 = subclass;
		Function<Type, Type> inferenceVariables = t -> {
			if (t instanceof TypeVariable)
				return getInferenceVariable(finalSubclass2, (TypeVariable<?>) t);
			else
				return null;
		};
		while (!subclass.equals(superclass)) {
			Set<Type> lesserSubtypes = new HashSet<>(Arrays.asList(subclass
					.getGenericInterfaces()));
			if (subclass.getSuperclass() != null)
				lesserSubtypes.addAll(Arrays.asList(subclass.getGenericSuperclass()));

			subtype = lesserSubtypes.stream()
					.filter(t -> superclass.isAssignableFrom(Types.getRawType(t)))
					.findAny().get();
			subtype = new TypeSubstitution(inferenceVariables).resolve(subtype);

			incorporateType(subtype);
			subclass = Types.getRawType(subtype);

			Class<?> finalSubclass = subclass;
			inferenceVariables = t -> {
				if (t instanceof TypeVariable)
					return getInferenceVariable(finalSubclass, (TypeVariable<?>) t);
				else
					return null;
			};
		}
	}

	private void resolveMinimalIndepdendentSet(Set<InferenceVariable> minimalSet) {
		Set<CaptureConversion> relatedCaptureConversions = new HashSet<>();
		bounds.getCaptureConversions().forEach(c -> {
			if (c.getInferenceVariables().stream().anyMatch(minimalSet::contains))
				relatedCaptureConversions.add(c);
		});

		if (relatedCaptureConversions.isEmpty()) {
			/*
			 * If the bound set does not contain a bound of the form G<..., αi, ...> =
			 * capture(G<...>) for all i (1 ≤ i ≤ n), then a candidate instantiation
			 * Ti is defined for each αi:
			 */
			BoundSet bounds = new BoundSet(this.bounds);
			Map<InferenceVariable, Type> instantiationCandidates = new HashMap<>();

			try {
				for (InferenceVariable variable : minimalSet) {
					IdentityProperty<Boolean> hasThrowableBounds = new IdentityProperty<>(
							false);

					Type instantiationCandidate;
					if (!bounds.getBoundsOn(variable).getProperLowerBounds().isEmpty()) {
						/*
						 * If αi has one or more proper lower bounds, L1, ..., Lk, then Ti =
						 * lub(L1, ..., Lk) (§4.10.4).
						 */
						instantiationCandidate = IntersectionType.from(Types
								.leastUpperBound(bounds.getBoundsOn(variable)
										.getProperLowerBounds()));
					} else if (hasThrowableBounds.get()) {
						/*
						 * Otherwise, if the bound set contains throws αi, and the proper
						 * upper bounds of αi are, at most, Exception, Throwable, and
						 * Object, then Ti = RuntimeException.
						 */
						throw new AssertionError();
					} else {
						/*
						 * Otherwise, where αi has proper upper bounds U1, ..., Uk, Ti =
						 * glb(U1, ..., Uk) (§5.1.10).
						 */
						instantiationCandidate = Types.greatestLowerBound(bounds
								.getBoundsOn(variable).getProperUpperBounds());
					}

					instantiationCandidates.put(variable, instantiationCandidate);
					bounds.incorporate().equality(variable, instantiationCandidate);
				}
			} catch (TypeInferenceException e) {
				instantiationCandidates = null;
			}

			if (instantiationCandidates != null) {
				this.bounds = bounds;

				for (Map.Entry<InferenceVariable, Type> instantiation : instantiationCandidates
						.entrySet())
					instantiate(instantiation.getKey(), instantiation.getValue());

				return;
			}
		}

		/*
		 * the bound set contains a bound of the form G<..., αi, ...> =
		 * capture(G<...>) for some i (1 ≤ i ≤ n), or;
		 * 
		 * If the bound set produced in the step above contains the bound false;
		 * 
		 * then let Y1, ..., Yn be fresh type variables whose bounds are as follows:
		 */
		Map<InferenceVariable, TypeVariableCapture> freshVariables = TypeVariableCapture
				.captureInferenceVariables(minimalSet, this);

		/*
		 * Otherwise, for all i (1 ≤ i ≤ n), all bounds of the form G<..., αi, ...>
		 * = capture(G<...>) are removed from the current bound set, and the bounds
		 * α1 = Y1, ..., αn = Yn are incorporated.
		 * 
		 * If the result does not contain the bound false, then the result becomes
		 * the new bound set, and resolution proceeds by selecting a new set of
		 * variables to instantiate (if necessary), as described above.
		 * 
		 * Otherwise, the result contains the bound false, and resolution fails.
		 */
		bounds.removeCaptureConversions(relatedCaptureConversions);
		for (Map.Entry<InferenceVariable, TypeVariableCapture> inferenceVariable : freshVariables
				.entrySet())
			instantiate(inferenceVariable.getKey(), inferenceVariable.getValue());
	}

	private void instantiate(InferenceVariable variable, Type instantiation) {
		bounds.incorporate().equality(variable, instantiation);
	}

	public Type resolveType(Type type) {
		return new TypeSubstitution(t -> {
			if (t instanceof InferenceVariable)
				return resolveInferenceVariable((InferenceVariable) t);
			else if (t instanceof TypeVariableCapture)
				return t;
			else if (t instanceof TypeVariable)
				return resolveTypeVariable((TypeVariable<?>) t);
			else
				return null;
		}).resolve(type);
	}

	public Type resolveType(GenericDeclaration declaration, Type type) {
		return new TypeSubstitution(t -> {
			if (t instanceof InferenceVariable)
				return resolveInferenceVariable((InferenceVariable) t);
			else if (t instanceof TypeVariableCapture)
				return t;
			else if (t instanceof TypeVariable)
				return resolveTypeVariable(declaration, (TypeVariable<?>) t);
			else
				return null;
		}).resolve(type);
	}

	public Type resolveTypeVariable(TypeVariable<?> typeVariable) {
		return resolveTypeVariable(typeVariable.getGenericDeclaration(),
				typeVariable);
	}

	public Type resolveTypeVariable(GenericDeclaration declaration,
			TypeVariable<?> typeVariable) {
		incorporateTypeParameters(typeVariable.getGenericDeclaration());

		return resolveInferenceVariable(capturedTypeVariables.get(declaration).get(
				typeVariable));
	}

	public boolean validate() {
		return validate(getInferenceVariables());
	}

	public boolean validate(InferenceVariable... variables) {
		return validate(Arrays.asList(variables));
	}

	public boolean validate(Collection<? extends InferenceVariable> variable) {
		return infer(variable).values().stream().allMatch(v -> v != null);
	}

	private Type resolveInferenceVariable(InferenceVariable variable) {
		if (bounds.getInferenceVariables().contains(variable))
			return bounds.getBoundsOn(variable).getInstantiation().orElse(variable);
		else
			return variable;
	}

	public Set<InferenceVariable> getInferenceVariables() {
		return bounds.getInferenceVariables();
	}

	public Map<TypeVariable<?>, InferenceVariable> getInferenceVariables(
			GenericDeclaration declaration) {
		incorporateTypeParameters(declaration);
		return new HashMap<>(capturedTypeVariables.get(declaration));
	}

	public InferenceVariable getInferenceVariable(TypeVariable<?> typeVariable) {
		return getInferenceVariable(typeVariable.getGenericDeclaration(),
				typeVariable);
	}

	public InferenceVariable getInferenceVariable(GenericDeclaration declaration,
			TypeVariable<?> typeVariable) {
		incorporateTypeParameters(declaration);
		return capturedTypeVariables.get(declaration).get(typeVariable);
	}

	public static void main(String... args) {
		test();
	}

	static class TT<TTT> extends TypeToken<TTT> {}

	static class Y<YT> extends TT<Set<YT>> {}

	static class G extends Y<List<String>> {}

	public static class Outer<T> {
		public class Inner<N extends T, J extends Collection<? extends T>, P> {}

		public class Inner2<M extends Number & Comparable<?>> extends
				Outer<Comparable<?>>.Inner<M, List<Integer>, T> {}
	}

	public static class Outer2<F, Z extends F> {
		public class Inner3<X extends Set<F>> extends Outer<F>.Inner<Z, X, Set<Z>> {
			Inner3() {
				new Outer<F>() {}.super();
			}
		}
	}

	public static <T> TypeToken<List<T>> listOf(Class<T> sub) {
		return new TypeToken<List<T>>() {}.withTypeArgument(
				new TypeParameter<T>() {}, sub);
	}

	public static void test() {
		System.out.println(new TypeToken<SchemaNode.Effective<?, ?>>() {}
				.resolveSupertypeParameters(SchemaNode.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<HashSet<String>>() {}
				.resolveSupertypeParameters(Set.class));
		System.out.println();
		System.out.println();

		System.out.println("List with T = String: " + listOf(String.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Collection<? super String>>() {}
				.resolveSubtypeParameters(HashSet.class));
		System.out.println();
		System.out.println();

		new TypeToken<Outer<Serializable>.Inner<String, HashSet<Serializable>, Set<String>>>() {}
				.getResolver();
		System.out.println();
		System.out.println();

		System.out
				.println(new TypeToken<Outer<Serializable>.Inner<String, HashSet<Serializable>, Set<String>>>() {}
						.resolveSubtypeParameters(Outer2.Inner3.class));
		System.out.println();
		System.out.println();

		System.out
				.println(new TypeToken<Outer2<Serializable, String>.Inner3<HashSet<Serializable>>>() {}
						.resolveSupertypeParameters(Outer.Inner.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Outer<String>.Inner2<Double>>() {}
				.resolveSupertypeParameters(Outer.Inner.class));
		System.out.println();
		System.out.println();

		System.out.println("type test: "
				+ new TypeToken<String>() {}
						.resolveSupertypeParameters(Comparable.class));
		System.out.println();
		System.out.println();

		class SM<YO> {}
		class NM<V extends Number> extends SM<V> {}
		System.out.println(new TypeToken<NM<?>>() {});
		System.out.println(new TypeToken<NM<?>>() {}
				.resolveSupertypeParameters(SM.class));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.of(new TypeToken<Nest<?>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.of(new TypeToken<Nest22<?>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.of(new TypeToken<Nest2<?>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.of(new TypeToken<Base<LeftN, RightN>>() {}
				.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken.of(new TypeToken<RightN>() {}
				.resolveSupertypeParameters(Base.class).getType()));
		System.out.println();
		System.out.println();

		System.out.println("TYPELITTEST: " + new TT<String>() {});
		System.out.println("TYPELITTEST-2: " + new Y<String>() {});
		System.out.println("TYPELITTEST-3: " + new G() {});
		System.out.println("TYPELITTEST-4: "
				+ new Y<Integer>() {}.resolveSupertypeParameters(Collection.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Self<?>>() {}
				.isAssignableFrom(new TypeToken<Nest<?>>() {}));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken
				.of(new TypeToken<Nest2<? extends Nest2<?>>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeToken
				.of(new TypeToken<Nest2<? extends Nest22<?>>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<SchemaNode.Effective<?, ?>>() {}
				.resolveSupertypeParameters(SchemaNode.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<Gurn<Integer>>() {}.getMethods()
				.iterator().next().infer());
		System.out.println();
		System.out.println();

		TypeToken<?> receiver = new TypeToken<BindingState>() {};
		System.out.println("RESOLVE 1:");
		System.out
				.println(receiver.resolveMethodOverload("bindingNode", int.class));
		System.out.println();
		System.out.println();

		receiver = new TypeToken<SchemaNodeConfigurator<?, ?>>() {};
		System.out.println("RESOLVE 2:");
		System.out.println(TypeToken.of(receiver.getType()).resolveMethodOverload(
				"name", Arrays.asList(String.class)));
		System.out.println();
		System.out.println();

		receiver = new TypeToken<ChildNodeConfigurator<?, ?>>() {};
		System.out.println("RESOLVE 3:");
		System.out.println(TypeToken.of(receiver.getType()).resolveMethodOverload(
				"name", Arrays.asList(String.class)));
		System.out.println();
		System.out.println();

		receiver = new TypeToken<DataBindingType.Effective<?>>() {};
		System.out.println("RESOLVE 4:");
		System.out.println(TypeToken.of(receiver.getType()).resolveMethodOverload(
				"child", Arrays.asList(String.class)));
		System.out.println();
		System.out.println();

		System.out.println(new TypeToken<IncludeTarget>() {}.resolveMethodOverload(
				"includer", Model.class, Collection.class));
		System.out.println();
		System.out.println();
	}
}

interface IncludeTarget {
	<T> void include(Model<T> model, T object);

	<T> void include(Model<T> model, Collection<? extends T> objects);

	void includer(Model<?> model, Object object);

	void includer(Model<?> model, Collection<?> objects);
}

interface Model<T> {}

interface BindingState {
	SchemaNode.Effective<?, ?> bindingNode(int parent);
}

interface SchemaNode<S extends SchemaNode<S, E>, E extends SchemaNode.Effective<S, E>> {
	interface Effective<S extends SchemaNode<S, E>, E extends Effective<S, E>>
			extends SchemaNode<S, E> {}

	ChildNode<?, ?> child(String name);
}

interface ChildNode<S extends ChildNode<S, E>, E extends ChildNode.Effective<S, E>>
		extends SchemaNode<S, E> {
	interface Effective<S extends ChildNode<S, E>, E extends Effective<S, E>>
			extends ChildNode<S, E>, SchemaNode.Effective<S, E> {}
}

interface SchemaNodeConfigurator<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<N, ?>> {
	public S name(String name);
}

interface ChildNodeConfigurator<S extends ChildNodeConfigurator<S, N>, N extends ChildNode<N, ?>>
		extends SchemaNodeConfigurator<S, N> {}

interface DataBindingType<T> extends
		BindingNode<T, DataBindingType<T>, DataBindingType.Effective<T>> {
	interface Effective<T> extends DataBindingType<T>,
			BindingNode.Effective<T, DataBindingType<T>, Effective<T>> {}
}

interface BindingNode<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		extends SchemaNode<S, E> {
	interface Effective<T, S extends BindingNode<T, S, E>, E extends Effective<T, S, E>>
			extends BindingNode<T, S, E>, SchemaNode.Effective<S, E> {}
}

class Nest<T extends Set<Nest<T>>> implements Self<Nest<T>> {
	@Override
	public Nest<T> copy() {
		return null;
	}
}

interface Blurn<T> {
	Set<T> blurn();
}

interface Gurn<X> extends Blurn<List<X>> {}

class Nest2<T extends Nest2<T>> {}

class Nest22<T> extends Nest2<Nest22<T>> {}

class Base<T extends Base<U, T>, U extends Base<T, U>> {}

class LeftN extends Base<RightN, LeftN> {}

class RightN extends Base<LeftN, RightN> {}
