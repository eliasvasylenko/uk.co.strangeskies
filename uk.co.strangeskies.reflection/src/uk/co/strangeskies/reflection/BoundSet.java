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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class BoundSet {
	private static final AtomicLong COUNTER = new AtomicLong();

	private final Set<Bound> bounds;
	private final Map<InferenceVariable, InferenceVariableData> inferenceVariableData;
	private final Set<CaptureConversion> captureConversions;

	public BoundSet() {
		bounds = new HashSet<>();
		inferenceVariableData = new HashMap<>();
		captureConversions = new HashSet<>();
	}

	public BoundSet(BoundSet boundSet) {
		this();

		bounds.addAll(boundSet.bounds);
		inferenceVariableData.putAll(boundSet.inferenceVariableData
				.values()
				.stream()
				.collect(
						Collectors.toMap(InferenceVariableData::getInferenceVariable,
								InferenceVariableData::new)));
	}

	Map<InferenceVariable, InferenceVariableData> getInferenceVariableData() {
		return inferenceVariableData;
	}

	public Set<CaptureConversion> getCaptureConversions() {
		return captureConversions;
	}

	public Set<Type> getUpperBounds(InferenceVariable variable) {
		return inferenceVariableData.get(variable).getUpperBounds();
	}

	public Set<Type> getLowerBounds(InferenceVariable variable) {
		return inferenceVariableData.get(variable).getLowerBounds();
	}

	public Set<Type> getProperUpperBounds(InferenceVariable variable) {
		return inferenceVariableData.get(variable).getUpperBounds().stream()
				.filter(BoundSet.this::isProperType).collect(Collectors.toSet());
	}

	public Set<Type> getProperLowerBounds(InferenceVariable variable) {
		return inferenceVariableData.get(variable).getLowerBounds().stream()
				.filter(BoundSet.this::isProperType).collect(Collectors.toSet());
	}

	public Set<Type> getEqualities(InferenceVariable variable) {
		return inferenceVariableData.get(variable).getEqualities();
	}

	public Optional<Type> getInstantiation(InferenceVariable variable) {
		return inferenceVariableData.get(variable).getEqualities().stream()
				.filter(BoundSet.this::isProperType).findAny();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<InferenceVariable> getInferenceVariablesMentionedBy(Type type) {
		return (Set) Types.getAllMentionedBy(type,
				inferenceVariableData.keySet()::contains);
	}

	public boolean isProperType(Type type) {
		return getInferenceVariablesMentionedBy(type).isEmpty();
	}

	@Override
	public String toString() {
		return new StringBuilder("{ ")
				.append(
						bounds.stream().map(Object::toString)
								.collect(Collectors.joining(", "))).append(" } ").toString();
	}

	public Set<InferenceVariable> getInferenceVariables() {
		return new HashSet<>(inferenceVariableData.keySet());
	}

	public Set<InferenceVariable> getInstantiatedVariables() {
		return inferenceVariableData.keySet().stream()
				.filter(i -> getInstantiation(i).isPresent())
				.collect(Collectors.toSet());
	}

	public Set<Bound> getBounds() {
		return new HashSet<>(bounds);
	}

	public BoundVisitor incorporate() {
		return new ReductionTarget();
	}

	class ReductionTarget implements BoundVisitor {
		private final ConstraintFormula constraintFormula;

		public ReductionTarget(ConstraintFormula constraintFormula) {
			this.constraintFormula = constraintFormula;
		}

		public ReductionTarget() {
			this.constraintFormula = null;
		}

		@Override
		public void acceptEquality(InferenceVariable a, InferenceVariable b) {
			bounds.add(new Bound(v -> v.acceptEquality(a, b)));

			inferenceVariableData.get(a).addEquality(b);
			inferenceVariableData.get(b).addEquality(a);
		}

		@Override
		public void acceptEquality(InferenceVariable a, Type b) {
			bounds.add(new Bound(v -> v.acceptEquality(a, b)));

			inferenceVariableData.get(a).addEquality(b);
		}

		@Override
		public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
			bounds.add(new Bound(v -> v.acceptSubtype(a, b)));

			inferenceVariableData.get(a).addUpperBound(b);
			inferenceVariableData.get(b).addLowerBound(a);
		}

		@Override
		public void acceptSubtype(InferenceVariable a, Type b) {
			bounds.add(new Bound(v -> v.acceptSubtype(a, b)));

			inferenceVariableData.get(a).addUpperBound(b);
		}

		@Override
		public void acceptSubtype(Type a, InferenceVariable b) {
			bounds.add(new Bound(v -> v.acceptSubtype(a, b)));

			inferenceVariableData.get(b).addLowerBound(a);
		}

		@Override
		public void acceptCaptureConversion(CaptureConversion c) {
			bounds.add(new Bound(v -> v.acceptCaptureConversion(c)));

			captureConversions.add(c);

			for (InferenceVariableData inferenceVariable : inferenceVariableData
					.values()) {
				for (Type equality : inferenceVariable.getEqualities())
					if (!inferenceVariableData.containsKey(equality))
						inferenceVariable.incorporateCapturedEquality(c, equality);

				for (Type lowerBound : inferenceVariable.getLowerBounds())
					if (!inferenceVariableData.containsKey(lowerBound))
						inferenceVariable.incorporateCapturedSupertype(c, lowerBound);

				for (Type upperBound : inferenceVariable.getUpperBounds())
					if (!inferenceVariableData.containsKey(upperBound))
						inferenceVariable.incorporateCapturedSubtype(c, upperBound);
			}
		}

		@Override
		public void acceptFalsehood() {
			if (constraintFormula != null)
				throw new TypeInferenceException("Cannot reduce constraint "
						+ constraintFormula + " into bounds set " + BoundSet.this + ".");
			else
				throw new TypeInferenceException(
						"Addition of falsehood into bounds set " + BoundSet.this + ".");
		}
	}

	public void removeCaptureConversions(
			Collection<? extends CaptureConversion> captureConversions) {
		captureConversions.forEach(c -> {
			this.captureConversions.remove(c);
			bounds.remove(new Bound(b -> b.acceptCaptureConversion(c)));
		});
	}

	public InferenceVariable createInferenceVariable() {
		return createInferenceVariable("INF");
	}

	public InferenceVariable createInferenceVariable(String name) {
		String finalName = name + "#" + COUNTER.incrementAndGet();

		InferenceVariable inferenceVariable = new InferenceVariable() {
			@Override
			public String toString() {
				return finalName;
			}
		};
		inferenceVariableData.put(inferenceVariable, new InferenceVariableData(
				this, inferenceVariable));
		return inferenceVariable;
	}
}
