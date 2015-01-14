package uk.co.strangeskies.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.BoundVisitor.PartialBoundVisitor;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.collection.computingmap.CacheComputingMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

public class Resolver {
	private BoundSet bounds;

	/*
	 * We maintain a set of generic declarations which have already been
	 * incorporated into the resolver such that inference variables have been
	 * captured over the type variables where appropriate - and in the case of
	 * Classes, such that bounds on inference variables may be implied for other
	 * classes through enclosing, subtype, and supertype relations.
	 */
	private final Set<GenericDeclaration> captures;
	private final Map<TypeVariable<?>, InferenceVariable<?>> inferenceVariables;
	private final MultiMap<InferenceVariable<?>, InferenceVariable<?>, ? extends Set<InferenceVariable<?>>> remainingDependencies;
	private final Map<InferenceVariable<?>, Type> instantiations;

	/*
	 * A ComputingMap doesn't really make much sense here over a regular Map...
	 * But it doesn't hurt anything, and it gives us soft-references for values
	 * out of the box.
	 */
	private static final CacheComputingMap<Set<ParameterizedType>, IdentityProperty<ParameterizedType>> BESTS = new CacheComputingMap<>(
			c -> new IdentityProperty<>(), true);

	public Resolver(BoundSet bounds) {
		this.bounds = bounds;

		captures = new HashSet<>();
		inferenceVariables = new HashMap<>();
		remainingDependencies = new MultiHashMap<>(HashSet::new);
		instantiations = new HashMap<>();
	}

	public Resolver() {
		this(new BoundSet());
	}

	public Resolver(Resolver that) {
		bounds = new BoundSet(that.bounds);

		captures = new HashSet<>(that.captures);
		inferenceVariables = new HashMap<>(that.inferenceVariables);
		remainingDependencies = new MultiHashMap<>(HashSet::new,
				that.remainingDependencies);
		instantiations = new HashMap<>(that.instantiations);
	}

	public BoundSet getBounds() {
		return bounds;
	}

	private void addRemainingDependency(InferenceVariable<?> variable,
			InferenceVariable<?> dependency) {
		/*
		 * An inference variable α depends on the resolution of an inference
		 * variable β if there exists an inference variable γ such that α depends on
		 * the resolution of γ and γ depends on the resolution of β.
		 */
		if (remainingDependencies.add(variable, dependency)) {
			for (InferenceVariable<?> transientDependency : remainingDependencies
					.get(dependency))
				addRemainingDependency(variable, transientDependency);

			remainingDependencies
					.entrySet()
					.stream()
					.filter(e -> e.getValue().contains(variable))
					.map(Entry::getKey)
					.forEach(
							transientDependent -> addRemainingDependency(transientDependent,
									variable));
		}
	}

