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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.utilities.DeepCopyable;

/**
 * <p>
 * A bound set as described in chapter 18 of the Java 8 language specification.
 * (Note that some sorts of bounds present in the document are missing from this
 * implementation, as this API is not intended to offer the full capabilities of
 * the compiler with respect to method references and closures.)
 * 
 * <p>
 * A bound set contains a number of {@link InferenceVariable} instances, and
 * maintains a set of bounds between them and between other types. Types which
 * are not inference variables, and do not mention inference variables, are
 * considered <em>proper types</em>.
 * 
 * <p>
 * Note that instances of {@link InferenceVariable} which are not contained
 * within a bound set are not considered inference variables within that
 * context, and are treated as proper types. Inference variables are considered
 * contained within a bound set if they were added through {@link #copy()},
 * {@link #addInferenceVariable(InferenceVariable)} , or as part of a capture
 * conversion added through a {@link IncorporationTarget} belonging to that
 * bound set.
 * 
 * <p>
 * The types of bounds which may be included in a bound set are as follows:
 * 
 * <ul>
 * <li>Equalities between inference variables and other types, which may or may
 * not be inference variables.</li>
 * <li>Upper bounds on inference variables, to types which may or may not
 * themselves be inference variables.</li>
 * <li>Lower bounds on inference variables, from types which may or may not
 * themselves be inference variables.</li>
 * <li>Instances of {@link CaptureConversion} which mention inference variables.
 * </li>
 * <li>The bound 'false', typically meaning that a type inference attempt has
 * failed.</li>
 * </ul>
 * 
 * <p>
 * An equality bound between an inference variable and a <em>proper type</em> is
 * considered that inference variable's <em>instantiation</em>.
 * 
 * <p>
 * When you add such a bound to a bound set, it may imply the addition of
 * further bounds, or the reduction of any number of {@link ConstraintFormula}
 * instances into the bound set.
 * 
 * <p>
 * Bound sets, along with the processes of incorporation and reduction
 * described, are typically used to facilitate inference of the type arguments
 * of a generic method invocations, and to resolve overloads for such
 * invocations between multiple methods when some are generic. This
 * implementation therefore allows us to type check and resolve such an
 * invocations at runtime.
 * 
 * <p>
 * We may also employ these processes towards other ends, such as type checking
 * for data serialisation formats and libraries, injection frameworks, etc.,
 * which may have slightly different rules and requirements to generic method
 * invocation. There are also applications further outside these areas, such as
 * inference of the type arguments of a generic supertype of a given type.
 * 
 * @author Elias N Vasylenko
 */
public class BoundSet implements DeepCopyable<BoundSet> {
	/**
	 * Consumer of different sorts of bounds which can be a applied to inference
	 * variables, as per chapter 18 of the Java 8 language specification.
	 * 
	 * @author Elias N Vasylenko
	 */
	public class IncorporationTarget {
		/**
		 * If one or both of the arguments passed are considered inference variables
		 * within the enclosing BoundSet, the appropriate equality bound is added
		 * and further bounds are inferred as per the Java language specification.
		 * Otherwise, the invocation has no effect.
		 * 
		 * @param first
		 *          The first of two types whose equality we wish to assert.
		 * @param second
		 *          The second of two types whose equality we wish to assert.
		 */
		public void equality(Type first, Type second) {
			if (!first.equals(second))
				try {
					if (inferenceVariableBounds.containsKey(first))
						inferenceVariableBounds.get(first).addEquality(second);

					if (inferenceVariableBounds.containsKey(second))
						inferenceVariableBounds.get(second).addEquality(first);
				} catch (Exception e) {
					throw new TypeException("Cannot add equality bound between '" + first
							+ "' and '" + second + "' to bound set '" + BoundSet.this, e);
				}
		}

		/**
		 * If one or both of the arguments passed are considered inference variables
		 * within the enclosing BoundSet, the appropriate subtype bound is added and
		 * further bounds are inferred as per the Java language specification.
		 * Otherwise, the invocation has no effect.
		 * 
		 * @param subtype
		 *          A type which we wish to assert is a subtype of another.
		 * @param supertype
		 *          A type which we wish to assert is a supertype of another.
		 */
		public void subtype(Type subtype, Type supertype) {
			if (!subtype.equals(supertype))
				try {
					if (inferenceVariableBounds.containsKey(subtype))
						inferenceVariableBounds.get(subtype).addUpperBound(supertype);

					if (inferenceVariableBounds.containsKey(supertype))
						inferenceVariableBounds.get(supertype).addLowerBound(subtype);
				} catch (Exception e) {
					throw new TypeException("Cannot add subtype bound between '"
							+ subtype + "' and '" + supertype + "' to bound set '"
							+ BoundSet.this, e);
				}
		}

