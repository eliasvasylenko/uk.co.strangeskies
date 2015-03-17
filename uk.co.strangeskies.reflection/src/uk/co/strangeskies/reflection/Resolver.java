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

	public Resolver(BoundSet bounds) {
		this.bounds = bounds;

		capturedDeclarations = new HashSet<>();
		capturedTypeVariables = new HashMap<>();
	}

	public Resolver() {
		this(new BoundSet());
	}

	public Resolver(Resolver that) {
		bounds = new BoundSet(that.bounds);

		capturedDeclarations = new HashSet<>(that.capturedDeclarations);
		capturedTypeVariables = new HashMap<>(that.capturedTypeVariables);
	}

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
			InferenceVariableData aData = bounds.getInferenceVariableData().get(a);

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

	public void capture(GenericDeclaration declaration) {
		if (capturedDeclarations.add(declaration)) {
			Map<TypeVariable<?>, InferenceVariable> declarationCaptures = new HashMap<>();
			capturedTypeVariables.put(declaration, declarationCaptures);

			Map<TypeVariable<?>, InferenceVariable> newInferenceVariables = InferenceVariable
					.capture(this, declaration);

			for (TypeVariable<?> typeVariable : newInferenceVariables.keySet()) {
				InferenceVariable inferenceVariable = newInferenceVariables
						.get(typeVariable);

				declarationCaptures.put(typeVariable, inferenceVariable);

				boolean anyProper = false;
				for (Type bound : bounds.getUpperBounds(inferenceVariable)) {
					anyProper = anyProper || bounds.isProperType(bound);
					bounds.incorporate().acceptSubtype(inferenceVariable, bound);
				}
				if (!anyProper)
					bounds.incorporate().acceptSubtype(inferenceVariable, Object.class);
			}
		}
	}

	public boolean isCaptured(GenericDeclaration declaration) {
		return capturedDeclarations.contains(declaration);
	}

	public void incorporateType(Type types) {
		new TypeVisitor() {
			@Override
			protected void visitClass(Class<?> t) {
				capture(t);
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
			protected void visitTypeVariable(TypeVariable<?> type) {}

			@Override
			protected void visitWildcardType(WildcardType type) {
				visit(type.getLowerBounds());
				visit(type.getUpperBounds());
			}
		}.visit(types);
	}

	static ParameterizedType constrainWildcards(ParameterizedType type) {
		Map<TypeVariable<?>, Type> capturedArguments = new HashMap<>(
				ParameterizedTypes.getAllTypeArguments(type));

		for (TypeVariable<?> parameter : new HashSet<>(capturedArguments.keySet())) {
			Type capturedArgument = capturedArguments.get(parameter);
			if (capturedArgument instanceof WildcardType)
				capturedArguments.put(parameter, new ConstrainedWildcard(parameter,
						(WildcardType) capturedArgument));
		}

		substituteBounds(capturedArguments);

		type = (ParameterizedType) ParameterizedTypes.uncheckedFrom(
				(Class<?>) type.getRawType(), capturedArguments);

		return type;
	}

	private static void substituteBounds(
			Map<? extends Type, ? extends Type> captures) {
		TypeSubstitution substitution = new TypeSubstitution(captures::get);

		for (Type type : captures.keySet()) {
			if (captures.get(type) instanceof ConstrainedWildcard) {
				ConstrainedWildcard capture = (ConstrainedWildcard) captures.get(type);

				for (int i = 0; i < capture.upperBounds.length; i++)
					capture.upperBounds[i] = substitution.resolve(capture.upperBounds[i]);

				for (int i = 0; i < capture.lowerBounds.length; i++)
					capture.lowerBounds[i] = substitution.resolve(capture.lowerBounds[i]);

				capture.upperBounds = IntersectionType.asArray(IntersectionType
						.from(capture.upperBounds));
			}
		}
	}

	private static class ConstrainedWildcard implements WildcardType {
		private Type[] upperBounds;
		private final Type[] lowerBounds;

		public ConstrainedWildcard(TypeVariable<?> parameter, WildcardType type) {
			IntersectionType firstUpperBound = IntersectionType
					.uncheckedFrom(parameter.getBounds());
			IntersectionType secondUpperBound = IntersectionType.uncheckedFrom(type
					.getUpperBounds());

			upperBounds = IntersectionType.asArray(IntersectionType.uncheckedFrom(
					firstUpperBound, secondUpperBound));

			lowerBounds = type.getLowerBounds();
		}

		@Override
		public Type[] getUpperBounds() {
			return upperBounds;
		}

		@Override
		public Type[] getLowerBounds() {
			return lowerBounds;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder("?");

			if (upperBounds.length > 0)
				builder.append(" extends ").append(
						Arrays.stream(upperBounds).map(Types::toString)
								.collect(Collectors.joining(" & ")));

			if (lowerBounds.length > 0)
				builder.append(" super ").append(
						Arrays.stream(lowerBounds).map(Types::toString)
								.collect(Collectors.joining(" & ")));

			return builder.toString();
		}

		@Override
		public boolean equals(Object that) {
			if (!(that instanceof WildcardType))
				return false;
			if (that == this)
				return true;
			WildcardType wildcard = (WildcardType) that;
			return Arrays.equals(upperBounds, wildcard.getUpperBounds())
					&& Arrays.equals(lowerBounds, wildcard.getLowerBounds());
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(getLowerBounds())
					^ Arrays.hashCode(getUpperBounds());
		}
	}

	public void incorporateParameterizedType(ParameterizedType type) {
		Class<?> rawType = Types.getRawType(type);
		capture(rawType);

		type = TypeVariableCapture.capture(type);
		// type = constrainWildcards(type);

		for (Map.Entry<TypeVariable<?>, Type> typeArgument : ParameterizedTypes
				.getAllTypeArguments(type).entrySet()) {
			new ConstraintFormula(Kind.EQUALITY, capturedTypeVariables.get(rawType)
					.get(typeArgument.getKey()), typeArgument.getValue())
					.reduceInto(bounds);
		}
	}

	public void incorporateInstantiation(TypeVariable<?> variable,
			Type instantiation) {
		capture(variable.getGenericDeclaration());
		new ConstraintFormula(Kind.EQUALITY, getInferenceVariable(variable),
				instantiation).reduceInto(bounds);
	}

	public Map<InferenceVariable, Type> infer(GenericDeclaration context) {
		return infer(getInferenceVariables(context));
	}

	public Map<InferenceVariable, Type> infer() {
		infer(getInferenceVariables());
		return bounds
				.getInstantiatedVariables()
				.stream()
				.collect(
						Collectors.toMap(Function.identity(),
								i -> bounds.getInstantiation(i).get()));
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
					variables.stream()
							.filter(v -> !bounds.getInstantiation(v).isPresent())
							.map(remainingDependencies::get).flatMap(Set::stream)
							.collect(Collectors.toSet()), remainingDependencies);

			for (InferenceVariable variable : new HashSet<>(remainingVariables)) {
				Optional<Type> instantiation = bounds.getInstantiation(variable);
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
				recalculateRemainingDependencies();
				variables.removeAll(bounds.getInstantiatedVariables());
			}
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
					if (!bounds.getProperLowerBounds(variable).isEmpty()) {
						/*
						 * If αi has one or more proper lower bounds, L1, ..., Lk, then Ti =
						 * lub(L1, ..., Lk) (§4.10.4).
						 */
						instantiationCandidate = IntersectionType.from(Types
								.leastUpperBound(bounds.getProperLowerBounds(variable)));
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
								.getProperUpperBounds(variable));
					}

					instantiationCandidates.put(variable, instantiationCandidate);
					bounds.incorporate().acceptEquality(variable, instantiationCandidate);
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
				.capture(minimalSet, bounds);

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
		bounds.incorporate().acceptEquality(variable, instantiation);
	}

	public Type resolveType(Type type) {
		return new TypeSubstitution(t -> {
			if (t instanceof InferenceVariable)
				return resolveInferenceVariable((InferenceVariable) t);
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
		capture(typeVariable.getGenericDeclaration());

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
		return bounds.getInstantiation(variable).orElse(variable);
	}

	public Set<InferenceVariable> getInferenceVariables() {
		return bounds.getInferenceVariables();
	}

	public Set<InferenceVariable> getInferenceVariables(
			GenericDeclaration declaration) {
		capture(declaration);
		return new HashSet<>(capturedTypeVariables.get(declaration).values());
	}

	public InferenceVariable getInferenceVariable(TypeVariable<?> typeVariable) {
		return getInferenceVariable(typeVariable.getGenericDeclaration(),
				typeVariable);
	}

	public InferenceVariable getInferenceVariable(GenericDeclaration declaration,
			TypeVariable<?> typeVariable) {
		capture(declaration);
		return capturedTypeVariables.get(declaration).get(typeVariable);
	}

	public static void main(String... args) {
		test();
	}

	static class TT<TTT> extends TypeLiteral<TTT> {}

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

	public static <T> TypeLiteral<List<T>> listOf(Class<T> sub) {
		return new TypeLiteral<List<T>>() {}.withTypeArgument(
				new TypeParameter<T>() {}, sub);
	}

	public static void test() {
		/*
		 * System.out.println(new TypeLiteral<SchemaNode.Effective<?, ?>>() {}
		 * .resolveSupertypeParameters(SchemaNode.class)); System.out.println();
		 * System.out.println();
		 */
		System.out.println(new TypeLiteral<HashSet<String>>() {}
				.resolveSupertypeParameters(Set.class));
		System.out.println();
		System.out.println();

		System.out.println("List with T = String: " + listOf(String.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeLiteral<Collection<? super String>>() {}
				.resolveSubtypeParameters(HashSet.class));
		System.out.println();
		System.out.println();

		new TypeLiteral<Outer<Serializable>.Inner<String, HashSet<Serializable>, Set<String>>>() {}
				.getResolver();
		System.out.println();
		System.out.println();

		System.out
				.println(new TypeLiteral<Outer<Serializable>.Inner<String, HashSet<Serializable>, Set<String>>>() {}
						.resolveSubtypeParameters(Outer2.Inner3.class));
		System.out.println();
		System.out.println();

		System.out
				.println(new TypeLiteral<Outer2<Serializable, String>.Inner3<HashSet<Serializable>>>() {}
						.resolveSupertypeParameters(Outer.Inner.class));
		System.out.println();
		System.out.println();

		System.out.println(new TypeLiteral<Outer<String>.Inner2<Double>>() {}
				.resolveSupertypeParameters(Outer.Inner.class));
		System.out.println();
		System.out.println();

		System.out.println("type test: "
				+ new TypeLiteral<String>() {}
						.resolveSupertypeParameters(Comparable.class));
		System.out.println();
		System.out.println();

		class SM<YO> {}
		class NM<V extends Number> extends SM<V> {}
		System.out.println(new TypeLiteral<NM<?>>() {});
		System.out.println(new TypeLiteral<NM<?>>() {}
				.resolveSupertypeParameters(SM.class));
		System.out.println();
		System.out.println();

		System.out
				.println(TypeLiteral.from(new TypeLiteral<Nest<?>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeLiteral.from(new TypeLiteral<Nest22<?>>() {}
				.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeLiteral.from(new TypeLiteral<Nest2<?>>() {}
				.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeLiteral
				.from(new TypeLiteral<Base<LeftN, RightN>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeLiteral.from(new TypeLiteral<RightN>() {}
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

		System.out.println(new TypeLiteral<Self<?>>() {}
				.isAssignableFrom(new TypeLiteral<Nest<?>>() {}));
		System.out.println();
		System.out.println();

		System.out.println(TypeLiteral
				.from(new TypeLiteral<Nest2<? extends Nest2<?>>>() {}.getType()));
		System.out.println();
		System.out.println();

		System.out.println(TypeLiteral
				.from(new TypeLiteral<Nest2<? extends Nest22<?>>>() {}.getType()));
		System.out.println();
		System.out.println();

		/*
		 * TODO The proper infinite types are correctly inferred for the actual
		 * bounds on S and E here. The problem must be some sort of non-terminating
		 * subsequent validation or infinite unnecessary bound inference...
		 */
		System.out.println(new TypeLiteral<SchemaNode.Effective<?, ?>>() {}
				.resolveSupertypeParameters(SchemaNode.class));
		System.out.println();
		System.out.println();

		TypeLiteral<?> receiver = new TypeLiteral<BindingState>() {};
		System.out.println("RESOLVE 1:");
		System.out.println(receiver.resolveMethodOverload("bindingNode",
				Arrays.asList(int.class)));
		System.out.println();
		System.out.println();

		receiver = new TypeLiteral<SchemaNodeConfigurator<?, ?>>() {};
		System.out.println("RESOLVE 2:");
		System.out.println(TypeLiteral.from(receiver.getType())
				.resolveMethodOverload("name", Arrays.asList(String.class)));
		System.out.println();
		System.out.println();

		receiver = new TypeLiteral<ChildNodeConfigurator<?, ?>>() {};
		System.out.println("RESOLVE 3:");
		System.out.println(TypeLiteral.from(receiver.getType())
				.resolveMethodOverload("name", Arrays.asList(String.class)));
		System.out.println();
		System.out.println();
	}
}

interface BindingState {
	SchemaNode.Effective<?, ?> bindingNode(int parent);
}

interface SchemaNode<S extends SchemaNode<S, E>, E extends SchemaNode.Effective<S, E>> {
	interface Effective<S extends SchemaNode<S, E>, E extends Effective<S, E>>
			extends SchemaNode<S, E> {}
}

interface ChildNode<S extends ChildNode<S, E>, E extends ChildNode.Effective<S, E>>
		extends SchemaNode<S, E> {
	interface Effective<S extends ChildNode<S, E>, E extends Effective<S, E>>
			extends ChildNode<S, E>, SchemaNode.Effective<S, E> {}
}

interface SchemaNodeConfigurator<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<?, ?>> {
	public S name(String name);
}

interface ChildNodeConfigurator<S extends ChildNodeConfigurator<S, N>, N extends ChildNode<?, ?>>
		extends SchemaNodeConfigurator<S, N> {
	public S name(String name);
}

class Nest<T extends Set<Nest<T>>> implements Self<Nest<T>> {
	@Override
	public Nest<T> copy() {
		return null;
	}
}

class Nest2<T extends Nest2<T>> {}

class Nest22<T> extends Nest2<Nest22<T>> {}

class Base<T extends Base<U, T>, U extends Base<T, U>> {}

class LeftN extends Base<RightN, LeftN> {}

class RightN extends Base<LeftN, RightN> {}
