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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TypeVariableCapture implements TypeVariable<GenericDeclaration> {
	private final static AtomicLong COUNTER = new AtomicLong();

	private final String name;

	private final Type[] upperBounds;
	private final Type[] lowerBounds;

	private final GenericDeclaration declaration;

	private TypeVariableCapture(Set<Type> upperBounds, Set<Type> lowerBounds,
			GenericDeclaration declaration) {
		this(upperBounds.toArray(new Type[upperBounds.size()]), lowerBounds
				.toArray(new Type[lowerBounds.size()]), declaration);
	}

	private TypeVariableCapture(Type[] upperBounds, Type[] lowerBounds,
			GenericDeclaration declaration) {
		this.name = "CAP#" + COUNTER.incrementAndGet();

		this.upperBounds = upperBounds.clone();
		this.lowerBounds = lowerBounds.clone();

		if (lowerBounds.length > 0
				&& !Types.isAssignable(IntersectionType.uncheckedFrom(lowerBounds),
						IntersectionType.from(upperBounds)))
			throw new TypeInferenceException("Bounds on capture '" + this
					+ "' are invalid. (" + Arrays.toString(lowerBounds) + " <: "
					+ Arrays.toString(upperBounds) + ")");

		this.declaration = declaration;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder().append(getName());

		if (upperBounds.length > 0
				&& !(upperBounds.length == 1 && upperBounds[0] == null))
			builder.append(" extends ").append(
					IntersectionType.uncheckedFrom(upperBounds));

		if (lowerBounds.length > 0
				&& !(lowerBounds.length == 1 && lowerBounds[0] == null))
			builder.append(" super ").append(
					IntersectionType.uncheckedFrom(lowerBounds));

		return builder.toString();
	}

	public Type[] getUpperBounds() {
		return upperBounds.clone();
	}

	public Type[] getLowerBounds() {
		return lowerBounds.clone();
	}

	private static void substituteBounds(
			Map<? extends Type, ? extends Type> captures) {
		TypeSubstitution substitution = new TypeSubstitution(captures::get);

		for (Type type : captures.keySet()) {
			if (captures.get(type) instanceof TypeVariableCapture) {
				TypeVariableCapture capture = (TypeVariableCapture) captures.get(type);

				for (int i = 0; i < capture.upperBounds.length; i++)
					capture.upperBounds[i] = substitution.resolve(capture.upperBounds[i]);

				for (int i = 0; i < capture.lowerBounds.length; i++)
					capture.lowerBounds[i] = substitution.resolve(capture.lowerBounds[i]);
			}
		}
	}

	public static ParameterizedType capture(ParameterizedType type) {
		Map<TypeVariable<?>, Type> arguments = ParameterizedTypes
				.getAllTypeArguments(type);
		Map<TypeVariable<?>, Type> captures = new HashMap<>();

		TypeVariable<?>[] parameters = new TypeVariable<?>[arguments.size()];
		GenericDeclaration declaration = createGenericDeclarationOver(parameters);

		int i = 0;
		for (TypeVariable<?> parameter : arguments.keySet()) {
			Type argument = arguments.get(parameter);
			Type capture;

			if (argument instanceof WildcardType) {
				Type upperBound = IntersectionType.from(
						IntersectionType.from(parameter.getBounds()),
						IntersectionType.from(((WildcardType) argument).getUpperBounds()));

				WildcardType constrained = WildcardTypes.fullyBounded(upperBound,
						IntersectionType.uncheckedFrom(((WildcardType) argument)
								.getLowerBounds()));
				capture = new TypeVariableCapture(constrained.getUpperBounds(),
						constrained.getLowerBounds(), declaration);
			} else
				capture = argument;

			parameters[i++] = parameter;
			captures.put(parameter, capture);
		}

		substituteBounds(captures);

		return (ParameterizedType) ParameterizedTypes.uncheckedFrom(
				Types.getRawType(type), captures);
	}

	public static Map<InferenceVariable, TypeVariableCapture> capture(
			Collection<? extends InferenceVariable> types, BoundSet bounds) {
		TypeVariable<?>[] parameters = new TypeVariable<?>[types.size()];
		GenericDeclaration declaration = createGenericDeclarationOver(parameters);

		int count = 0;

		Map<InferenceVariable, TypeVariableCapture> typeVariableCaptures = new HashMap<>();
		for (InferenceVariable inferenceVariable : types) {
			/*
			 * For all i (1 ≤ i ≤ n), if αi has one or more proper lower bounds L1,
			 * ..., Lk, then let the lower bound of Yi be lub(L1, ..., Lk); if not,
			 * then Yi has no lower bound.
			 */
			Set<Type> lowerBoundSet = bounds.getLowerBounds(inferenceVariable)
					.stream().filter(bounds::isProperType).collect(Collectors.toSet());

			Type[] lowerBounds;
			if (lowerBoundSet.isEmpty())
				lowerBounds = new Type[0];
			else
				lowerBounds = IntersectionType.asArray(Types
						.leastUpperBound(lowerBoundSet));

			/*
			 * For all i (1 ≤ i ≤ n), where αi has upper bounds U1, ..., Uk, let the
			 * upper bound of Yi be glb(U1 θ, ..., Uk θ), where θ is the substitution
			 * [α1:=Y1, ..., αn:=Yn].
			 */
			Set<Type> upperBoundSet = bounds.getUpperBounds(inferenceVariable);

			Type[] upperBounds;
			if (upperBoundSet.isEmpty())
				upperBounds = new Type[0];
			else
				upperBounds = IntersectionType.asArray(IntersectionType
						.uncheckedFrom(upperBoundSet));

			TypeVariableCapture capture = new TypeVariableCapture(upperBounds,
					lowerBounds, declaration);
			/*
			 * If the type variables Y1, ..., Yn do not have well-formed bounds (that
			 * is, a lower bound is not a subtype of an upper bound, or an
			 * intersection type is inconsistent), then resolution fails.
			 */

			parameters[count++] = capture;

			typeVariableCaptures.put(inferenceVariable, capture);
		}

		substituteBounds(typeVariableCaptures);

		return typeVariableCaptures;
	}

	private static GenericDeclaration createGenericDeclarationOver(
			TypeVariable<?>[] captures) {
		return new GenericDeclaration() {
			@Override
			public Annotation[] getDeclaredAnnotations() {
				return new Annotation[0];
			}

			@Override
			public Annotation[] getAnnotations() {
				return new Annotation[0];
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> paramClass) {
				return null;
			}

			@Override
			public TypeVariable<?>[] getTypeParameters() {
				return captures.clone();
			}
		};
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Set<TypeVariableCapture> getAllMentionedBy(Type type) {
		return (Set) Types.getAllMentionedBy(type,
				TypeVariableCapture.class::isInstance);
	}

	public static boolean isProperType(Type type) {
		return getAllMentionedBy(type).isEmpty();
	}

	@Override
	public <U extends Annotation> U getAnnotation(Class<U> arg0) {
		return null;
	}

	@Override
	public Annotation[] getAnnotations() {
		return new Annotation[0];
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return new Annotation[0];
	}

	@Override
	public Type[] getBounds() {
		return getUpperBounds();
	}

	@Override
	public GenericDeclaration getGenericDeclaration() {
		return declaration;
	}

	@Override
	public AnnotatedType[] getAnnotatedBounds() {
		AnnotatedType[] annotatedTypes = new AnnotatedType[getBounds().length];
		for (int i = 0; i < getBounds().length; i++) {
			Type bound = getBounds()[i];
			annotatedTypes[i] = new AnnotatedType() {
				@Override
				public Annotation[] getDeclaredAnnotations() {
					return new Annotation[0];
				}

				@Override
				public Annotation[] getAnnotations() {
					return new Annotation[0];
				}

				@Override
				public <T extends Annotation> T getAnnotation(Class<T> paramClass) {
					return null;
				}

				@Override
				public Type getType() {
					return bound;
				}
			};
		}
		return annotatedTypes;
	}
}
