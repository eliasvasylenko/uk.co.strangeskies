package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.checkerframework.checker.nullness.qual.Nullable;

// TODO common supertype with InferenceVariable to share behaviour.
public class TypeVariableCapture implements TypeVariable<GenericDeclaration> {
	private final static AtomicLong COUNTER = new AtomicLong();

	private final String name;
	private final Type[] upperBounds;
	private final Type[] lowerBounds;

	private final GenericDeclaration declaration;

	public TypeVariableCapture(Type[] upperBounds, Type[] lowerBounds,
			GenericDeclaration declaration) {
		this.name = "CAP#" + COUNTER.incrementAndGet();

		this.upperBounds = upperBounds.clone();
		this.lowerBounds = lowerBounds.clone();

		this.declaration = declaration;
	}

	public Type[] getUpperBounds() {
		return upperBounds.clone();
	}

	public Type[] getLowerBounds() {
		return lowerBounds.clone();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

	static <T extends Type> void substituteBounds(
			Map<T, TypeVariableCapture> inferenceVariables) {
		TypeSubstitution substitution = new TypeSubstitution();
		for (Map.Entry<T, TypeVariableCapture> variable : inferenceVariables
				.entrySet())
			substitution = substitution.where(variable.getKey(), variable.getValue());

		for (Map.Entry<T, TypeVariableCapture> variable : inferenceVariables
				.entrySet()) {
			for (int i = 0; i < variable.getValue().upperBounds.length; i++)
				variable.getValue().upperBounds[i] = substitution.resolve(variable
						.getValue().upperBounds[i]);
			for (int i = 0; i < variable.getValue().lowerBounds.length; i++)
				variable.getValue().lowerBounds[i] = substitution.resolve(variable
						.getValue().lowerBounds[i]);
		}
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
		return upperBounds.clone();
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
