/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.co.strangeskies.reflection.IntersectionTypes.uncheckedIntersectionOf;
import static uk.co.strangeskies.reflection.ParameterizedTypes.getAllTypeArguments;

import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.utilities.DeepCopyable;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Isomorphism;

/**
 * <p>
 * A {@link TypeResolver} represents a view over an underlying {@link BoundSet},
 * and provides a number of important functionalities for interacting with that
 * {@link BoundSet}. Multiple {@link TypeResolver}s can provide different views
 * of the same {@link BoundSet} instance.
 * 
 * <p>
 * Whenever any {@link InferenceVariable} is created by way of a
 * {@link TypeResolver} instance, that {@link InferenceVariable} will be
 * associated with a particular {@link GenericDeclaration}. Within this context,
 * which is so described by a {@link GenericDeclaration}, at most only one
 * {@link InferenceVariable} may by created for any given {@link TypeVariable}.
 * A {@link TypeResolver} always creates {@link InferenceVariable} according to
 * the behavior of
 * {@link TypeResolver#inferOverTypeParameters(GenericDeclaration)}.
 * 
 * <p>
 * A {@link TypeResolver} is a flexible and powerful tool, but for typical
 * use-cases it may be recommended to use the more limited, but more type safe,
 * facilities provided by the {@link TypeToken} and {@link ExecutableToken}
 * classes.
 * 
 * @author Elias N Vasylenko
 */
public class TypeResolver implements DeepCopyable<TypeResolver> {
	private BoundSet bounds;

	/**
	 * Create a new {@link TypeResolver} over the given {@link BoundSet}.
	 * 
	 * @param bounds
	 *          The exact bound set we wish to create a resolver over. Operations
	 *          on the new resolver may mutate the given bound set.
	 */
	public TypeResolver(BoundSet bounds) {
		this.bounds = bounds;
	}

	/**
	 * Create a new resolver over a new bound set.
	 */
	public TypeResolver() {
		this(new BoundSet());
	}

	@Override
	public TypeResolver copy() {
		TypeResolver copy = new TypeResolver(bounds.copy());

		return copy;
	}

	/**
	 * Create a copy of an existing resolver. All the inference variables
	 * contained within the resolver will be substituted for the new inference
	 * variables, and all the bounds on them will be substituted for equivalent
	 * bounds. These mappings will be entered into the given map when they are
	 * made.
	 * 
	 * @param isomorphism
	 *          an isomorphism for inference variables
	 * @return A newly derived resolver, with each instance of an inference
	 *         variable substituted for new mappings.
	 */
	@Override
	public TypeResolver deepCopy(Isomorphism isomorphism) {
		return new TypeResolver(bounds.deepCopy(isomorphism));
	}

	/**
	 * Create a copy of an existing resolver. All the inference variables
	 * contained within the resolver will be substituted for the values they index
	 * to in the given map in the new resolver, and all the bounds on them will be
	 * substituted for equivalent bounds.
	 * 
	 * @param isomorphism
	 *          an isomorphism for inference variables
	 * @return A newly derived resolver, with each instance of an inference
	 *         variable substituted for its mapping in the given map, where one
	 *         exists.
	 */
	public TypeResolver withInferenceVariableSubstitution(Isomorphism isomorphism) {
		return new TypeResolver(bounds.withInferenceVariableSubstitution(isomorphism));
	}

	/**
	 * @return The bound set backing this resolver.
	 */
	public BoundSet getBounds() {
		return bounds;
	}