	private void recalculateRemainingDependencies() {
		Set<InferenceVariable<?>> leftOfCapture = new HashSet<>();

		Set<InferenceVariable<?>> inferenceVariables = new HashSet<>(
				this.inferenceVariables.values());
		inferenceVariables.removeAll(instantiations.keySet());

		/*
		 * An inference variable α depends on the resolution of itself.
		 */
		for (InferenceVariable<?> inferenceVariable : inferenceVariables)
			addRemainingDependency(inferenceVariable, inferenceVariable);

		/*
		 * An inference variable α appearing on the left-hand side of a bound of the
		 * form G<..., α, ...> = capture(G<...>) depends on the resolution of every
		 * other inference variable mentioned in this bound (on both sides of the =
		 * sign).
		 */
		bounds.stream().forEach(b -> b.accept(new PartialBoundVisitor() {
			@Override
			public void acceptCaptureConversion(Map<Type, InferenceVariable<?>> c) {
				for (InferenceVariable<?> variable : c.values()) {
					if (inferenceVariables.contains(variable)) {
						for (InferenceVariable<?> dependency : c.values())
							if (inferenceVariables.contains(dependency))
								addRemainingDependency(variable, dependency);
						for (Type inC : c.keySet())
							for (InferenceVariable<?> dependency : InferenceVariable
									.getAllMentionedBy(inC))
								if (inferenceVariables.contains(dependency))
									addRemainingDependency(variable, dependency);
					}
				}

				leftOfCapture.addAll(c.values());
			}
		}));

		/*
		 * Given a bound of one of the following forms, where T is either an
		 * inference variable β or a type that mentions β:
		 */
		bounds.stream().forEach(b -> b.accept(new PartialBoundVisitor() {
			/*
			 * α = T, T = α
			 */
			@Override
			public void acceptEquality(InferenceVariable<?> a, InferenceVariable<?> b) {
				assessDependency(a, b);
				assessDependency(b, a);
			}

			@Override
			public void acceptEquality(InferenceVariable<?> a, Type b) {
				for (InferenceVariable<?> inB : InferenceVariable.getAllMentionedBy(b))
					assessDependency(a, inB);
			}

			/*
			 * α <: T, T <: α
			 */
			@Override
			public void acceptSubtype(InferenceVariable<?> a, InferenceVariable<?> b) {
				assessDependency(a, b);
				assessDependency(b, a);
			}

			@Override
			public void acceptSubtype(InferenceVariable<?> a, Type b) {
				for (InferenceVariable<?> inB : InferenceVariable.getAllMentionedBy(b))
					assessDependency(a, inB);
			}

			@Override
			public void acceptSubtype(Type a, InferenceVariable<?> b) {
				for (InferenceVariable<?> inA : InferenceVariable.getAllMentionedBy(a))
					assessDependency(inA, b);
			}

			/*
			 * If α appears on the left-hand side of another bound of the form G<...,
			 * α, ...> = capture(G<...>), then β depends on the resolution of α.
			 * Otherwise, α depends on the resolution of β.
			 */
			public void assessDependency(InferenceVariable<?> a,
					InferenceVariable<?>... b) {
				if (leftOfCapture.contains(a)) {
					for (InferenceVariable<?> bItem : b)
						if (inferenceVariables.contains(bItem))
							addRemainingDependency(bItem, a);
				} else {
					for (InferenceVariable<?> bItem : b)
						if (inferenceVariables.contains(bItem))
							addRemainingDependency(a, bItem);
				}
			}
		}));
	}

	public void capture(GenericDeclaration declaration) {
		if (captures.add(declaration)) {
			List<? extends InferenceVariable<?>> newInferenceVariables = InferenceVariable
					.overGenericTypeContext(this, declaration);

			for (InferenceVariable<?> inferenceVariable : newInferenceVariables)
				if (!inferenceVariables
						.containsKey(inferenceVariable.getTypeVariable())) {
					inferenceVariables.put(inferenceVariable.getTypeVariable(),
							inferenceVariable);

					boolean anyProper = false;
					for (Type bound : inferenceVariable.getBounds()) {
						anyProper = anyProper || Types.isProperType(bound);
						bounds.incorporate().acceptSubtype(inferenceVariable, bound);
					}
					if (!anyProper)
						bounds.incorporate().acceptSubtype(inferenceVariable, Object.class);
				}

			recalculateRemainingDependencies();
		}
	}

	public boolean isCaptured(GenericDeclaration declaration) {
		return captures.contains(declaration);
	}

	public Type substituteCaptures(Type type) {
		TypeSubstitution substitution = new TypeSubstitution();
		for (InferenceVariable<?> variable : getInferenceVariables())
			substitution = substitution.where(variable.getTypeVariable(), variable);

		return substitution.resolve(type);
	}