		/**
		 * The given capture conversion is added to the enclosing bound set, and
		 * further bounds may be inferred as per the Java language specification.
		 * 
		 * @param captureConversion
		 *          The capture conversion to be incorporated into the bound set.
		 */
		public void captureConversion(CaptureConversion captureConversion) {
			try {
				addCaptureConversion(captureConversion);
			} catch (Exception e) {
				throw new TypeException("Cannot add capture conversion '"
						+ captureConversion + "' to bound set '" + BoundSet.this, e);
			}
		}

		/**
		 * As {@link #falsehood(boolean)} with the argument {@code true} passed to
		 * the parameter {@code throwing}.
		 */
		public void falsehood() {
			falsehood(true);
		}

		/**
		 * False is added to the bound set, invalidating it, and an exception is
		 * optionally thrown describing the problem.
		 * 
		 * @param throwing
		 *          If this parameter is set to true, an exception will be thrown
		 *          detailing the problem, including information about any
		 *          constraint formula which may have been the cause.
		 */
		public void falsehood(boolean throwing) {
			valid = false;
			if (throwing)
				throw new TypeException("Addition of falsehood into bounds set '"
						+ BoundSet.this + "'.");
		}
	}

	private final Map<InferenceVariable, InferenceVariableBounds> inferenceVariableBounds;
	private final Set<CaptureConversion> captureConversions;
	private boolean valid;

	/**
	 * Create an empty bound set.
	 */
	public BoundSet() {
		inferenceVariableBounds = new HashMap<>();
		captureConversions = new HashSet<>();
		valid = true;
	}

	/**
	 * Create a copy of an existing bound set. All the inference variables
	 * contained within the given bound set will also be contained in the new
	 * bound set, and all the bounds on them will also be copied. Subsequent
	 * modifications to the given bound set will not affect the new one, and vice
	 * versa.
	 */
	@Override
	public BoundSet copy() {
		BoundSet copy = new BoundSet();

		copy.captureConversions.addAll(captureConversions);
		copy.inferenceVariableBounds.putAll(inferenceVariableBounds
				.values()
				.stream()
				.collect(
						Collectors.toMap(InferenceVariableBounds::getInferenceVariable,
								i -> i.copyInto(copy))));

		return copy;
	}

	/**
	 * Create a copy of an existing bound set. All the inference variables
	 * contained within the given bound set will be substituted for new inference
	 * variables in the new bound set, and all the bounds on them will be
	 * substituted for equivalent bounds.
	 */
	@Override
	public BoundSet deepCopy() {
		BoundSet copy = new BoundSet();

		/*- 
		Map<InferenceVariable, InferenceVariable> inferenceVariableSubstitutions = new HashMap<>();

		for (InferenceVariable inferenceVariable : inferenceVariableBounds.keySet()) {
			InferenceVariable inferenceVariableCopy = new InferenceVariable(
					inferenceVariable.getName());

			inferenceVariableSubstitutions.put(inferenceVariable,
					inferenceVariableCopy);

			copy.inferenceVariableBounds.put(
					inferenceVariableCopy,
					new InferenceVariableBounds(copy, inferenceVariableBounds
							.get(inferenceVariable), inferenceVariableCopy));
		}

		copy.inferenceVariableBounds.putAll(inferenceVariableBounds
				.values()
				.stream()
				.collect(
						Collectors.toMap(InferenceVariableBounds::getInferenceVariable,
								i -> new InferenceVariableBounds(copy, i))));

		copy.captureConversions.addAll(captureConversions);

		copy.applyTypeSubstitution(new TypeSubstitution(
				inferenceVariableSubstitutions::get));
		 */

		return copy;
	}

	private void applyTypeSubstitution(TypeSubstitution where) {
		inferenceVariableBounds.values().forEach(
				b -> b.applyTypeSubstitution(where));
	}

	/**
	 * @return True if the bound set contains the bound 'false', false otherwise.
	 */
	public boolean containsFalse() {
		return !valid;
	}

	/**
	 * @return A set of all inference variables contained by this bound set.
	 */
	public Set<InferenceVariable> getInferenceVariables() {
		return new HashSet<>(inferenceVariableBounds.keySet());
	}