	/**
	 * Each type variable within the given {@link GenericDeclaration}, and each
	 * non-statically enclosing declaration thereof, is incorporated into the
	 * backing {@link BoundSet}. Each new {@link InferenceVariable} created in
	 * this process is registered in this {@link TypeResolver} under the given
	 * declaration, including those of enclosing {@link GenericDeclaration}s.
	 * 
	 * <p>
	 * If the declaration is a non-static {@link Executable}, first the declaring
	 * class is incorporated, then the resulting inference variables are also
	 * registered under the {@link Executable}, then the type parameters of the
	 * {@link Executable} itself are registered. This means that any
	 * {@link Executable}s registered within a single resolver will always share
	 * the inference variables on their declaring class with those registered
	 * directly under that class.
	 * 
	 * @param declaration
	 *          The declaration we wish to incorporate.
	 * @return A mapping from the {@link InferenceVariable}s on the given
	 *         declaration, to their new capturing {@link InferenceVariable}s.
	 */
	public Map<TypeVariable<?>, InferenceVariable> inferOverTypeParameters(GenericDeclaration declaration) {
		if (!capturedTypeVariables.containsKey(declaration)) {
			Map<TypeVariable<?>, InferenceVariable> declarationCaptures = new HashMap<>();
			capturedTypeVariables.put(declaration, declarationCaptures);

			GenericDeclaration enclosingDeclaration;

			if (declaration instanceof Executable && !Modifier.isStatic(((Executable) declaration).getModifiers())) {
				enclosingDeclaration = ((Executable) declaration).getDeclaringClass();
			} else if (declaration instanceof Class<?> && !Modifier.isStatic(((Class<?>) declaration).getModifiers())) {
				enclosingDeclaration = ((Class<?>) declaration).getEnclosingClass();
			} else {
				enclosingDeclaration = null;
			}

			if (enclosingDeclaration != null) {
				declarationCaptures.putAll(inferOverTypeParameters(enclosingDeclaration));
			}

			infer(getBounds(), declaration, declarationCaptures);

			return declarationCaptures;
		}
		return getInferenceVariables(declaration);
	}

	private static void infer(
			BoundSet bounds,
			GenericDeclaration declaration,
			Map<TypeVariable<?>, InferenceVariable> existingCaptures) {
		Stream<TypeVariable<?>> declarationVariables;
		if (declaration instanceof Class)
			declarationVariables = ParameterizedTypes.getAllTypeParameters((Class<?>) declaration);
		else
			declarationVariables = Arrays.stream(declaration.getTypeParameters());

		List<Map.Entry<? extends TypeVariable<?>, InferenceVariable>> captures = declarationVariables.map(v -> {
			InferenceVariable i;

			if (existingCaptures.containsKey(v)) {
				i = existingCaptures.get(v);
			} else {
				i = new InferenceVariable(v.getName());
				bounds.addInferenceVariable(i);
			}

			return new AbstractMap.SimpleEntry<>(v, i);
		}).collect(toList());

		captures.forEach(c -> existingCaptures.put(c.getKey(), c.getValue()));

		TypeSubstitution substitution = new TypeSubstitution(existingCaptures);
		for (Map.Entry<? extends TypeVariable<?>, InferenceVariable> capture : captures) {
			bounds.withIncorporated().subtype(capture.getValue(),
					substitution.resolve(uncheckedIntersectionOf(capture.getKey().getBounds())));
		}

		for (Map.Entry<? extends TypeVariable<?>, InferenceVariable> capture : captures) {
			InferenceVariable inferenceVariable = capture.getValue();

			bounds.getBoundsOn(inferenceVariable).getUpperBounds().forEach(
					bound -> bounds.withIncorporated().subtype(inferenceVariable, bound));
			bounds.withIncorporated().subtype(inferenceVariable, Object.class);
		}
	}