	public void incorporateTypes(Type... types) {
		new TypeVisitor() {
			@Override
			protected void visitClass(Class<?> type) {
				capture(type);
			}

			@Override
			protected void visitParameterizedType(ParameterizedType type) {
				incorporateParameterizedType(type);
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
			protected void visitTypeVariable(TypeVariable<?> type) {
				capture(type.getGenericDeclaration());
			}

			@Override
			protected void visitWildcardType(WildcardType type) {
				visit(type.getLowerBounds());
				visit(type.getUpperBounds());
			}
		}.visit(types);
	}

	public void incorporateParameterizedType(ParameterizedType type) {
		capture(Types.getRawType(type));

		TypeSubstitution substitution = new TypeSubstitution();
		for (InferenceVariable<?> variable : inferenceVariables.values())
			substitution = substitution.where(variable.getTypeVariable(), variable);

		for (int i = 0; i < type.getActualTypeArguments().length; i++) {
			TypeVariable<?> typeParameter = ((Class<?>) type.getRawType())
					.getTypeParameters()[i];
			Type actualTypeArgument = type.getActualTypeArguments()[i];
			actualTypeArgument = substitution.resolve(actualTypeArgument);

			incorporateConstraint(new ConstraintFormula(Kind.EQUALITY,
					inferenceVariables.get(typeParameter), actualTypeArgument));
		}

		recalculateRemainingDependencies();
	}

	public void incorporateConstraint(ConstraintFormula constraintFormula) {
		bounds.incorporate(constraintFormula);
	}

	public void incorporateInstantiation(TypeVariable<?> variable,
			Type instantiation) {
		capture(variable.getGenericDeclaration());
		bounds.incorporate(new ConstraintFormula(Kind.EQUALITY,
				getInferenceVariable(variable), instantiation));
	}

	public Map<InferenceVariable<?>, Type> infer(GenericDeclaration context) {
		return infer(getInferenceVariables(context));
	}

	public Map<InferenceVariable<?>, Type> infer() {
		return infer(inferenceVariables.values());
	}

	public Map<InferenceVariable<?>, Type> infer(
			InferenceVariable<?>... variables) {
		return infer(Arrays.asList(variables));
	}

	public Map<InferenceVariable<?>, Type> infer(
			Collection<? extends InferenceVariable<?>> variables) {
		/*
		 * Given a set of inference variables to resolve, let V be the union of this
		 * set and all variables upon which the resolution of at least one variable
		 * in this set depends.
		 */
		resolveIndependentSet(variables.stream()
				.filter(v -> !instantiations.containsKey(v))
				.map(remainingDependencies::get).flatMap(Set::stream)
				.collect(Collectors.toSet()));

		Map<InferenceVariable<?>, Type> instantiations = new HashMap<>();

		for (InferenceVariable<?> variable : variables)
			instantiations.put(variable, this.instantiations.get(variable));

		return instantiations;
	}

	private void resolveIndependentSet(Set<InferenceVariable<?>> variables) {
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
			Set<InferenceVariable<?>> minimalSet = new HashSet<>(variables);
			int minimalSetSize = variables.size();
			for (InferenceVariable<?> variable : variables)
				if (remainingDependencies.get(variable).size() < minimalSetSize)
					minimalSetSize = (minimalSet = remainingDependencies.get(variable))
							.size();

			resolveMinimalIndepdendentSet(minimalSet);

			variables.removeAll(minimalSet);
		}
	}