	/**
	 * @param inferenceVariable
	 *          An inference variable whose state we wish to query.
	 * @return A container representing the state of the given inference variable
	 *         with respect to its bounds.
	 */
	public InferenceVariableBounds getBoundsOn(InferenceVariable inferenceVariable) {
		return inferenceVariableBounds.get(inferenceVariable);
	}

	/**
	 * @return All capture conversion bounds contained within this bound set.
	 */
	public Set<CaptureConversion> getCaptureConversions() {
		return new HashSet<>(captureConversions);
	}

	/**
	 * Determine which inference variables are mentioned by a given type.
	 * 
	 * @param type
	 *          The type for which to find all mentioned inference variables.
	 * @return A set of all the inference variables which are contained within
	 *         this bound set and which are mentioned by the given type.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<InferenceVariable> getInferenceVariablesMentionedBy(Type type) {
		return (Set) Types.getAllMentionedBy(type,
				inferenceVariableBounds.keySet()::contains);
	}

	/**
	 * Determine whether a given type is proper.
	 * 
	 * @param type
	 *          The type for which to determine properness.
	 * @return True if the given type is proper, false otherwise.
	 */
	public boolean isProperType(Type type) {
		return getInferenceVariablesMentionedBy(type).isEmpty();
	}

	/**
	 * @param type
	 *          The type we wish to classify.
	 * @return True if the given type an inference variable within the context of
	 *         this bound set, false otherwise.
	 */
	public boolean isInferenceVariable(Type type) {
		return inferenceVariableBounds.containsKey(type);
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder("{ ");

		for (InferenceVariableBounds inferenceVariable : inferenceVariableBounds
				.values()) {
			String name = inferenceVariable.getInferenceVariable().getTypeName();
			for (Type equality : inferenceVariable.getEqualities())
				stringBuilder.append(name).append(" = ").append(equality.getTypeName())
						.append(", ");
			for (Type supertype : inferenceVariable.getUpperBounds())
				stringBuilder.append(name).append(" <: ")
						.append(supertype.getTypeName()).append(", ");
			for (Type subtype : inferenceVariable.getLowerBounds())
				stringBuilder.append(subtype.getTypeName()).append(" <: ").append(name)
						.append(", ");
		}
		for (CaptureConversion capture : captureConversions)
			stringBuilder.append(capture).append(", ");
		stringBuilder.append(valid);

		return stringBuilder.append(" }").toString();
	}

	/**
	 * @return Each member of the set returned by {@link #getInferenceVariables()}
	 *         which has a valid instantiation.
	 */
	public Set<InferenceVariable> getInstantiatedVariables() {
		return inferenceVariableBounds.keySet().stream()
				.filter(i -> getBoundsOn(i).getInstantiation().isPresent())
				.collect(Collectors.toSet());
	}

	/**
	 * @return A consumer through which bounds may be added to this bound set.
	 */
	public IncorporationTarget incorporate() {
		return new IncorporationTarget();
	}

	/**
	 * Incorporate each bound from this given bound set into the receiver bound
	 * set. Inference variables which are contained in the given bound set will
	 * also be contained within the receiver bound set after incorporation.
	 * 
	 * @param boundSet
	 *          The bound set whose bounds we wish to incorporate.
	 */
	public void incorporate(BoundSet boundSet) {
		incorporate(boundSet, boundSet.getInferenceVariables());
	}

	/**
	 * Incorporate each bound on the given inference variables from this given
	 * bound set into the receiver bound set. Inference variables which are
	 * contained in the given bound set will also be contained within the receiver
	 * bound set after incorporation.
	 * 
	 * @param boundSet
	 *          The bound set whose bounds we wish to incorporate.
	 * @param inferenceVariables
	 *          The inference variables whose bounds we wish to incorporate.
	 */
	public void incorporate(BoundSet boundSet,
			Collection<? extends InferenceVariable> inferenceVariables) {
		Set<InferenceVariable> inferenceVariableSet = new HashSet<>();

		/*
		 * Include all related inference variables within the given boundSet.
		 */
		for (InferenceVariable inferenceVariable : inferenceVariables)
			if (boundSet.isInferenceVariable(inferenceVariable)) {
				inferenceVariableSet.add(inferenceVariable);
				inferenceVariableSet.addAll(boundSet.getBoundsOn(inferenceVariable)
						.getRemainingDependencies());
			}

		/*
		 * Add the inference variables to this bound set.
		 */
		for (InferenceVariable inferenceVariable : inferenceVariableSet)
			inferenceVariableBounds.putIfAbsent(inferenceVariable,
					new InferenceVariableBounds(this, inferenceVariable));

		/*
		 * Incorporate their bounds.
		 */
		for (InferenceVariable inferenceVariable : inferenceVariableSet) {
			InferenceVariableBounds bounds = boundSet.getBoundsOn(inferenceVariable);

			for (Type lowerBound : bounds.getLowerBounds())
				incorporate().subtype(lowerBound, inferenceVariable);

			for (Type upperBound : bounds.getUpperBounds())
				incorporate().subtype(inferenceVariable, upperBound);

			for (Type equality : bounds.getEqualities())
				incorporate().equality(inferenceVariable, equality);
		}
	}