	/**
	 * The given type is incorporated into the resolver, in a fashion dictated by
	 * the class of that type, as follows:
	 * 
	 * <ul>
	 * <li>{@link Class} as per
	 * {@link #inferOverTypeParameters(GenericDeclaration)} .</li>
	 * 
	 * <li>{@link ParameterizedType} as per
	 * {@link #captureTypeArguments(ParameterizedType)}.</li>
	 * 
	 * <li>{@link GenericArrayType} as per {@link #captureType(Type)} invoked for
	 * it's component type.</li>
	 * 
	 * <li>{@link IntersectionType} as per {@link #captureType(Type)} invoked for
	 * each member.</li>
	 * 
	 * <li>{@link WildcardType} as per
	 * {@link #inferOverWildcardType(WildcardType)}.</li>
	 * </ul>
	 * 
	 * @param type
	 *          The type we wish to incorporate.
	 * @return The new internal representation of the given type, which may
	 *         substitute type variable captures in place of wildcards.
	 */
	public Type inferOverType(Type type) {
		IdentityProperty<Type> result = new IdentityProperty<>(type);

		new TypeVisitor() {
			@Override
			protected void visitClass(Class<?> t) {
				inferOverTypeParameters(t);
			}

			@Override
			protected void visitParameterizedType(ParameterizedType type) {
				result.set(inferOverTypeArguments(type));
			}

			@Override
			protected void visitGenericArrayType(GenericArrayType type) {
				result.set(inferOverTypeArguments(type));
			}

			@Override
			protected void visitIntersectionType(IntersectionType type) {
				visit(type.getTypes());
			}

			@Override
			protected void visitTypeVariable(TypeVariable<?> type) {}

			@Override
			protected void visitWildcardType(WildcardType type) {
				inferOverWildcardType(type);
			}

			@Override
			protected void visitInferenceVariable(InferenceVariable type) {
				bounds = bounds.withInferenceVariable(type);
			}
		}.visit(type);

		return result.get();
	}

	/**
	 * Incorporate a new inference variable for a given wildcard type, and add the
	 * bounds of the wildcard as bounds to the inference variable.
	 * 
	 * @param type
	 *          The wildcard type to capture as a bounded inference variable.
	 * @return The new inference variable created to satisfy the given wildcard.
	 */
	public InferenceVariable inferOverWildcardType(WildcardType type) {
		InferenceVariable w = new InferenceVariable();
		BoundSet bounds = bounds.withInferenceVariable(w);

		for (Type lowerBound : type.getLowerBounds())
			bounds = new ConstraintFormula(Kind.SUBTYPE, lowerBound, w).reduce(bounds);

		for (Type upperBound : type.getUpperBounds())
			bounds = new ConstraintFormula(Kind.SUBTYPE, w, upperBound).reduce(bounds);

		this.bounds = bounds;

		return w;
	}

	/**
	 * Add inference variables for the type parameters of the given type to the
	 * resolver, then incorporate containment constraints based on the arguments
	 * of the given type.
	 * 
	 * @param type
	 *          The type whose generic type arguments we wish to perform inference
	 *          operations over.
	 * @return A parameterized type derived from the given type, with inference
	 *         variables in place of wildcards where appropriate.
	 */
	public GenericArrayType inferOverTypeArguments(GenericArrayType type) {
		Type innerComponent = Types.getInnerComponentType(type);
		if (innerComponent instanceof ParameterizedType) {
			return ArrayTypes.fromComponentType(inferOverTypeArguments((ParameterizedType) innerComponent),
					Types.getArrayDimensions(type));
		} else
			return type;
	}

	/**
	 * Add inference variables for the type parameters of the given type to the
	 * resolver, then incorporate containment constraints based on the arguments
	 * of the given type.
	 * 
	 * @param type
	 *          The type whose generic type arguments we wish to perform inference
	 *          operations over.
	 * @return A parameterized type derived from the given type, with inference
	 *         variables in place of wildcards where appropriate.
	 */
	public ParameterizedType inferOverTypeArguments(ParameterizedType type) {
		Class<?> rawType = Types.getRawType(type);

		getAllTypeArguments(type).map(e -> new SimpleEntry<>(e.getKey(), e.getValue()));

		for (Map.Entry<TypeVariable<?>, Type> argument : arguments) {
			if (argument instanceof InferenceVariable) {
				declarationCaptures.put(argument.getKey(), (InferenceVariable) argument.getValue());
			}
		}

		infer(getBounds(), rawType, declarationCaptures);

		for (Map.Entry<TypeVariable<?>, Type> argument : arguments) {
			bounds = new ConstraintFormula(Kind.CONTAINMENT, declarationCaptures.get(argument.getKey()), argument.getValue())
					.reduce(bounds);
		}

		type = (ParameterizedType) resolveType(ParameterizedTypes.parameterizeUnchecked(rawType, declarationCaptures::get));
		return type;
	}

