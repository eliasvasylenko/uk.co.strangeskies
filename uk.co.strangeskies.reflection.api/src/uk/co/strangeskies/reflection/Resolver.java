package uk.co.strangeskies.reflection;

import java.lang.reflect.Executable;
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
import java.util.TreeMap;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.Bound.PartialBoundVisitor;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utilities.IdentityComparator;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;

public class Resolver {
	private BoundSet bounds;
	private final Map<GenericTypeContext<?>, Map<TypeVariable<?>, InferenceVariable<?>>> inferenceVariables;

	private final Map<InferenceVariable<?>, Type> instantiations;

	private final MultiMap<InferenceVariable<?>, InferenceVariable<?>, ? extends Set<InferenceVariable<?>>> remainingDependencies;
	private final Map<Collection<Type>, IntersectionType> leastUpperBounds;

	public Resolver() {
		this.bounds = new BoundSet();
		this.inferenceVariables = new TreeMap<>(
				new IdentityComparator<GenericTypeContext<?>>());

		instantiations = new HashMap<>();

		remainingDependencies = new MultiHashMap<>(HashSet::new);
		leastUpperBounds = new HashMap<>();
	}

	public void incorporateTypeContext(GenericTypeContext<?> context) {
		if (inferenceVariables.containsKey(context))
			return;

		inferenceVariables.put(context, new HashMap<>());

		if (context.getDeclaringType() instanceof ParameterizedType)
			incorporateType(context, (ParameterizedType) context.getDeclaringType());
		incorporateGenericDeclaration(context, context.getGenericDeclaration());
	}

	private void incorporateGenericDeclaration(GenericTypeContext<?> context,
			GenericDeclaration declaration) {
		Map<TypeVariable<?>, InferenceVariable<?>> typeVariableMappings = inferenceVariables
				.get(context);

		List<? extends InferenceVariable<?>> newInferenceVariables = InferenceVariable
				.overGenericTypeContext(context, declaration);

		System.out.println("  " + declaration + " @ " + typeVariableMappings
				+ " ? " + newInferenceVariables);

		for (InferenceVariable<?> inferenceVariable : newInferenceVariables)
			if (!typeVariableMappings
					.containsKey(inferenceVariable.getTypeVariable())) {
				typeVariableMappings.put(inferenceVariable.getTypeVariable(),
						inferenceVariable);

				boolean anyProper = false;
				for (Type bound : inferenceVariable.getBounds()) {
					anyProper = anyProper || Types.isProperType(bound);
					bounds.incorporate().acceptSubtype(inferenceVariable, bound);
				}
				if (!anyProper)
					bounds.incorporate().acceptSubtype(inferenceVariable, Object.class);
			}

		System.out.println("WATTTTTTTTTTTTTTTTTTTTTT" + bounds);

		recalculateRemainingDependencies();
	}