	void addCaptureConversion(CaptureConversion captureConversion) {
		captureConversions.add(captureConversion);

		/*
		 * When a bound set contains a bound of the form G<α1, ..., αn> =
		 * capture(G<A1, ..., An>), new bounds are implied and new constraint
		 * formulas may be implied, as follows.
		 * 
		 * Let P1, ..., Pn represent the type parameters of G and let B1, ..., Bn
		 * represent the bounds of these type parameters. Let θ represent the
		 * substitution [P1:=α1, ..., Pn:=αn]. Let R be a type that is not an
		 * inference variable (but is not necessarily a proper type).
		 * 
		 * A set of bounds on α1, ..., αn is implied, constructed from the declared
		 * bounds of P1, ..., Pn as specified in §18.1.3.
		 * 
		 * In addition, for all i (1 ≤ i ≤ n):
		 */
		for (InferenceVariable inferenceVariable : captureConversion
				.getInferenceVariables()) {
			InferenceVariableBounds bounds = inferenceVariableBounds
					.get(inferenceVariable);

			if (bounds == null) {
				bounds = new InferenceVariableBounds(BoundSet.this, inferenceVariable);
				inferenceVariableBounds.put(inferenceVariable, bounds);
			}

			/*
			 * Recalculate existing dependencies on each inference variable due to
			 * capture, then add dependencies to all other inference variables
			 * mentioned by the capture.
			 */
			bounds.addCaptureConversion(captureConversion);

			Type capturedArgument = captureConversion
					.getCapturedArgument(inferenceVariable);
			TypeVariable<?> capturedParmeter = captureConversion
					.getCapturedParameter(inferenceVariable);

			if (capturedArgument instanceof WildcardType) {
				/*
				 * If Ai is a wildcard of the form ?, or;
				 * 
				 * If Ai is a wildcard of the form ? extends T, or;
				 * 
				 * If Ai is a wildcard of the form ? super T:
				 */
				WildcardType capturedWildcard = (WildcardType) capturedArgument;

				for (Type equality : bounds.getEqualities())
					if (!inferenceVariableBounds.containsKey(equality))
						bounds.incorporateCapturedEquality(capturedWildcard, equality);

				for (Type upperBound : bounds.getUpperBounds())
					if (!inferenceVariableBounds.containsKey(upperBound))
						bounds.incorporateCapturedSubtype(captureConversion,
								capturedWildcard, capturedParmeter, upperBound);

				for (Type lowerBound : bounds.getLowerBounds())
					if (!inferenceVariableBounds.containsKey(lowerBound))
						bounds.incorporateCapturedSupertype(capturedWildcard, lowerBound);
			} else
				/*
				 * If Ai is not a wildcard, then the bound αi = Ai is implied.
				 */
				incorporate().equality(inferenceVariable, capturedArgument);
		}
	}

	void removeCaptureConversions(
			Collection<? extends CaptureConversion> captureConversions) {
		this.captureConversions.removeAll(captureConversions);

		for (CaptureConversion captureConversion : captureConversions)
			for (InferenceVariable inferenceVariable : captureConversion
					.getInferenceVariables())
				getBoundsOn(inferenceVariable).removeCaptureConversion(
						captureConversion);
	}

	/**
	 * Add an inference variable to this bound set such that bounds can be
	 * inferred over it.
	 * 
	 * @param inferenceVariable
	 *          The inference variable to add to the bound set.
	 */
	public void addInferenceVariable(InferenceVariable inferenceVariable) {
		if (!inferenceVariableBounds.containsKey(inferenceVariable))
			inferenceVariableBounds.put(inferenceVariable,
					new InferenceVariableBounds(this, inferenceVariable));
	}
}