	/**
	 * Infer proper instantiations for each inference variable registered within
	 * this {@link TypeResolver} instance.
	 * 
	 * @return A mapping from each inference variable registered under this
	 *         resolver, to their newly inferred instantiations.
	 */
	public Stream<Entry<InferenceVariable, Type>> infer() {
		return infer(bounds.getInferenceVariables().collect(toList()));
	}

	/**
	 * Infer a proper instantiations for each {@link InferenceVariable} mentioned
	 * by the given type.
	 * 
	 * @param type
	 *          the type whose proper form we wish to infer
	 * @return a new type derived from the given type by substitution of
	 *         instantiations for each {@link InferenceVariable} mentioned
	 */
	public Type infer(Type type) {
		return new TypeSubstitution(t -> getBounds().containsInferenceVariable(t) ? infer((InferenceVariable) t) : null)
				.resolve(type);
	}

	/**
	 * Infer a proper instantiations for a single given {@link InferenceVariable}.
	 * 
	 * @param inferenceVariable
	 *          the type whose proper form we wish to infer
	 * @return a new instantiation for the given {@link InferenceVariable}
	 */
	public Type infer(InferenceVariable inferenceVariable) {
		if (getBounds().containsInferenceVariable(inferenceVariable)) {
			if (!getBounds().getBoundsOn(inferenceVariable).getInstantiation().isPresent()) {
				Set<InferenceVariable> set = new HashSet<>(1);
				set.add(inferenceVariable);
				infer(set);
			}
			return resolveInferenceVariable(inferenceVariable);
		} else {
			return inferenceVariable;
		}
	}

	/**
	 * Infer proper instantiations for the given {@link InferenceVariable}s.
	 * 
	 * @param variables
	 *          the inference variables for which we wish to infer instantiations
	 * @return a mapping from each of the given inference variables to their
	 *         inferred instantiations
	 */
	public Stream<Map.Entry<InferenceVariable, Type>> infer(InferenceVariable... variables) {
		return infer(Arrays.asList(variables));
	}

	/**
	 * Infer proper instantiations for the given {@link InferenceVariable}s.
	 * 
	 * @param variables
	 *          The inference variables for which we wish to infer instantiations.
	 * @return A mapping from each of the given inference variables to their
	 *         inferred instantiations.
	 */
	public Stream<Map.Entry<InferenceVariable, Type>> infer(Collection<? extends InferenceVariable> variables) {
		variables = variables
				.stream()
				.filter(getBounds()::containsInferenceVariable)
				.map(InferenceVariable.class::cast)
				.collect(Collectors.toSet());

		Map<InferenceVariable, Type> instantiations = new HashMap<>();
		/*
		 * Given a set of inference variables to resolve, let V be the union of this
		 * set and all variables upon which the resolution of at least one variable
		 * in this set depends.
		 */
		Set<InferenceVariable> independentSet = variables
				.stream()
				.filter(v -> !bounds.getBoundsOn(v).getInstantiation().isPresent())
				.map(v -> bounds.getBoundsOn(v).getRemainingDependencies())
				.flatMap(identity())
				.collect(toSet());

		resolveIndependentSet(independentSet);

		return variables
				.stream()
				.map(i -> new AbstractMap.SimpleEntry<>(i, bounds.getBoundsOn(i).getInstantiation().orElseThrow(
						() -> new ReflectionException(p -> p.cannotInstantiateInferenceVariable(i, bounds)))));
	}

