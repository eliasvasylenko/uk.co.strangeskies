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

import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utilities.DeepCopyable;
import uk.co.strangeskies.utilities.IdentityProperty;

/**
 * <p>
 * A {@link Resolver} represents a view over an underlying {@link BoundSet}, and
 * provides a number of important functionalities for interacting with that
 * {@link BoundSet}. Multiple {@link Resolver}s can provide different views of
 * the same {@link BoundSet} instance.
 * 
 * <p>
 * Whenever any {@link InferenceVariable} is created by way of a
 * {@link Resolver} instance, that {@link InferenceVariable} will be associated
 * with a particular {@link GenericDeclaration}. Within this context, which is
 * so described by a {@link GenericDeclaration}, at most only one
 * {@link InferenceVariable} may by created for any given {@link TypeVariable}.
 * A {@link Resolver} always creates {@link InferenceVariable} according to the
 * behaviour of {@link Resolver#incorporateTypeParameters(GenericDeclaration)}.
 * 
 * <p>
 * A {@link Resolver} is a flexible and powerful tool, but for typical use-cases
 * it may be recommended to use the more limited, but more type safe, facilities
 * provided by the {@link TypeToken} and {@link Invokable} classes.
 * 
 * @author Elias N Vasylenko
 */
public class Resolver implements DeepCopyable<Resolver> {
	private BoundSet bounds;

	/*
	 * The extra indirection here, rather than just a Map<TypeVariable<?>,
	 * InferenceVariable> by itself, is because we store TypeVariables for
	 * containing types, meaning otherwise we may have unexpected collisions if we
	 * incorporate two types with different parameterizations of the same
	 * containing type.
	 */
	private final Map<GenericDeclaration, Map<TypeVariable<?>, InferenceVariable>> capturedTypeVariables;

	private final Set<TypeVariableCapture> wildcardCaptures;

	/**
	 * Create a new {@link Resolver} over the given {@link BoundSet}.
	 * 
	 * @param bounds
	 *          The exact bound set we wish to create a resolver over. Operations
	 *          on the new resolver may mutate the given bound set.
	 */
	public Resolver(BoundSet bounds) {
		this.bounds = bounds;

		capturedTypeVariables = new HashMap<>();
		wildcardCaptures = new HashSet<>();
	}

	/**
	 * Create a new resolver over a new bound set.
	 */
	public Resolver() {
		this(new BoundSet());
	}

	@Override
	public Resolver copy() {
		Resolver copy = new Resolver(bounds.copy());

		copy.capturedTypeVariables.putAll(capturedTypeVariables);

		return copy;
	}

	@Override
	public Resolver deepCopy() {
		return deepCopy(new HashMap<>());
	}

	Resolver deepCopy(
			Map<InferenceVariable, InferenceVariable> inferenceVariableSubstitutions) {
		return withNewBoundsSubstitution(
				inferenceVariableSubstitutions,
				bounds
						.withNewInferenceVariableSubstitution(inferenceVariableSubstitutions));
	}

	/**
	 * Create a copy of an existing resolver. All the inference variables
	 * contained within the resolver will be substituted for the values they index
	 * to in the given map in the new resolver, and all the bounds on them will be
	 * substituted for equivalent bounds.
	 * 
	 * @param inferenceVariableSubstitutions
	 *          A mapping from inference variables which may be mentioned in the
	 *          resolver to inference variables they should be substituted for in
	 *          the derived resolver.
	 * @return A newly derived resolver, with each instance of an inference
	 *         variable substituted for its mapping in the given map, where one
	 *         exists.
	 */
	public Resolver withInferenceVariableSubstitution(
			Map<InferenceVariable, InferenceVariable> inferenceVariableSubstitutions) {
		return withNewBoundsSubstitution(inferenceVariableSubstitutions,
				bounds
						.withInferenceVariableSubstitution(inferenceVariableSubstitutions));
	}