	private void resolveMinimalIndepdendentSet(
			Set<InferenceVariable<?>> minimalSet) {
		IdentityProperty<Boolean> containsCaptureConversion = new IdentityProperty<Boolean>(
				false);
		bounds.stream().forEach(b -> b.accept(new PartialBoundVisitor() {
			@Override
			public void acceptCaptureConversion(Map<Type, InferenceVariable<?>> c) {
				if (c.values().stream().anyMatch(minimalSet::contains))
					containsCaptureConversion.set(true);
			};
		}));

		if (!containsCaptureConversion.get()) {
			/*
			 * If the bound set does not contain a bound of the form G<..., αi, ...> =
			 * capture(G<...>) for all i (1 ≤ i ≤ n), then a candidate instantiation
			 * Ti is defined for each αi:
			 */
			BoundSet bounds = new BoundSet(this.bounds);
			Map<InferenceVariable<?>, Type> instantiationCandidates = new HashMap<>();

			try {
				for (InferenceVariable<?> variable : minimalSet) {
					Set<Type> lowerBounds = new HashSet<>();
					Set<Type> upperBounds = new HashSet<>();
					IdentityProperty<Boolean> hasThrowableBounds = new IdentityProperty<>(
							false);
					bounds.stream().forEach(b -> b.accept(new PartialBoundVisitor() {
						@Override
						public void acceptSubtype(Type a, InferenceVariable<?> b) {
							if (b.equals(variable) && Types.isProperType(a))
								lowerBounds.add(a);
						};

						@Override
						public void acceptSubtype(InferenceVariable<?> a, Type b) {
							if (a.equals(variable) && Types.isProperType(b))
								upperBounds.add(b);
						};

						@Override
						public void acceptEquality(InferenceVariable<?> a, Type b) {
							if (a.equals(variable) && Types.isProperType(b)) {
								upperBounds.add(b);
								lowerBounds.add(b);
							}
						};
					}));
					Type instantiationCandidate;
					if (!lowerBounds.isEmpty()) {
						/*
						 * If αi has one or more proper lower bounds, L1, ..., Lk, then Ti =
						 * lub(L1, ..., Lk) (§4.10.4).
						 */
						instantiationCandidate = leastUpperBound(lowerBounds);
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
						instantiationCandidate = greatestLowerBound(upperBounds);
					}
					instantiationCandidates.put(variable, instantiationCandidate);
					bounds.incorporate().acceptEquality(variable, instantiationCandidate);
				}
			} catch (TypeInferenceException e) {
				e.printStackTrace();
				instantiationCandidates = null;
			}

			if (instantiationCandidates != null) {
				for (Map.Entry<InferenceVariable<?>, Type> instantiation : instantiationCandidates
						.entrySet()) {
					this.instantiations.put(instantiation.getKey(),
							instantiation.getValue());
				}
				remainingDependencies.keySet().removeAll(instantiations.keySet());
				remainingDependencies.removeAllFromAll(instantiations.keySet());

				this.bounds = bounds;
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
		for (InferenceVariable<?> variable : minimalSet)
			instantiations.put(variable, null);
	}

	public static Type leastUpperBound(Type... upperBounds) {
		return leastUpperBound(Arrays.asList(upperBounds));
	}

	public static Type leastUpperBound(Collection<Type> upperBounds) {
		Type upperBound = leastUpperBoundImpl(upperBounds);

		/*
		 * Not sure if this is necessary! But it's cheap enough to check. Can't
		 * validate IntersectionTypes and ParameterizedTypes as we create them, as
		 * they may contain uninitialised proxies in place of ParameterizedTypes.
		 */
		Types.validate(upperBound);

		return upperBound;
	}

	private static Type leastUpperBoundImpl(Collection<Type> upperBounds) {
		if (upperBounds.size() == 1)
			/*
			 * If k = 1, then the lub is the type itself: lub(U) = U.
			 */
			return upperBounds.iterator().next();
		else {
			/*
			 * For each Ui (1 ≤ i ≤ k):
			 */
			Iterator<Type> lowerBoundsIterator = upperBounds.iterator();
			MultiMap<Class<?>, ParameterizedType, ? extends Set<ParameterizedType>> erasedCandidates = new MultiHashMap<>(
					HashSet::new);
			erasedCandidates.addAll(getErasedSupertypes(lowerBoundsIterator.next()));

			while (lowerBoundsIterator.hasNext()) {
				Type t = lowerBoundsIterator.next();
				Map<Class<?>, ParameterizedType> erasedSupertypes = getErasedSupertypes(t);
				erasedCandidates.keySet().retainAll(erasedSupertypes.keySet());
				for (Map.Entry<Class<?>, ParameterizedType> erasedSupertype : erasedSupertypes
						.entrySet())
					if (erasedCandidates.containsKey(erasedSupertype.getKey())
							&& erasedSupertype.getValue() != null)
						erasedCandidates.add(erasedSupertype.getKey(),
								erasedSupertype.getValue());
			}

			minimiseCandidates(erasedCandidates);

			List<Type> bestTypes = erasedCandidates
					.entrySet()
					.stream()
					.map(
							e -> best(e.getKey(),
									new ArrayList<ParameterizedType>(e.getValue())))
					.collect(Collectors.toList());

			return IntersectionType.uncheckedOf(bestTypes);
		}
	}

	private static void minimiseCandidates(
			MultiMap<Class<?>, ParameterizedType, ? extends Set<ParameterizedType>> erasedCandidates) {
		List<Class<?>> minimalCandidates = new ArrayList<>(
				erasedCandidates.keySet());
		if (minimalCandidates.size() > 1)
			for (int i = 0; i < minimalCandidates.size(); i++)
				for (int j = i + 1; j < minimalCandidates.size(); j++) {
					if (minimalCandidates.get(i).isAssignableFrom(
							minimalCandidates.get(j))) {
						minimalCandidates.remove(i);
						j = i;
					} else if (minimalCandidates.get(j).isAssignableFrom(
							minimalCandidates.get(i))) {
						minimalCandidates.remove(j--);
					}
				}
		erasedCandidates.keySet().retainAll(minimalCandidates);
	}

	private static Type best(Class<?> rawClass,
			List<ParameterizedType> parameterizations) {
		if (parameterizations.isEmpty())
			return rawClass;
		else if (parameterizations.size() == 1) {
			Type parameterization = parameterizations.iterator().next();
			return parameterization == null ? rawClass : parameterization;
		}

		/*
		 * Proxy guard against recursive generation of infinite types
		 */
		IdentityProperty<ParameterizedType> bestResult;
		synchronized (BESTS) {
			if (BESTS.keySet().contains(new HashSet<>(parameterizations)))
				return BESTS.get(new HashSet<>(parameterizations)).get();

			bestResult = new IdentityProperty<>();
			BESTS.putGet(new HashSet<>(parameterizations)).set(
					Types.parameterizedTypeProxy(bestResult));
		}

		Map<TypeVariable<?>, Type> leastContainingParameterization = new HashMap<>();

		Class<?> enclosingClass = rawClass;
		do {
			for (int i = 0; i < parameterizations.size(); i++) {
				ParameterizedType parameterization = parameterizations.get(i);
				for (int j = 0; j < enclosingClass.getTypeParameters().length; j++) {
					TypeVariable<?> variable = enclosingClass.getTypeParameters()[j];
					if (parameterization != null) {
						Type argumentU = parameterization.getActualTypeArguments()[j];
						Type argumentV = leastContainingParameterization.get(variable);

						if (argumentV == null)
							leastContainingParameterization.put(variable, argumentU);
						else {
							leastContainingParameterization.put(variable,
									leastContainingArgument(argumentU, argumentV));
						}
					}
				}
				parameterizations.set(i,
						(ParameterizedType) parameterization.getOwnerType());
			}
		} while ((enclosingClass = enclosingClass.getEnclosingClass()) != null);

		ParameterizedType best = (ParameterizedType) Types
				.uncheckedParameterizedType(rawClass, leastContainingParameterization);

		bestResult.set(best);

		return best;
	}

	private static Type leastContainingArgument(Type argumentU, Type argumentV) {
		if (argumentU instanceof WildcardType
				&& (!(argumentV instanceof WildcardType) || ((WildcardType) argumentV)
						.getUpperBounds().length > 0)) {
			Type swap = argumentU;
			argumentU = argumentV;
			argumentV = swap;
		}

		if (argumentU instanceof WildcardType) {
			if (((WildcardType) argumentU).getUpperBounds().length > 0) {
				if (((WildcardType) argumentV).getUpperBounds().length > 0) {
					/*
					 * lcta(? extends U, ? extends V) = ? extends lub(U, V)
					 */
					return Types.upperBoundedWildcard(leastUpperBoundImpl(Arrays.asList(
							argumentU, argumentV)));
				} else {
					/*
					 * lcta(? extends U, ? super V) = U if U = V, otherwise ?
					 */
					return argumentU.equals(argumentV) ? argumentU : Types
							.unboundedWildcard();
				}
			} else {
				/*
				 * lcta(? super U, ? super V) = ? super glb(U, V)
				 */
				return Types.lowerBoundedWildcard(greatestLowerBound(argumentU,
						argumentV));
			}
		} else if (argumentV instanceof WildcardType) {
			if (((WildcardType) argumentV).getUpperBounds().length > 0) {
				/*
				 * lcta(U, ? extends V) = ? extends lub(U, V)
				 */
				return Types.upperBoundedWildcard(leastUpperBoundImpl(Arrays.asList(
						argumentU, argumentV)));
			} else {
				/*
				 * lcta(U, ? super V) = ? super glb(U, V)
				 */
				return Types.lowerBoundedWildcard(greatestLowerBound(argumentU,
						argumentV));
			}
		} else {
			/*
			 * lcta(U, V) = U if U = V, otherwise ? extends lub(U, V)
			 */
			return argumentU.equals(argumentV) ? argumentU : Types
					.upperBoundedWildcard(leastUpperBoundImpl(Arrays.asList(argumentU,
							argumentV)));
		}
	}

	private static Map<Class<?>, ParameterizedType> getErasedSupertypes(Type of) {
		Map<Class<?>, ParameterizedType> supertypes = new HashMap<>();

		TypeLiteral<?> ofLiteral = TypeLiteral.from(of);

		RecursiveTypeVisitor
				.build()
				.visitSupertypes()
				.classVisitor(
						type -> {
							Type parameterized = ofLiteral.resolveSupertypeParameters(type)
									.getType();
							supertypes
									.put(
											type,
											(parameterized instanceof ParameterizedType) ? (ParameterizedType) parameterized
													: null);
						})
				.parameterizedTypeVisitor(
						type -> supertypes.put(Types.getRawType(type), type)).create()
				.visit(of);

		return supertypes;
	}

	public static Type greatestLowerBound(Type... lowerBounds) {
		return greatestLowerBound(Arrays.asList(lowerBounds));
	}

	public static Type greatestLowerBound(Collection<? extends Type> lowerBounds) {
		return IntersectionType.of(lowerBounds);
	}

	public Type resolveType(Type type) {
		return new TypeSubstitution(t -> {
			if (t instanceof InferenceVariable)
				return resolveInferenceVariable((InferenceVariable<?>) t);
			else if (t instanceof TypeVariable)
				return resolveTypeVariable((TypeVariable<?>) t);
			else
				return null;
		}).resolve(type);
	}

	public Type resolveTypeVariable(TypeVariable<?> typeVariable) {
		Type instantiation = instantiations.get(inferenceVariables
				.get(typeVariable));

		if (instantiation != null)
			return instantiation;
		else {
			capture(typeVariable.getGenericDeclaration());

			return resolveInferenceVariable(inferenceVariables.get(typeVariable));
		}
	}

	public boolean validate(InferenceVariable<?>... variables) {
		return validate(Arrays.asList(variables));
	}

	public boolean validate(Collection<? extends InferenceVariable<?>> variable) {
		return infer(variable).values().stream().allMatch(v -> v != null);
	}

	private Type resolveInferenceVariable(InferenceVariable<?> variable) {
		IdentityProperty<Type> instantiation = new IdentityProperty<>(
				instantiations.get(variable));

		if (instantiation.get() == null) {
			bounds.stream().forEach(b -> b.accept(new PartialBoundVisitor() {
				@Override
				public void acceptEquality(InferenceVariable<?> a, Type b) {
					if (a.equals(variable)) {
						Set<Type> mentioned = Types.getAllMentionedBy(b,
								InferenceVariable.class::isInstance);

						if (mentioned.isEmpty()
								|| (instantiation.get() == null && mentioned.stream()
										.map(InferenceVariable.class::cast)
										.allMatch(i -> i.getResolver() != Resolver.this)))
							instantiation.set(b);
					}
				}

				@Override
				public void acceptEquality(InferenceVariable<?> a,
						InferenceVariable<?> b) {
					if (instantiation.get() == null) {
						if (a.equals(variable)) {
							if (b.getResolver() != Resolver.this)
								instantiation.set(b);
						} else if (b.equals(variable) && a.getResolver() != Resolver.this)
							instantiation.set(a);
					}
				}
			}));

			if (instantiation.get() == null)
				instantiation.set(variable);
		}

		return instantiation.get();
	}

	public Set<InferenceVariable<?>> getInferenceVariables() {
		return new HashSet<>(inferenceVariables.values());
	}

	public Set<InferenceVariable<?>> getInferenceVariables(
			GenericDeclaration declaration) {
		capture(declaration);
		return Arrays.stream(declaration.getTypeParameters())
				.map(this::getInferenceVariable).collect(Collectors.toSet());
	}

	public InferenceVariable<?> getInferenceVariable(TypeVariable<?> typeVariable) {
		capture(typeVariable.getGenericDeclaration());
		return inferenceVariables.get(typeVariable);
	}

	public static void main(String... args) {
		test();
	}

	static class TT<TTT> extends TypeLiteral<TTT> {}

	static class Y<YT> extends TT<Set<YT>> {}

	static class G extends Y<List<String>> {}

	public static <T> void test() {
		System.out.println("List with T = String: "
				+ new TypeLiteral<List<T>>() {}.withTypeArgument(
						new TypeParameter<T>() {}.getType(), String.class));

		System.out.println("TYPELITTEST: " + new TypeLiteral<String>() {});
		System.out.println("TYPELITTEST-2: " + new Y<Integer>() {});
		System.out.println("TYPELITTEST-3: " + new G());

		System.out.println("type test: "
				+ new TypeLiteral<String>() {}
						.resolveSupertypeParameters(Comparable.class));
	}
}
