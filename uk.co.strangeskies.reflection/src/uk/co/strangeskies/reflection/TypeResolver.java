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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.co.strangeskies.reflection.IntersectionTypes.uncheckedIntersectionOf;

import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
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

	/*
	 * The extra indirection here, rather than just a Map<TypeVariable<?>,
	 * InferenceVariable> by itself, is because we store TypeVariables for owning
	 * types of generic declarations, meaning otherwise we may have unexpected
	 * collisions if we incorporate two types with different parameterizations of
	 * the same containing type.
	 */
	private final Map<GenericDeclaration, Map<TypeVariable<?>, InferenceVariable>> capturedTypeVariables;

	private final Set<TypeVariableCapture> wildcardCaptures;

	/**
	 * Create a new {@link TypeResolver} over the given {@link BoundSet}.
	 * 
	 * @param bounds
	 *          The exact bound set we wish to create a resolver over. Operations
	 *          on the new resolver may mutate the given bound set.
	 */
	public TypeResolver(BoundSet bounds) {
		this.bounds = bounds;

		capturedTypeVariables = new HashMap<>();
		wildcardCaptures = new HashSet<>();
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
		copy.wildcardCaptures.addAll(wildcardCaptures);
		copy.capturedTypeVariables.putAll(capturedTypeVariables);

		return copy;
	}

	/*
	 * TODO also deep copy wildcardCaptures!
	 */
	@Override
	public TypeResolver deepCopy() {
		return deepCopy(new Isomorphism());
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
		return withNewBoundsSubstitution(isomorphism, bounds.deepCopy(isomorphism));
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
		return withNewBoundsSubstitution(isomorphism, bounds.withInferenceVariableSubstitution(isomorphism));
	}

	private TypeResolver withNewBoundsSubstitution(Isomorphism isomorphism, BoundSet bounds) {
		TypeResolver copy = new TypeResolver(bounds);

		copy.wildcardCaptures.addAll(wildcardCaptures);

		if (isomorphism.byIdentity().isEmpty()) {
			copy.capturedTypeVariables.putAll(capturedTypeVariables);
		} else {
			for (GenericDeclaration declaration : capturedTypeVariables.keySet())
				copy.capturedTypeVariables.put(declaration,
						capturedTypeVariables.get(declaration).entrySet().stream().collect(toMap(

								Entry::getKey,

								i -> (InferenceVariable) isomorphism.byIdentity().getMapping(i.getValue()))));
		}

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

	private static void infer(BoundSet bounds, GenericDeclaration declaration,
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
			bounds.incorporate().subtype(capture.getValue(),
					substitution.resolve(uncheckedIntersectionOf(capture.getKey().getBounds())));
		}

		for (Map.Entry<? extends TypeVariable<?>, InferenceVariable> capture : captures) {
			InferenceVariable inferenceVariable = capture.getValue();

			for (Type bound : bounds.getBoundsOn(inferenceVariable).getUpperBounds()) {
				bounds.incorporate().subtype(inferenceVariable, bound);
			}
			bounds.incorporate().subtype(inferenceVariable, Object.class);
		}
	}

	/**
	 * The given type is captured into the resolver, in a fashion dictated by the
	 * class of that type, as follows:
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
	public Type captureType(Type type) {
		IdentityProperty<Type> result = new IdentityProperty<>(type);

		new TypeVisitor() {
			@Override
			protected void visitClass(Class<?> t) {
				inferOverTypeParameters(t);
			}

			@Override
			protected void visitParameterizedType(ParameterizedType type) {
				result.set(captureTypeArguments(type));
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
				incorporateWildcardCaptures(TypeVariableCapture.captureWildcard(type));
			}

			@Override
			protected void visitInferenceVariable(InferenceVariable type) {
				bounds.addInferenceVariable(type);
			}
		}.visit(type);

		return result.get();
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
				bounds.addInferenceVariable(type);
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
		bounds.addInferenceVariable(w);

		for (Type lowerBound : type.getLowerBounds())
			ConstraintFormula.reduce(Kind.SUBTYPE, lowerBound, w, bounds);

		for (Type upperBound : type.getUpperBounds())
			ConstraintFormula.reduce(Kind.SUBTYPE, w, upperBound, bounds);

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

		List<Map.Entry<TypeVariable<?>, Type>> arguments = ParameterizedTypes.getAllTypeArguments(type).collect(toList());
		Map<TypeVariable<?>, InferenceVariable> declarationCaptures;

		if (!capturedTypeVariables.containsKey(rawType)) {
			declarationCaptures = new HashMap<>();
			capturedTypeVariables.put(rawType, declarationCaptures);

			for (Map.Entry<TypeVariable<?>, Type> argument : arguments) {
				if (argument instanceof InferenceVariable) {
					declarationCaptures.put(argument.getKey(), (InferenceVariable) argument.getValue());
				}
			}

			infer(getBounds(), rawType, declarationCaptures);
		} else {
			declarationCaptures = capturedTypeVariables.get(rawType);
		}

		for (Map.Entry<TypeVariable<?>, Type> argument : arguments) {
			ConstraintFormula.reduce(Kind.CONTAINMENT, declarationCaptures.get(argument.getKey()), argument.getValue(),
					getBounds());
		}

		type = (ParameterizedType) resolveType(ParameterizedTypes.parameterizeUnchecked(rawType, declarationCaptures::get));
		return type;
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
	public Set<Type> getProperUpperBounds(Type type) {
		type = resolveType(type);

		Set<Type> upperBounds = Types.getUpperBounds(type);

		for (Type upperBound : new HashSet<>(upperBounds))
			if (getBounds().containsInferenceVariable(upperBound)) {
				upperBounds.remove(upperBound);

				InferenceVariableBounds bounds = getBounds().getBoundsOn((InferenceVariable) upperBound);
				Stream.concat(bounds.getUpperBounds().stream(), bounds.getEqualities().stream())
						.filter(t -> !getBounds().containsInferenceVariable(t)).forEach(upperBounds::add);
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
	public Set<Type> getProperLowerBounds(Type type) {
		type = resolveType(type);

		Set<Type> lowerBounds = Types.getLowerBounds(type);

		for (Type lowerBound : new HashSet<>(lowerBounds)) {
			if (getBounds().containsInferenceVariable(lowerBound)) {
				lowerBounds.remove(lowerBound);

				InferenceVariableBounds bounds = getBounds().getBoundsOn((InferenceVariable) lowerBound);
				Stream.concat(bounds.getLowerBounds().stream(), bounds.getEqualities().stream())
						.filter(t -> !getBounds().containsInferenceVariable(t)).forEach(lowerBounds::add);
			}
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

		return getProperUpperBounds(type).stream().map(Types::getRawType)
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

		return Types.getRawType(getProperUpperBounds(type).stream().findFirst().orElse(Object.class));
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
		inferOverTypeArguments(type);

		Iterator<Map.Entry<TypeVariable<?>, Type>> originalArguments = ParameterizedTypes.getAllTypeArguments(type)
				.iterator();

		type = TypeVariableCapture.captureWildcardArguments(type);

		ParameterizedTypes.getAllTypeArguments(type).forEach(capturedArgument -> {
			Type originalArgument = originalArguments.next().getValue();

			if (originalArgument instanceof WildcardType) {
				wildcardCaptures.add((TypeVariableCapture) capturedArgument.getValue());
			}

			ConstraintFormula.reduce(Kind.EQUALITY, capturedTypeVariables.get(rawType).get(capturedArgument.getKey()),
					capturedArgument.getValue(), bounds);
		});

		return type;
	}

	/**
	 * Resubstitute any type variable captures mentioned in the given type for the
	 * wildcards which they originally captured, if they were captured through
	 * incorporation of wildcard types into this {@link TypeResolver} instance.
	 * 
	 * @param type
	 *          The type for which we wish to make the substitution.
	 * @return A new type derived from that given by making the substitution.
	 */
	public Type resubstituteCapturedWildcards(Type type) {
		if (wildcardCaptures.isEmpty())
			return type;
		else
			return new TypeSubstitution().where(wildcardCaptures::contains, t -> ((TypeVariableCapture) t).getCapturedType())
					.resolve(type);
	}

	/**
	 * @return The set of type variable captures which were captured from plain
	 *         wildcards, such that those wildcards might be resubstituted for
	 *         them.
	 */
	public Set<TypeVariableCapture> getWildcardCaptures() {
		return wildcardCaptures;
	}

	/**
	 * Add to the set of type variable captures which are considered to have been
	 * captured from plain wildcards, such that those wildcards might be
	 * resubstituted for them.
	 * 
	 * @param wildcardCaptures
	 *          The new type variable captures.
	 */
	public void incorporateWildcardCaptures(TypeVariableCapture... wildcardCaptures) {
		incorporateWildcardCaptures(Arrays.asList(wildcardCaptures));
	}

	/**
	 * Add to the set of type variable captures which are considered to have been
	 * captured from plain wildcards, such that those wildcards might be
	 * resubstituted for them.
	 * 
	 * @param wildcardCaptures
	 *          The new type variable captures.
	 */
	public void incorporateWildcardCaptures(Collection<? extends TypeVariableCapture> wildcardCaptures) {
		this.wildcardCaptures.addAll(wildcardCaptures);
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
	public void incorporateInstantiation(TypeVariable<?> variable, Type instantiation) {
		if (!InferenceVariable.isProperType(instantiation))
			throw new IllegalArgumentException("The given type, '" + instantiation
					+ "', is not proper, and so is not a valid instantiation for '" + variable + "'.");

		inferOverTypeParameters(variable.getGenericDeclaration());
		ConstraintFormula.reduce(Kind.EQUALITY, getInferenceVariable(variable), instantiation, bounds);
	}

	/**
	 * Any type parameters of the given subclass and superclass are incorporated
	 * into the {@link TypeResolver}, as are the parameters of any classes
	 * between, i.e. those classes which are a supertype of the given subclass,
	 * and a subtype of the given superclass. For each subclass in the hierarchy
	 * which provides a parameterization of a corresponding superclass, these
	 * bounds are created and incorporated.
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
		Type subtype;
		if (Types.isGeneric(subclass)) {
			subtype = ParameterizedTypes.parameterizeUnchecked(subclass, i -> null);
		} else {
			subtype = subclass;
		}

		inferOverTypeParameters(subclass);

		if (!superclass.isAssignableFrom(subclass)) {
			throw new IllegalArgumentException("Type '" + subtype + "' is not a valid subtype of '" + superclass + "'.");
		}

		while (!subclass.equals(superclass)) {
			Class<?> finalSubclass = subclass;
			Function<Type, Type> inferenceVariables = t -> {
				if (t instanceof TypeVariable)
					return getInferenceVariable(finalSubclass, (TypeVariable<?>) t);
				else
					return null;
			};

			Set<Type> lesserSubtypes = new HashSet<>(Arrays.asList(subclass.getGenericInterfaces()));
			if (subclass.getSuperclass() != null)
				lesserSubtypes.addAll(Arrays.asList(subclass.getGenericSuperclass()));
			if (lesserSubtypes.isEmpty())
				lesserSubtypes.add(Object.class);

			subtype = lesserSubtypes.stream().filter(t -> Types.isAssignable(Types.getRawType(t), superclass)).findAny()
					.get();
			subtype = new TypeSubstitution(inferenceVariables).resolve(subtype);

			captureType(subtype);
			subclass = Types.getRawType(subtype);
		}
	}

	/**
	 * Infer proper instantiations for each inference variable registered within
	 * this {@link TypeResolver} instance.
	 * 
	 * @return A mapping from each inference variable registered under this
	 *         resolver, to their newly inferred instantiations.
	 */
	public Map<InferenceVariable, Type> infer() {
		infer(getInferenceVariables());
		return bounds.getInstantiatedVariables().stream()
				.collect(Collectors.toMap(Function.identity(), i -> bounds.getBoundsOn(i).getInstantiation().get()));
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
		Map<InferenceVariable, Type> instantiations = infer(inferenceVariables.values());
		return inferenceVariables.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> instantiations.get(e.getValue())));
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
		return new TypeSubstitution(t -> getBounds().containsInferenceVariable(t) ? infer((InferenceVariable) t) : null)
				.resolve(type);
	}

	/**
	 * Infer a proper instantiations for a single given {@link InferenceVariable}.
	 * 
	 * @param inferenceVariable
	 *          The type whose proper form we wish to infer.
	 * @return A new instantiation for the given {@link InferenceVariable}.
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
	public Map<InferenceVariable, Type> infer(Collection<? extends InferenceVariable> variables) {
		variables = variables.stream().filter(getBounds()::containsInferenceVariable).map(InferenceVariable.class::cast)
				.collect(Collectors.toSet());

		Map<InferenceVariable, Type> instantiations = new HashMap<>();
		/*
		 * Given a set of inference variables to resolve, let V be the union of this
		 * set and all variables upon which the resolution of at least one variable
		 * in this set depends.
		 */
		Set<InferenceVariable> independentSet = variables.stream()
				.filter(v -> !bounds.getBoundsOn(v).getInstantiation().isPresent())
				.map(v -> bounds.getBoundsOn(v).getRemainingDependencies()).flatMap(Set::stream).collect(Collectors.toSet());

		resolveIndependentSet(independentSet);

		for (InferenceVariable variable : variables) {
			InferenceVariableBounds variableBounds = bounds.getBoundsOn(variable);
			if (variableBounds.getInstantiation().isPresent()) {
				instantiations.put(variable, variableBounds.getInstantiation().get());
			} else {
				throw new ReflectionException(p -> p.cannotInstantiateInferenceVariable(variable, bounds));
			}
		}

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
			for (InferenceVariable variable : variables) {
				Set<InferenceVariable> remainingOnVariable = bounds.getBoundsOn(variable).getRemainingDependencies();

				if (remainingOnVariable.size() < minimalSet.size()) {
					minimalSet = remainingOnVariable;
				} else if (remainingOnVariable.size() == minimalSet.size()
						&& relatedCaptureConversions(remainingOnVariable).isEmpty()) {
					minimalSet = remainingOnVariable;
				}
			}

			resolveMinimalIndepdendentSet(minimalSet);

			variables.removeAll(bounds.getInstantiatedVariables());
		}
	}

	private Set<CaptureConversion> relatedCaptureConversions(Set<InferenceVariable> variables) {
		Set<CaptureConversion> relatedCaptureConversions = new HashSet<>();

		bounds.getCaptureConversions().forEach(c -> {
			if (c.getInferenceVariables().stream().anyMatch(variables::contains))
				relatedCaptureConversions.add(c);
		});

		return relatedCaptureConversions;
	}

	private void resolveMinimalIndepdendentSet(Set<InferenceVariable> minimalSet) {
		Set<CaptureConversion> relatedCaptureConversions = relatedCaptureConversions(minimalSet);

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
					IdentityProperty<Boolean> hasThrowableBounds = new IdentityProperty<>(false);

					Type instantiationCandidate;
					if (!bounds.getBoundsOn(variable).getProperLowerBounds().isEmpty()) {
						/*
						 * If αi has one or more proper lower bounds, L1, ..., Lk, then Ti =
						 * lub(L1, ..., Lk) (§4.10.4).
						 */
						instantiationCandidate = Types.leastUpperBound(bounds.getBoundsOn(variable).getProperLowerBounds());
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
						instantiationCandidate = Types.greatestLowerBound(bounds.getBoundsOn(variable).getProperUpperBounds());
					}

					instantiationCandidates.put(variable, instantiationCandidate);
				}

				for (Map.Entry<InferenceVariable, Type> instantiation : instantiationCandidates.entrySet()) {
					instantiate(bounds, instantiation.getKey(), instantiation.getValue());
				}

				this.bounds = bounds;

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

	private void instantiate(BoundSet bounds, InferenceVariable variable, Type instantiation) {
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
	public Type resolveTypeWithResubstitutedWildcardCaptures(Type type) {
		return new TypeSubstitution(t -> {
			if (t instanceof InferenceVariable)
				t = resolveInferenceVariable((InferenceVariable) t);
			else if (t instanceof TypeVariable)
				t = resolveTypeVariable((TypeVariable<?>) t);
			else
				return null;

			if (wildcardCaptures.contains(t))
				return ((TypeVariableCapture) t).getCapturedType();
			else
				return t;
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
	public Type resolveTypeWithResubstitutedWildcardCaptures(GenericDeclaration declaration, Type type) {
		return new TypeSubstitution(t -> {
			if (t instanceof InferenceVariable)
				t = resolveInferenceVariable((InferenceVariable) t);
			else if (t instanceof TypeVariable)
				t = resolveTypeVariable(declaration, (TypeVariable<?>) t);
			else
				return null;

			if (wildcardCaptures.contains(t))
				return ((TypeVariableCapture) t).getCapturedType();
			else
				return t;
		}).resolve(type);
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
	 *          context of this {@link TypeResolver}.
	 * @return A parameterized type over the given type, according to the
	 *         inference variables and parameters registered in this resolver, or
	 *         the given type if it is not generic.
	 */
	public Type resolveTypeParametersWithResubstitutedWildcardCaptures(Class<?> type) {
		inferOverTypeParameters(type);
		return resolveTypeWithResubstitutedWildcardCaptures(
				ParameterizedTypes.parameterizeUnchecked(type, getInferenceVariables(type)::get));
	}

	/**
	 * Resolve the type parameters registered under the given class, and derive a
	 * parameterized type using these parameters if appropriate. If the given
	 * class is not generic, it is returned unchanged.
	 * 
	 * @param type
	 *          The type whose parameterization we wish to determine within the
	 *          context of this {@link TypeResolver}.
	 * @return A parameterized type over the given type, according to the
	 *         inference variables and parameters registered in this resolver, or
	 *         the given type if it is not generic.
	 */
	public Type resolveTypeParameters(Class<?> type) {
		inferOverTypeParameters(type);
		return resolveType(ParameterizedTypes.parameterizeUnchecked(type, getInferenceVariables(type)::get));
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
		return resolveTypeVariable(typeVariable.getGenericDeclaration(), typeVariable);
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
	public Type resolveTypeVariable(GenericDeclaration declaration, TypeVariable<?> typeVariable) {
		if (!capturedTypeVariables.containsKey(declaration))
			return typeVariable;

		InferenceVariable inferenceVariable = capturedTypeVariables.get(declaration).get(typeVariable);
		return inferenceVariable == null ? typeVariable : resolveInferenceVariable(inferenceVariable);
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
	 * interaction with this {@link TypeResolver}. Note that this set of
	 * collections may only be a subset of those which would be returned by an
	 * invocation of {@link BoundSet#getInferenceVariables()} on the underlying
	 * resolver.
	 * 
	 * @return The set of variables incorporated into this {@link TypeResolver}.
	 */
	public Set<InferenceVariable> getInferenceVariables() {
		return capturedTypeVariables.values().stream().map(Map::values).flatMap(Collection::stream)
				.collect(Collectors.toSet());
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
	 * @return The set of variables incorporated into this {@link TypeResolver}
	 *         under the context provided by the given declaration.
	 */
	public Map<TypeVariable<?>, InferenceVariable> getInferenceVariables(GenericDeclaration declaration) {
		return Collections.unmodifiableMap(capturedTypeVariables.get(declaration));
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
		return getInferenceVariable(typeVariable.getGenericDeclaration(), typeVariable);
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
	public InferenceVariable getInferenceVariable(GenericDeclaration declaration, TypeVariable<?> typeVariable) {
		return capturedTypeVariables.get(declaration).get(typeVariable);
	}
}