	private Resolver withNewBoundsSubstitution(
			Map<InferenceVariable, InferenceVariable> inferenceVariableSubstitutions,
			BoundSet bounds) {
		Resolver copy = new Resolver(bounds);

		for (GenericDeclaration declaration : capturedTypeVariables.keySet())
			copy.capturedTypeVariables.put(
					declaration,
					capturedTypeVariables.get(declaration).entrySet().stream()
							.collect(Collectors.toMap(Entry::getKey, i -> {
								return inferenceVariableSubstitutions.get(i.getValue());
							})));

		return copy;
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
	 * this process is registered in this {@link Resolver} under the given
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
	public Map<TypeVariable<?>, InferenceVariable> incorporateTypeParameters(
			GenericDeclaration declaration) {
		if (!capturedTypeVariables.containsKey(declaration)) {
			Map<TypeVariable<?>, InferenceVariable> declarationCaptures = new HashMap<>();
			capturedTypeVariables.put(declaration, declarationCaptures);

			if (declaration instanceof Executable
					&& !Modifier.isStatic(((Executable) declaration).getModifiers()))
				declarationCaptures
						.putAll(incorporateTypeParameters(((Executable) declaration)
								.getDeclaringClass()));

			capture(getBounds(), declaration, declarationCaptures);

			return declarationCaptures;
		}
		return getInferenceVariables(declaration);
	}

	private static Map<TypeVariable<?>, InferenceVariable> capture(
			BoundSet bounds, GenericDeclaration declaration,
			Map<TypeVariable<?>, InferenceVariable> existingCaptures) {
		List<TypeVariable<?>> declarationVariables;
		if (declaration instanceof Class)
			declarationVariables = ParameterizedTypes
					.getAllTypeParameters((Class<?>) declaration);
		else
			declarationVariables = Arrays.asList(declaration.getTypeParameters());

		Map<TypeVariable<?>, InferenceVariable> captures = declarationVariables
				.stream().collect(
						Collectors.toMap(Function.identity(),
								t -> new InferenceVariable(t.getName())));
		existingCaptures.putAll(captures);

		for (InferenceVariable variable : captures.values())
			bounds.addInferenceVariable(variable);

		TypeSubstitution substitution = new TypeSubstitution(existingCaptures::get);
		for (TypeVariable<?> variable : captures.keySet())
			bounds.incorporate().subtype(
					captures.get(variable),
					substitution.resolve(IntersectionType.uncheckedFrom(variable
							.getBounds())));

		for (TypeVariable<?> typeVariable : captures.keySet()) {
			InferenceVariable inferenceVariable = captures.get(typeVariable);

			boolean anyProper = false;
			for (Type bound : bounds.getBoundsOn(inferenceVariable).getUpperBounds()) {
				anyProper = anyProper || bounds.isProperType(bound);
				bounds.incorporate().subtype(inferenceVariable, bound);
			}
			if (!anyProper)
				bounds.incorporate().subtype(inferenceVariable, Object.class);
		}

		return captures;
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
	 * {@link #inferOverWildcardType(WildcardType)}.</li>
	 * </ul>
	 * 
	 * @param types
	 *          The type we wish to incorporate.
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
				inferOverWildcardType(type);
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
	public InferenceVariable inferOverWildcardType(WildcardType type) {
		InferenceVariable w = new InferenceVariable();
		bounds.addInferenceVariable(w);

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
		for (Type bound : getUpperBounds(lowerBound))
			ConstraintFormula.reduce(Kind.SUBTYPE, bound, type, bounds);
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
		for (Type bound : getLowerBounds(upperBound))
			ConstraintFormula.reduce(kind, type, bound, bounds);
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
			Class<?> rawType = Types.getRawType(type);

			Map<TypeVariable<?>, InferenceVariable> inferenceVariables = incorporateTypeParameters(rawType);
			Map<TypeVariable<?>, Type> arguments = ParameterizedTypes
					.getAllTypeArguments((ParameterizedType) innerComponent);

			for (TypeVariable<?> typeVariable : inferenceVariables.keySet())
				ConstraintFormula.reduce(Kind.CONTAINMENT,
						inferenceVariables.get(typeVariable), arguments.get(typeVariable),
						getBounds());

			return (GenericArrayType) ArrayTypes.fromComponentType(
					resolveType(ParameterizedTypes.uncheckedFrom(rawType,
							inferenceVariables::get)), Types.getArrayDimensions(type));
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

		Map<TypeVariable<?>, InferenceVariable> inferenceVariables = incorporateTypeParameters(rawType);
		Map<TypeVariable<?>, Type> arguments = ParameterizedTypes
				.getAllTypeArguments(type);

		for (TypeVariable<?> typeVariable : inferenceVariables.keySet())
			ConstraintFormula.reduce(Kind.CONTAINMENT,
					inferenceVariables.get(typeVariable), arguments.get(typeVariable),
					getBounds());

		return (ParameterizedType) resolveType(ParameterizedTypes.uncheckedFrom(
				rawType, inferenceVariables::get));
	}

	/**
	 * Find the upper bounds of a given type. Unlike
	 * {@link Types#getUpperBounds(Type)} this respects bounds on the inference
	 * variables in this resolver.
	 * 
	 * @param type
	 *          The type whose bounds we wish to discover.
	 * @return The upper bounds of the given type.
	 */
	public Set<Type> getUpperBounds(Type type) {
		type = resolveType(type);

		Set<Type> upperBounds = Types.getUpperBounds(type);

		for (Type upperBound : new HashSet<>(upperBounds))
			if (getBounds().isInferenceVariable(upperBound)) {
				upperBounds.remove(upperBound);

				InferenceVariableBounds bounds = getBounds().getBoundsOn(
						(InferenceVariable) upperBound);
				Stream
						.concat(bounds.getUpperBounds().stream(),
								bounds.getEqualities().stream())
						.filter(t -> !getBounds().isInferenceVariable(t))
						.forEach(upperBounds::add);
			}

		if (upperBounds.isEmpty())
			upperBounds.add(Object.class);

		return upperBounds;
	}

	/**
	 * Find the lower bounds of a given type. Unlike
	 * {@link Types#getLowerBounds(Type)} this respects bounds on the inference
	 * variables in this resolver.
	 * 
	 * @param type
	 *          The type whose bounds we wish to discover.
	 * @return The lower bounds of the given type, or null if no such bounds
	 *         exist.
	 */
	public Set<Type> getLowerBounds(Type type) {
		type = resolveType(type);

		Set<Type> lowerBounds = Types.getLowerBounds(type);

		for (Type lowerBound : new HashSet<>(lowerBounds))
			if (getBounds().isInferenceVariable(lowerBound)) {
				lowerBounds.remove(lowerBound);

				InferenceVariableBounds bounds = getBounds().getBoundsOn(
						(InferenceVariable) lowerBound);
				Stream
						.concat(bounds.getLowerBounds().stream(),
								bounds.getEqualities().stream())
						.filter(t -> !getBounds().isInferenceVariable(t))
						.forEach(lowerBounds::add);
			}

		return lowerBounds;
	}

	/**
	 * Determine the raw types of a given type, accounting for inference variables
	 * which may have instantiations or upper bounds within the context of this
	 * resolver.
	 * 
	 * @param type
	 *          The type of which we wish to determine the raw type.
	 * @return The raw type of the given type.
	 */
	public Set<Class<?>> getRawTypes(Type type) {
		type = resolveType(type);

		return getUpperBounds(type).stream().map(Types::getRawType)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Determine the raw type of a given type, accounting for inference variables
	 * which may have instantiations or upper bounds within the context of this
	 * resolver.
	 * 
	 * @param type
	 *          The type of which we wish to determine the raw type.
	 * @return The raw type of the given type.
	 */
	public Class<?> getRawType(Type type) {
		type = resolveType(type);

		return Types.getRawType(getUpperBounds(type).stream().findFirst()
				.orElse(Object.class));
	}

	/**
	 * Add inference variables for the type parameters of the given type to the
	 * resolver, then incorporate equality constraints to new
	 * {@link TypeVariableCapture}s for those inference variables, based on the
	 * bounds on the arguments.
	 * 
	 * @param type
	 *          The type whose generic type arguments we wish to capture.
	 * @return A parameterized type derived from the given type, with type
	 *         variable captures in place of wildcards where appropriate.
	 */
	public ParameterizedType captureTypeArguments(ParameterizedType type) {
		Class<?> rawType = Types.getRawType(type);
		incorporateTypeParameters(rawType);

		Map<TypeVariable<?>, Type> originalArguments = ParameterizedTypes
				.getAllTypeArguments(type);

		type = TypeVariableCapture.captureWildcardArguments(type);

		Map<TypeVariable<?>, Type> capturedArguments = ParameterizedTypes
				.getAllTypeArguments(type);

		for (TypeVariable<?> parameter : originalArguments.keySet()) {
			if (originalArguments.get(parameter) instanceof WildcardType) {
				wildcardCaptures.add((TypeVariableCapture) capturedArguments
						.get(parameter));
			}
		}

		for (Map.Entry<TypeVariable<?>, Type> typeArgument : ParameterizedTypes
				.getAllTypeArguments(type).entrySet())
			ConstraintFormula.reduce(Kind.EQUALITY, capturedTypeVariables
					.get(rawType).get(typeArgument.getKey()), typeArgument.getValue(),
					bounds);

		return type;
	}

	/**
	 * Incorporate an instantiation for a type variable. In other words, find the
	 * {@link InferenceVariable} registered for the given {@link TypeVariable}
	 * under the {@link GenericDeclaration} it belongs to, and incorporate an
	 * equality bound on that inference variable to the given type.
	 * 
	 * @param variable
	 *          The type variable whose inference variable we wish to instantiate.
	 * @param instantiation
	 *          The type with which to instantiate the given type variable. This
	 *          should be a proper type.
	 */
	public void incorporateInstantiation(TypeVariable<?> variable,
			Type instantiation) {
		if (!getBounds().isProperType(instantiation))
			throw new IllegalArgumentException("The given type, '" + instantiation
					+ "', is not proper, and so is not a valid instantiation for '"
					+ variable + "'.");

		incorporateTypeParameters(variable.getGenericDeclaration());
		ConstraintFormula.reduce(Kind.EQUALITY, getInferenceVariable(variable),
				instantiation, bounds);
	}

	/**
	 * Infer proper instantiations for each inference variable registered within
	 * this {@link Resolver} instance.
	 * 
	 * @return A mapping from each inference variable registered under this
	 *         resolver, to their newly inferred instantiations.
	 */
	public Map<InferenceVariable, Type> infer() {
		infer(getInferenceVariables());
		return bounds
				.getInstantiatedVariables()
				.stream()
				.collect(
						Collectors.toMap(Function.identity(), i -> bounds.getBoundsOn(i)
								.getInstantiation().get()));
	}

	/**
	 * Infer proper instantiations for each {@link InferenceVariable} registered
	 * under the given {@link GenericDeclaration}.
	 * 
	 * @param context
	 *          The generic declaration whose inference variables we wish to infer
	 *          instantiations for.
	 * @return A mapping from each inference variable registered under the given
	 *         generic declaration, to their newly inferred instantiations.
	 */
	public Map<TypeVariable<?>, Type> infer(GenericDeclaration context) {
		Map<TypeVariable<?>, InferenceVariable> inferenceVariables = getInferenceVariables(context);
		Map<InferenceVariable, Type> instantiations = infer(inferenceVariables
				.values());
		return inferenceVariables
				.entrySet()
				.stream()
				.collect(
						Collectors.toMap(Entry::getKey,
								e -> instantiations.get(e.getValue())));
	}

	/**
	 * Infer a proper instantiations for the {@link InferenceVariable} registered
	 * for the given {@link TypeVariable} under the {@link GenericDeclaration} it
	 * belongs to.
	 * 
	 * @param variable
	 *          The type variable whose instantiation we wish to infer.
	 * @return The proper instantiation inferred for the given type variable.
	 */
	public Type infer(TypeVariable<?> variable) {
		return infer(Arrays.asList(getInferenceVariable(variable))).get(variable);
	}

	/**
	 * Infer a proper instantiations for each {@link InferenceVariable} mentioned
	 * by the given type.
	 * 
	 * @param type
	 *          The type whose proper form we wish to infer.
	 * @return A new type derived from the given type by substitution of
	 *         instantiations for each {@link InferenceVariable} mentioned.
	 */
	public Type infer(Type type) {
		return new TypeSubstitution(infer(getBounds()
				.getInferenceVariablesMentionedBy(type))::get).resolve(type);
	}

	/**
	 * Infer proper instantiations for the given {@link InferenceVariable}s.
	 * 
	 * @param variables
	 *          The inference variables for which we wish to infer instantiations.
	 * @return A mapping from each of the given inference variables to their
	 *         inferred instantiations.
	 */
	public Map<InferenceVariable, Type> infer(InferenceVariable... variables) {
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
	public Map<InferenceVariable, Type> infer(
			Collection<? extends InferenceVariable> variables) {
		variables = variables.stream().filter(getBounds()::isInferenceVariable)
				.map(InferenceVariable.class::cast).collect(Collectors.toSet());

		Map<InferenceVariable, Type> instantiations = new HashMap<>();

		Set<InferenceVariable> remainingVariables = new HashSet<>(variables);
		do {
			/*
			 * Given a set of inference variables to resolve, let V be the union of
			 * this set and all variables upon which the resolution of at least one
			 * variable in this set depends.
			 */
			resolveIndependentSet(variables.stream()
					.filter(v -> !bounds.getBoundsOn(v).getInstantiation().isPresent())
					.map(v -> bounds.getBoundsOn(v).getRemainingDependencies())
					.flatMap(Set::stream).collect(Collectors.toSet()));

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
			int minimalSetSize = variables.size();
			for (InferenceVariable variable : variables) {
				Set<InferenceVariable> remainingOnVariable = bounds.getBoundsOn(
						variable).getRemainingDependencies();
				if (remainingOnVariable.size() < minimalSetSize)
					minimalSetSize = (minimalSet = remainingOnVariable).size();
			}

			resolveMinimalIndepdendentSet(minimalSet);

			variables.removeAll(bounds.getInstantiatedVariables());
			if (!variables.isEmpty()) {
				variables.removeAll(bounds.getInstantiatedVariables());
			}
		}
	}

	/**
	 * Any type parameters of the given subclass and superclass are incorporated
	 * into the {@link Resolver}, as are the parameters of any classes between,
	 * i.e. those classes which are a supertype of the given subclass, and a
	 * subtype of the given superclass. For each subclass in the hierarchy which
	 * provides a parameterization of a corresponding superclass, these bounds are
	 * created and incorporated.
	 * 
	 * <p>
	 * This has the effect, when either the given subclass or superclass are
	 * generic, of establishing any relationship the type arguments of that class
	 * may have with the other class.
	 * 
	 * @param subclass
	 *          A subclass of the given superclass.
	 * @param superclass
	 *          A superclass of the given subclass.
	 */
	public void incorporateTypeHierarchy(Class<?> subclass, Class<?> superclass) {
		Type subtype = ParameterizedTypes.uncheckedFrom(subclass, i -> null);
		incorporateTypeParameters(subclass);

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
			if (lesserSubtypes.isEmpty())
				lesserSubtypes.add(Object.class);

			subtype = lesserSubtypes.stream()
					.filter(t -> Types.isAssignable(Types.getRawType(t), superclass))
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
			BoundSet bounds = this.bounds.copy();
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
			} catch (TypeException e) {
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
		TypeVariableCapture.captureInferenceVariables(minimalSet, getBounds());

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
	}

	private void instantiate(InferenceVariable variable, Type instantiation) {
		bounds.incorporate().equality(variable, instantiation);
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

	/**
	 * Derive a new type from the type given, with any mentioned instances of
	 * {@link InferenceVariable} and {@link TypeVariable} substituted with their
	 * proper instantiations where available, as per
	 * {@link #resolveInferenceVariable(InferenceVariable)} and
	 * {@link #resolveTypeVariable(GenericDeclaration, TypeVariable)}
	 * respectively.
	 * 
	 * @param declaration
	 *          The generic declaration whose context will provide
	 * @param type
	 *          The type we wish to resolve.
	 * @return The resolved type.
	 */
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

	/**
	 * Resolve the type parameters registered under the given class, and derive a
	 * parameterized type using these parameters if appropriate. If the given
	 * class is not generic, it is returned unchanged.
	 * 
	 * @param type
	 *          The type whose parameterization we wish to determine within the
	 *          context of this {@link Resolver}.
	 * @return A parameterized type over the given type, according to the
	 *         inference variables and parameters registered in this resolver, or
	 *         the given type if it is not generic.
	 */
	public Type resolveTypeParameters(Class<?> type) {
		incorporateTypeParameters(type);
		return resolveType(ParameterizedTypes.uncheckedFrom(type,
				getInferenceVariables(type)::get));
	}

	/**
	 * Resolve the proper instantiation of a given {@link TypeVariable} if one
	 * exists. The type variable will be resolved to an {@link InferenceVariable}
	 * with respect to the context provided by its declaring class.
	 * 
	 * @param typeVariable
	 *          The type variable whose proper instantiation we wish to determine.
	 * @return The proper instantiation of the given {@link TypeVariable} if one
	 *         exists, otherwise the {@link TypeVariable} itself.
	 */
	public Type resolveTypeVariable(TypeVariable<?> typeVariable) {
		return resolveTypeVariable(typeVariable.getGenericDeclaration(),
				typeVariable);
	}

	/**
	 * Resolve the proper instantiation of a given {@link TypeVariable} if one
	 * exists. The type variable will be resolved to an {@link InferenceVariable}
	 * with respect to the context provided by the given
	 * {@link GenericDeclaration}.
	 * 
	 * @param declaration
	 *          The {@link GenericDeclaration} under which we will check
	 *          registration of the given {@link TypeVariable}.
	 * @param typeVariable
	 *          The type variable whose proper instantiation we wish to determine.
	 * @return The proper instantiation of the given {@link TypeVariable} if one
	 *         exists, otherwise the {@link TypeVariable} itself.
	 */
	public Type resolveTypeVariable(GenericDeclaration declaration,
			TypeVariable<?> typeVariable) {
		incorporateTypeParameters(declaration);

		InferenceVariable inferenceVariable = capturedTypeVariables
				.get(declaration).get(typeVariable);
		return inferenceVariable == null ? typeVariable
				: resolveInferenceVariable(inferenceVariable);
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
		if (bounds.getInferenceVariables().contains(variable))
			return bounds.getBoundsOn(variable).getInstantiation().orElse(variable);
		else
			return variable;
	}

	/**
	 * Find all the inference variables which have been created through
	 * interaction with this {@link Resolver}. Note that this set of collections
	 * may only be a subset of those which would be returned by an invocation of
	 * {@link BoundSet#getInferenceVariables()} on the underlying resolver.
	 * 
	 * @return The set of variables incorporated into this {@link Resolver}.
	 */
	public Set<InferenceVariable> getInferenceVariables() {
		return capturedTypeVariables.values().stream().map(Map::values)
				.flatMap(Collection::stream).collect(Collectors.toSet());
	}

	/**
	 * Find all the inference variables registered within the context of the given
	 * {@link GenericDeclaration}. Typically this will be one for each of the
	 * {@link TypeVariable}s in the declaration, and one for each
	 * {@link TypeVariable} in any non-statically enclosing classes.
	 * 
	 * @param declaration
	 *          The {@link GenericDeclaration} for which we will resolve inference
	 *          variables.
	 * @return The set of variables incorporated into this {@link Resolver} under
	 *         the context provided by the given declaration.
	 */
	public Map<TypeVariable<?>, InferenceVariable> getInferenceVariables(
			GenericDeclaration declaration) {
		return new HashMap<>(capturedTypeVariables.get(declaration));
	}

	/**
	 * Resolve the proper {@link InferenceVariable} for a given
	 * {@link TypeVariable} with respect to the context provided by its declaring
	 * class.
	 * 
	 * @param typeVariable
	 *          The type variable whose proper instantiation we wish to determine.
	 * @return The proper instantiation of the given {@link TypeVariable} if one
	 *         exists, otherwise the {@link TypeVariable} itself.
	 */
	public InferenceVariable getInferenceVariable(TypeVariable<?> typeVariable) {
		return getInferenceVariable(typeVariable.getGenericDeclaration(),
				typeVariable);
	}

	/**
	 * Resolve the {@link InferenceVariable} for a given {@link TypeVariable} with
	 * respect to the context provided by the given {@link GenericDeclaration}.
	 * 
	 * @param declaration
	 *          The {@link GenericDeclaration} under which we will check
	 *          registration of the given {@link TypeVariable}.
	 * @param typeVariable
	 *          The type variable whose proper instantiation we wish to determine.
	 * @return The proper instantiation of the given {@link TypeVariable} if one
	 *         exists, otherwise the {@link TypeVariable} itself.
	 */
	public InferenceVariable getInferenceVariable(GenericDeclaration declaration,
			TypeVariable<?> typeVariable) {
		return capturedTypeVariables.get(declaration).get(typeVariable);
	}
}