	private void resolveIndependentSet(Set<InferenceVariable> variables) {
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
			for (InferenceVariable variable : variables) {
				Set<InferenceVariable> remainingOnVariable = bounds
						.getBoundsOn(variable)
						.getRemainingDependencies()
						.collect(toSet());

				if (remainingOnVariable.size() < minimalSet.size()) {
					minimalSet = remainingOnVariable;
				} else if (remainingOnVariable.size() == minimalSet.size()
						&& !bounds.getRelatedCaptureConversions(remainingOnVariable).findAny().isPresent()) {
					minimalSet = remainingOnVariable;
				}
			}

			resolveMinimalIndepdendentSet(minimalSet);

			bounds
					.getInferenceVariableBounds()
					.filter(i -> i.getInstantiation().isPresent())
					.map(InferenceVariableBounds::getInferenceVariable)
					.forEach(variables::remove);
		}
	}

	private void resolveMinimalIndepdendentSet(Set<InferenceVariable> minimalSet) {
		if (bounds.getRelatedCaptureConversions(minimalSet).findAny().isPresent()) {
			/*
			 * If the bound set does not contain a bound of the form G<..., αi, ...> =
			 * capture(G<...>) for all i (1 ≤ i ≤ n), then a candidate instantiation
			 * Ti is defined for each αi:
			 */
			Map<InferenceVariable, Type> instantiationCandidates = new HashMap<>();

			try {
				for (InferenceVariable variable : minimalSet) {
					IdentityProperty<Boolean> hasThrowableBounds = new IdentityProperty<>(false);

					Type instantiationCandidate;
					List<Type> properLowerBounds = bounds
							.getBoundsOn(variable)
							.getLowerBounds()
							.filter(InferenceVariable::isProperType)
							.collect(toList());
					if (!properLowerBounds.isEmpty()) {
						/*
						 * If αi has one or more proper lower bounds, L1, ..., Lk, then Ti =
						 * lub(L1, ..., Lk) (§4.10.4).
						 */
						instantiationCandidate = Types.leastUpperBound(properLowerBounds);
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
								.getBoundsOn(variable)
								.getUpperBounds()
								.filter(InferenceVariable::isProperType)
								.collect(toList()));
					}

					instantiationCandidates.put(variable, instantiationCandidate);
				}

				bounds = bounds.withInstantiations(instantiationCandidates);

				return;
			} catch (ReflectionException e) {}
		}

		/*
		 * the bound set contains a bound of the form G<..., αi, ...> =
		 * capture(G<...>) for some i (1 ≤ i ≤ n), or;
		 * 
		 * If the bound set produced in the step above contains the bound false;
		 * 
		 * then let Y1, ..., Yn be fresh type variables whose bounds are as follows:
		 * 
		 * ...
		 * 
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
		bounds = TypeVariableCapture.captureInferenceVariables(minimalSet, getBounds());
	}

	private void instantiate(BoundSet bounds, InferenceVariable variable, Type instantiation) {
		bounds.withIncorporated().equality(variable, instantiation);
	}

	/**
	 * Derive a new type from the type given, with any mentioned instances of
	 * {@link InferenceVariable} and {@link TypeVariable} substituted with their
	 * proper instantiations where available, as per
	 * {@link #resolveInferenceVariable(InferenceVariable)} and
	 * {@link #resolveTypeVariable(TypeVariable)} respectively.
	 * 
	 * @param type
	 *          The type we wish to resolve.
	 * @return The resolved type.
	 */
	public Type resolveType(Type type) {
		return new TypeSubstitution()
				.where(InferenceVariable.class::isInstance, t -> resolveInferenceVariable((InferenceVariable) t))
				.resolve(type);
	}

	/**
	 * Resolve the proper instantiation of a given {@link InferenceVariable} if
	 * one exists.
	 * 
	 * @param variable
	 *          The inference variable whose proper instantiation we wish to
	 *          determine.
	 * @return The proper instantiation of the given {@link InferenceVariable} if
	 *         one exists, otherwise the {@link InferenceVariable} itself.
	 */
	public Type resolveInferenceVariable(InferenceVariable variable) {
		if (bounds.getInferenceVariables().anyMatch(variable::equals))
			return bounds.getBoundsOn(variable).getInstantiation().orElse(variable);
		else
			return variable;
	}
}