	public void incorporateType(GenericTypeContext<?> context,
			ParameterizedType parameterizedType) {
		incorporateTypeContext(context);

		incorporateGenericDeclaration(context,
				(Class<?>) parameterizedType.getRawType());

		Map<TypeVariable<?>, InferenceVariable<?>> typeVariableMappings = this.inferenceVariables
				.get(context);

		TypeResolver resolver = new TypeResolver();
		for (InferenceVariable<?> variable : typeVariableMappings.values())
			resolver = resolver.where(variable.getTypeVariable(), variable);

		for (int i = 0; i < parameterizedType.getActualTypeArguments().length; i++) {
			TypeVariable<?> typeParameter = ((Class<?>) parameterizedType
					.getRawType()).getTypeParameters()[i];

			incorporate(new ConstraintFormula(Kind.EQUALITY,
					resolver.resolveType(parameterizedType.getActualTypeArguments()[i]),
					typeVariableMappings.get(typeParameter)));
		}

		recalculateRemainingDependencies();
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

		Set<InferenceVariable<?>> inferenceVariables = getInferenceVariables();
		inferenceVariables.removeAll(instantiations.keySet());

		/*
		 * An inference variable α depends on the resolution of itself.
		 */
		for (InferenceVariable<?> inferenceVariable : getInferenceVariables())
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

	public boolean validate() {
		return validate(getInferenceVariables());
	}

	public boolean validate(InferenceVariable<?>... variable) {
		return validate(variable);
	}

	public boolean validate(Collection<? extends InferenceVariable<?>> variable) {
		return resolve(variable).values().stream().allMatch(v -> v != null);
	}

	public Map<InferenceVariable<?>, Type> resolve() {
		return resolve(getInferenceVariables());
	}

	public Type resolve(InferenceVariable<?> variable) {
		Type instantiation = instantiations.get(variable);

		if (instantiation == null) {
			resolveIndependentSet(remainingDependencies.get(variable));
			instantiation = instantiations.get(variable);
		}

		return instantiation;
	}

	public Map<InferenceVariable<?>, Type> resolve(
			InferenceVariable<?>... variables) {
		return resolve(Arrays.asList(variables));
	}

	public Map<InferenceVariable<?>, Type> resolve(
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
		while (!variables.isEmpty()) {
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

			remainingDependencies.keySet().removeAll(minimalSet);
			remainingDependencies.removeAllFromAll(minimalSet);
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
					instantiations.put(instantiation.getKey(), instantiation.getValue());
				}
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

	private Type leastUpperBound(Type... lowerBounds) {
		return leastUpperBound(Arrays.asList(lowerBounds));
	}

	private Type leastUpperBound(Collection<Type> lowerBounds) {
		if (lowerBounds.size() == 1)
			/*
			 * If k = 1, then the lub is the type itself: lub(U) = U.
			 */
			return lowerBounds.iterator().next();
		else {
			/*
			 * Proxy guard against recursive generation of infinite types
			 */
			IntersectionType leastUpperBoundProxy = leastUpperBounds.get(lowerBounds);
			if (leastUpperBoundProxy != null)
				return leastUpperBoundProxy;

			IdentityProperty<IntersectionType> leastUpperBoundResult = new IdentityProperty<>();
			leastUpperBoundProxy = () -> leastUpperBoundResult.get().getTypes();
			leastUpperBounds.put(lowerBounds, leastUpperBoundProxy);

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

			List<Type> bestTypes = erasedCandidates.entrySet().stream()
					.map(e -> best(e.getKey(), e.getValue()))
					.collect(Collectors.toList());

			leastUpperBoundResult.set(IntersectionType.of(bestTypes));

			return leastUpperBoundResult.get();
		}
	}

	private void minimiseCandidates(
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

	private Type best(Class<?> rawClass, Set<ParameterizedType> parameterizations) {
		if (parameterizations.isEmpty())
			return rawClass;
		else if (parameterizations.size() == 1) {
			Type parameterization = parameterizations.iterator().next();
			return parameterization == null ? rawClass : parameterization;
		}

		List<TypeVariable<?>> typeVariables = new ArrayList<>();
		Class<?> enclosingClass = rawClass;
		do {
			typeVariables.addAll(Arrays.asList(enclosingClass.getTypeParameters()));
		} while ((enclosingClass = enclosingClass.getEnclosingClass()) != null);

		Map<TypeVariable<?>, Type> leastContainingParameterization = new HashMap<>();

		for (ParameterizedType type : parameterizations)
			for (TypeVariable<?> variable : typeVariables)
				if (type != null) {
					Type argumentU = TypeToken.of(type).resolveType(variable).getType();
					Type argumentV = leastContainingParameterization.get(variable);

					if (argumentV == null)
						leastContainingParameterization.put(variable, argumentU);
					else {
						leastContainingParameterization.put(variable,
								leastContainingArgument(argumentU, argumentV));
					}
				}

		TypeResolver resolver = new TypeResolver();
		for (TypeVariable<?> variable : typeVariables) {
			Type argument = leastContainingParameterization.get(variable);
			resolver = resolver.where(variable, argument);
		}

		Type best = Types.parameterizedType(rawClass,
				leastContainingParameterization);
		return best;
	}

	private Type leastContainingArgument(Type argumentU, Type argumentV) {
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<Class<?>, ParameterizedType> getErasedSupertypes(Type of) {
		Map<Class<?>, ParameterizedType> supertypes = new HashMap<>();

		RecursiveTypeVisitor
				.build()
				.visitSupertypes()
				.classVisitor(
						type -> {
							Type parameterized = ((TypeToken) TypeToken.of(of)).getSupertype(
									type).getType();
							supertypes
									.put(
											type,
											(parameterized instanceof ParameterizedType) ? (ParameterizedType) parameterized
													: null);
						})
				.parameterizedTypeVisitor(
						type -> supertypes.put(TypeLiteral.of(type).getRawType(), type))
				.create().visit(of);

		return supertypes;
	}

	private Type greatestLowerBound(Type... upperBounds) {
		return greatestLowerBound(Arrays.asList(upperBounds));
	}

	private Type greatestLowerBound(Collection<Type> upperBounds) {
		if (upperBounds.size() == 1)
			return upperBounds.iterator().next();
		else
			return validateGreatestLowerBound(IntersectionType.of(upperBounds));
	}

	public IntersectionType validateGreatestLowerBound(
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

	public Type resolve(GenericTypeContext<?> context, Type type) {
		return null; // TODO
	}

	public Type resolveTypeParameters(GenericTypeContext<?> context,
			Class<?> rawType) {
		incorporateTypeContext(context);

		if (rawType.isArray())
			return Types.genericArrayType(resolveTypeParameters(context,
					rawType.getComponentType()));

		Class<?> enclosingClass = rawType;
		Map<TypeVariable<?>, Type> typeParameters = new HashMap<>();
		do {
			for (TypeVariable<?> typeParameter : enclosingClass.getTypeParameters())
				typeParameters.put(typeParameter, null);
		} while ((enclosingClass = enclosingClass.getEnclosingClass()) != null);

		if (typeParameters.isEmpty())
			return rawType;

		Class<?> declaringClass = Types.getRawType(context.getDeclaringType());
		if (declaringClass.isAssignableFrom(rawType)) {
			incorporateType(context, Types.parameterizedType(rawType));

			Class<?> temp = declaringClass;
			declaringClass = rawType;
			rawType = temp;
		} else if (!rawType.isAssignableFrom(declaringClass))
			return Types.parameterizedType(rawType);

		ParameterizedType subtype = null;
		do {
			Set<Type> supertypes = new HashSet<>();
			supertypes.addAll(Arrays.asList(declaringClass.getGenericInterfaces()));
			if (declaringClass.getGenericSuperclass() != null)
				supertypes.add(declaringClass.getGenericSuperclass());

			declaringClass = null;
			for (Type supertype : supertypes) {
				Class<?> superclass = Types.getRawType(supertype);
				if (rawType.isAssignableFrom(superclass)) {
					subtype = (ParameterizedType) supertype;
					declaringClass = superclass;
					break;
				}
			}

			if (declaringClass != null)
				incorporateType(context, subtype);
		} while (declaringClass != null && rawType.isAssignableFrom(declaringClass));

		resolve(getInferenceVariables(context, typeParameters.keySet()));

		for (TypeVariable<?> typeParameter : typeParameters.keySet())
			typeParameters.put(typeParameter,
					getInstantiation(context, typeParameter));

		return Types.parameterizedType(rawType, typeParameters);
	}

	public Type resolve(GenericTypeContext<?> context,
			TypeVariable<?> typeVariable) {
		incorporateTypeContext(context);

		Type instantiation = instantiations.get(inferenceVariables.get(context)
				.get(typeVariable));
		if (instantiation != null)
			return instantiation;

		if (typeVariable.getGenericDeclaration() instanceof Executable) {
			incorporateGenericDeclaration(context,
					typeVariable.getGenericDeclaration());
			resolveTypeParameters(context,
					((Executable) typeVariable.getGenericDeclaration())
							.getDeclaringClass());
		} else if (typeVariable.getGenericDeclaration() instanceof Class)
			resolveTypeParameters(context,
					((Class<?>) typeVariable.getGenericDeclaration()));

		resolve(inferenceVariables.get(context).get(typeVariable));
		return getInstantiation(context, typeVariable);
	}

	public Type getInstantiation(GenericTypeContext<?> context,
			TypeVariable<?> variable) {
		return instantiations.get(inferenceVariables.get(context).get(variable));
	}

	public InferenceVariable<?> getInferenceVariable(
			GenericTypeContext<?> context, TypeVariable<?> variable) {
		return inferenceVariables.get(context).get(variable);
	}

	public Set<InferenceVariable<?>> getInferenceVariables(
			GenericTypeContext<?> context,
			Collection<? extends TypeVariable<?>> variables) {
		HashSet<InferenceVariable<?>> inferenceVariables = new HashSet<>();
		for (TypeVariable<?> variable : variables)
			inferenceVariables
					.add(this.inferenceVariables.get(context).get(variable));
		return inferenceVariables;
	}

	public Set<InferenceVariable<?>> getInferenceVariables(
			GenericTypeContext<?> context) {
		return new HashSet<>(inferenceVariables.get(context).values());
	}

	public Set<InferenceVariable<?>> getInferenceVariables() {
		return inferenceVariables.values().stream().map(Map::values)
				.flatMap(Collection::stream).collect(Collectors.toSet());
	}

	public void incorporate(ConstraintFormula constraintFormula) {
		bounds.incorporate(constraintFormula);
	}

	static class A<T> {
		class B<U extends Set<T>> {

		}

		class C<V> extends A<Map<V, V>>.B<HashSet<Map<V, V>>> {}
	}

	public static void main(String... args) {
		TypeLiteral<?> stringsType = new TypeLiteral<A<Map<String, String>>.B<HashSet<Map<String, String>>>>() {};

		Resolver resolver = new Resolver();
		System.out.println(resolver.resolveTypeParameters(stringsType, A.C.class));
	}
}
