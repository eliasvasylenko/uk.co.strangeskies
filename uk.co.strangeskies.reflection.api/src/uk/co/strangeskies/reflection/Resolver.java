package uk.co.strangeskies.reflection;

import java.lang.reflect.Executable;
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

import uk.co.strangeskies.reflection.Bound.PartialBoundVisitor;
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
	private final Set<GenericDeclaration> incorporatedDeclarations;
	private final Map<TypeVariable<?>, InferenceVariable<?>> inferenceVariables;
	private final MultiMap<InferenceVariable<?>, InferenceVariable<?>, ? extends Set<InferenceVariable<?>>> remainingDependencies;
	private final Map<InferenceVariable<?>, Type> instantiations;

	/*
	 * A ComputingMap doesn't really make much sense here over a regular Map...
	 * But it doesn't hurt anything, and it gives us soft-references for values
	 * out of the box.
	 */
	private static final CacheComputingMap<Collection<Type>, IdentityProperty<IntersectionType>> LEAST_UPPER_BOUNDS = new CacheComputingMap<>(
			c -> new IdentityProperty<>(), true);

	public Resolver(Resolver that) {
		bounds = new BoundSet(that.bounds);

		incorporatedDeclarations = new HashSet<>(that.incorporatedDeclarations);
		inferenceVariables = new HashMap<>(that.inferenceVariables);
		remainingDependencies = new MultiHashMap<>(that.remainingDependencies);
		instantiations = new HashMap<>(that.instantiations);
	}

	public Resolver(Type... overTypes) {
		this(new BoundSet(), overTypes);
	}

	public Resolver(BoundSet bounds, Type... overTypes) {
		this.bounds = bounds;

		incorporatedDeclarations = new HashSet<>();
		inferenceVariables = new HashMap<>();
		remainingDependencies = new MultiHashMap<>(HashSet::new);
		instantiations = new HashMap<>();

		bounds.stream().forEach(b -> b.accept(new PartialBoundVisitor() {
			@Override
			public void acceptEquality(InferenceVariable<?> inferenceVariable,
					Type type) {
				instantiations.put(inferenceVariable, type);
			}
		}));

		incorporateTypes(overTypes);
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

	public void incorporateGenericDeclaration(GenericDeclaration declaration) {
		if (incorporatedDeclarations.add(declaration)) {
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

	public void incorporateTypes(Type... types) {
		new TypeVisitor() {
			@Override
			protected void visitClass(Class<?> type) {
				incorporateGenericDeclaration(type);
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
				incorporateGenericDeclaration(type.getGenericDeclaration());
			}

			@Override
			protected void visitWildcardType(WildcardType type) {
				visit(type.getLowerBounds());
				visit(type.getUpperBounds());
			}
		}.visit(types);
	}

	public void incorporateParameterizedType(ParameterizedType type) {
		incorporateGenericDeclaration(Types.getRawType(type));

		TypeSubstitution resolver = new TypeSubstitution();
		for (InferenceVariable<?> variable : inferenceVariables.values())
			resolver = resolver.where(variable.getTypeVariable(), variable);

		System.out.println("incorporating " + type);

		for (int i = 0; i < type.getActualTypeArguments().length; i++) {
			TypeVariable<?> typeParameter = ((Class<?>) type.getRawType())
					.getTypeParameters()[i];
			Type actualTypeArgument = type.getActualTypeArguments()[i];

			/*-
			if (actualTypeArgument instanceof TypeVariable) {
				System.out.println(" - " + actualTypeArgument + " @ "
						+ ((TypeVariable<?>) actualTypeArgument).getGenericDeclaration());
				// actualTypeArgument = inferenceVariables.get(context).get(
				// actualTypeArgument);
				if (inferenceVariables.get(context).containsKey(actualTypeArgument))
					actualTypeArgument = inferenceVariables.get(context).get(
							actualTypeArgument);
			} else
			 * /
			if (actualTypeArgument instanceof TypeVariable)
				System.out.println("ac: " + actualTypeArgument + " @ "
						+ ((TypeVariable<?>) actualTypeArgument).getGenericDeclaration()
						+ " ~~ " + parameterizedType);

			if (actualTypeArgument instanceof TypeVariable
					&& ((TypeVariable<?>) actualTypeArgument).getGenericDeclaration()
							.equals(parameterizedType.getRawType()))
				return;
			actualTypeArgument = resolve(context, actualTypeArgument);
			System.out.println("ac: " + actualTypeArgument);

			if (actualTypeArgument == null)
				return;
			 */
			actualTypeArgument = resolver.resolve(actualTypeArgument);

			incorporate(new ConstraintFormula(Kind.EQUALITY,
					inferenceVariables.get(typeParameter), actualTypeArgument));
		}
		System.out.println(bounds);

		recalculateRemainingDependencies();
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
					System.out.println("    eqal{{{{ " + variable + " = "
							+ instantiationCandidate);
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

	private static Type leastUpperBound(Type... lowerBounds) {
		return leastUpperBound(Arrays.asList(lowerBounds));
	}

	private static Type leastUpperBound(Collection<Type> lowerBounds) {
		if (lowerBounds.size() == 1)
			/*
			 * If k = 1, then the lub is the type itself: lub(U) = U.
			 */
			return lowerBounds.iterator().next();
		else {
			/*
			 * Proxy guard against recursive generation of infinite types
			 */
			IdentityProperty<IntersectionType> leastUpperBoundResult;

			synchronized (LEAST_UPPER_BOUNDS) {
				if (LEAST_UPPER_BOUNDS.keySet().contains(lowerBounds))
					return LEAST_UPPER_BOUNDS.get(lowerBounds).get();

				leastUpperBoundResult = new IdentityProperty<>();
				LEAST_UPPER_BOUNDS.putGet(lowerBounds).set(
						() -> leastUpperBoundResult.get().getTypes());
			}

			/*
			 * For each Ui (1 ≤ i ≤ k):
			 */
			Iterator<Type> lowerBoundsIterator = lowerBounds.iterator();
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

			leastUpperBoundResult.set(IntersectionType.of(bestTypes));

			return leastUpperBoundResult.get();
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

		Type best = Types.parameterizedType(rawClass,
				leastContainingParameterization);
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
					return Types.upperBoundedWildcard(leastUpperBound(argumentU,
							argumentV));
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
				return Types
						.upperBoundedWildcard(leastUpperBound(argumentU, argumentV));
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
					.upperBoundedWildcard(leastUpperBound(argumentU, argumentV));
		}
	}

	private static Map<Class<?>, ParameterizedType> getErasedSupertypes(Type of) {
		Map<Class<?>, ParameterizedType> supertypes = new HashMap<>();

		RecursiveTypeVisitor
				.build()
				.visitSupertypes()
				.classVisitor(
						type -> {
							Type parameterized = new Resolver(of).resolveTypeParameters(type);
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

	private static Type greatestLowerBound(Type... upperBounds) {
		return greatestLowerBound(Arrays.asList(upperBounds));
	}

	private static Type greatestLowerBound(Collection<Type> upperBounds) {
		if (upperBounds.size() == 1)
			return upperBounds.iterator().next();
		else
			return validateGreatestLowerBound(IntersectionType.of(upperBounds));
	}

	public static IntersectionType validateGreatestLowerBound(
			IntersectionType intersectionType) {
		Type[] types = intersectionType.getTypes();

		for (int i = 0; i < types.length; i++)
			for (int j = i + 1; j < types.length; j++)
				if (!Types.isAssignable(types[j], types[i])
						&& !Types.isAssignable(types[i], types[j]))
					throw new TypeInferenceException("Type '" + intersectionType
							+ "' is not a valid greater lowest bound.");
		return intersectionType;
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

	public Type resolveTypeParameters(Class<?> rawType) {
		Class<?> enclosingClass = rawType;
		Map<TypeVariable<?>, Type> typeParameters = new HashMap<>();
		do {
			for (TypeVariable<?> typeParameter : enclosingClass.getTypeParameters())
				typeParameters.put(typeParameter, null);
		} while ((enclosingClass = enclosingClass.getEnclosingClass()) != null);

		if (typeParameters.isEmpty())
			return rawType;

		if (rawType.isArray())
			return Types.genericArrayType(resolveTypeParameters(rawType
					.getComponentType()));

		/*
		 * TODO here we need to figure out which, if any, candidates in the existing
		 * set of incorporated generic declarations can be related through subtype
		 * relationships.
		 */
		Class<?> declaringClass = Types.getRawType(context.getDeclaringType());
		if (declaringClass.isAssignableFrom(rawType)) {
			incorporateTypes(Types.parameterizedType(rawType));

			Class<?> temp = declaringClass;
			declaringClass = rawType;
			rawType = temp;
		} else if (!rawType.isAssignableFrom(declaringClass))
			return Types.parameterizedType(rawType);

		Type subtype = null;
		do {
			Set<Type> supertypes = new HashSet<>();
			supertypes.addAll(Arrays.asList(declaringClass.getGenericInterfaces()));
			if (declaringClass.getGenericSuperclass() != null)
				supertypes.add(declaringClass.getGenericSuperclass());

			declaringClass = null;
			for (Type supertype : supertypes) {
				Class<?> superclass = Types.getRawType(supertype);
				if (rawType.isAssignableFrom(superclass)) {
					subtype = supertype;
					declaringClass = superclass;
					break;
				}
			}

			if (declaringClass != null && subtype instanceof ParameterizedType)
				incorporateTypes(subtype);
		} while (declaringClass != null && rawType.isAssignableFrom(declaringClass));

		// infer(typeParameters.keySet().stream()
		// .map(inferenceVariables.get(context)::get).collect(Collectors.toSet()));

		for (TypeVariable<?> typeParameter : typeParameters.keySet())
			typeParameters.put(typeParameter,
					resolveInferenceVariable(inferenceVariables.get(typeParameter)));

		return Types.parameterizedType(rawType, typeParameters);
	}

	public Type resolveTypeVariable(TypeVariable<?> typeVariable) {
		Type instantiation = instantiations.get(inferenceVariables
				.get(typeVariable));
		if (instantiation != null)
			return instantiation;

		if (typeVariable.getGenericDeclaration() instanceof Executable) {
			incorporateGenericDeclaration(typeVariable.getGenericDeclaration());
			resolveTypeParameters(((Executable) typeVariable.getGenericDeclaration())
					.getDeclaringClass());
		} else if (typeVariable.getGenericDeclaration() instanceof Class) {
			resolveTypeParameters(((Class<?>) typeVariable.getGenericDeclaration()));
		}

		return resolveInferenceVariable(inferenceVariables.get(typeVariable));
	}

	public void incorporate(ConstraintFormula constraintFormula) {
		bounds.incorporate(constraintFormula);
	}

	public void incorporateInstantiation(TypeVariable<?> variable,
			Type instantiation) {
		incorporateGenericDeclaration(variable.getGenericDeclaration());
		bounds.incorporate(new ConstraintFormula(Kind.EQUALITY,
				getInferenceVariable(variable), instantiation));
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
					instantiation.set(b);
				}

				@Override
				public void acceptEquality(InferenceVariable<?> a,
						InferenceVariable<?> b) {
					if (instantiation.get() == null) {
						if (a.equals(variable)
								&& b.getGenericDeclaration().equals(
										a.getGenericTypeContext().getGenericDeclaration()))
							instantiation.set(b);
						else if (b.equals(variable)
								&& a.getGenericDeclaration().equals(
										b.getGenericTypeContext().getGenericDeclaration()))
							instantiation.set(a);
					}
				}
			}));

			if (instantiation.get() == null && variable != null)
				instantiation.set(variable);
		}
		System.out.println("==> " + variable + " INSt= " + instantiation.get());

		return instantiation.get();
	}

	public Map<InferenceVariable<?>, Type> infer(GenericTypeContainer<?> context) {
		return infer(inferenceVariables.values());
	}

	public Map<InferenceVariable<?>, Type> infer(
			InferenceVariable<?>... variables) {
		return infer(Arrays.asList(variables));
	}

	public Set<InferenceVariable<?>> getInferenceVariables() {
		return new HashSet<>(inferenceVariables.values());
	}

	public Set<InferenceVariable<?>> getInferenceVariables(
			GenericDeclaration declaration) {
		incorporateGenericDeclaration(declaration);
		return Arrays.stream(declaration.getTypeParameters())
				.map(this::getInferenceVariable).collect(Collectors.toSet());
	}

	public InferenceVariable<?> getInferenceVariable(TypeVariable<?> typeVariable) {
		incorporateGenericDeclaration(typeVariable.getGenericDeclaration());
		return inferenceVariables.get(typeVariable);
	}

	static class A<T> {
		class B<U extends Set<T>> {

		}

		class C<V> extends A<Map<V, V>>.B<HashSet<Map<V, V>>> {}
	}

	public static void main(String... args) {
		test();
	}

	class X extends ArrayList {}

	public static <T> void test() {
		System.out
				.println("XXX  "
						+ ((TypeVariable<?>) ((ParameterizedType) new Resolver(X.class)
								.resolveTypeParameters(Iterable.class))
								.getActualTypeArguments()[0]).getGenericDeclaration());

		/*-
		stringsType = new TypeLiteral<List<String>>() {};
		resolver.incorporateEquality(stringsType,
				new TypeParameter<T>() {}.getType(), Short.class);
		System.out.println(resolver.resolve(stringsType,
				new TypeLiteral<Map<String[], List<T>[][]>>() {}.getType()));
		System.out.println();
		 */

		System.out.println(new Resolver(new TypeLiteral<ArrayList<String>>() {}
				.getType()).resolveTypeParameters(Collection.class));
		System.out.println();

		System.out
				.println(new Resolver(
						new TypeLiteral<A<Map<String, String>>.B<HashSet<Map<String, String>>>>() {}
								.getType()).resolveTypeParameters(A.C.class));
	}
}
