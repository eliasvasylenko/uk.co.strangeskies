package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

import uk.co.strangeskies.utilities.IdentityProperty;

public class TypeVariableCapture extends CaptureType implements
		TypeVariable<GenericDeclaration> {
	private final GenericDeclaration declaration;

	private TypeVariableCapture(Type[] upperBounds, Type[] lowerBounds,
			GenericDeclaration declaration) {
		super(upperBounds, lowerBounds);

		this.declaration = declaration;
	}

	public static Map<InferenceVariable, TypeVariableCapture> capture(
			Collection<? extends InferenceVariable> types) {
		TypeVariable<?>[] captures = new TypeVariable<?>[types.size()];
		GenericDeclaration declaration = new GenericDeclaration() {
			@Override
			public Annotation[] getDeclaredAnnotations() {
				return new Annotation[0];
			}

			@Override
			public Annotation[] getAnnotations() {
				return new Annotation[0];
			}

			@Override
			public <T extends Annotation> @Nullable T getAnnotation(
					Class<T> paramClass) {
				return null;
			}

			@Override
			public TypeVariable<?>[] getTypeParameters() {
				return null;
			}
		};

		IdentityProperty<Integer> count = new IdentityProperty<>(0);
		return CaptureType.capture(
				types,
				inferenceVariable -> {
					/*
					 * For all i (1 ≤ i ≤ n), if αi has one or more proper lower bounds
					 * L1, ..., Lk, then let the lower bound of Yi be lub(L1, ..., Lk); if
					 * not, then Yi has no lower bound.
					 */
					Set<Type> lowerBoundSet = Arrays
							.stream(inferenceVariable.getLowerBounds())
							.filter(Types::isProperType).collect(Collectors.toSet());

					Type[] lowerBounds;
					if (lowerBoundSet.isEmpty())
						lowerBounds = new Type[0];
					else
						lowerBounds = IntersectionType.asArray(Resolver
								.leastUpperBound(lowerBoundSet));

					/*
					 * For all i (1 ≤ i ≤ n), where αi has upper bounds U1, ..., Uk, let
					 * the upper bound of Yi be glb(U1 θ, ..., Uk θ), where θ is the
					 * substitution [α1:=Y1, ..., αn:=Yn].
					 */
					Set<Type> upperBoundSet = Arrays
							.stream(inferenceVariable.getLowerBounds())
							.filter(Types::isProperType).collect(Collectors.toSet());

					Type[] upperBounds;
					if (upperBoundSet.isEmpty())
						upperBounds = new Type[0];
					else
						upperBounds = IntersectionType.asArray(IntersectionType
								.of(upperBoundSet));

					TypeVariableCapture capture = new TypeVariableCapture(upperBounds,
							lowerBounds, declaration);
					/*
					 * If the type variables Y1, ..., Yn do not have well-formed bounds
					 * (that is, a lower bound is not a subtype of an upper bound, or an
					 * intersection type is inconsistent), then resolution fails.
					 */

					captures[count.get()] = capture;
					count.set(count.get() + 1);

					return capture;
				});
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
	public <U extends Annotation> @Nullable U getAnnotation(Class<U> arg0) {
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
				public <T extends Annotation> @Nullable T getAnnotation(
						Class<T> paramClass) {
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
