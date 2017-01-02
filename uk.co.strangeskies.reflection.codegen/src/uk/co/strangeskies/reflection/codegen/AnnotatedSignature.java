package uk.co.strangeskies.reflection.codegen;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * An base type for any source declaration objects which are annotated.
 * Implementations should be immutable.
 * 
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          a self-bound over the type of the implementing class
 */
public interface AnnotatedSignature<S extends AnnotatedSignature<S>> extends Signature<S> {
	/**
	 * @return the annotations on this declaration
	 */
	Stream<? extends Annotation> getAnnotations();

	/**
	 * Derive a version of this declaration with the given annotations. Users
	 * should take care not to specify annotations which could not be applied to
	 * the declaration due to their {@link Target}, as there is no validation of
	 * applicability.
	 * <p>
	 * 
	 * Annotations already present on the receiving declaration will be replaced
	 * rather than appended.
	 * 
	 * @param annotations
	 *          the annotations with which to annotate this declaration
	 * @return a new declaration of the same type, and with the same content, but
	 *         with the given annotations
	 */
	default S withAnnotations(Annotation... annotations) {
		return withAnnotations(asList(annotations));
	}

	/**
	 * Derive a version of this declaration with the given annotations. Users
	 * should take care not to specify annotations which could not be applied to
	 * the declaration due to their {@link Target}, as there is no validation of
	 * applicability.
	 * <p>
	 * 
	 * Annotations already present on the receiving declaration will be replaced
	 * rather than appended.
	 * 
	 * @param annotations
	 *          the annotations with which to annotate this declaration
	 * @return a new declaration of the same type, and with the same content, but
	 *         with the given annotations
	 */
	S withAnnotations(Collection<? extends Annotation> annotations);

	static boolean equals(AnnotatedSignature<?> first, AnnotatedSignature<?> second) {
		return first == second
				|| (first.getAnnotations().collect(toSet()).equals(second.getAnnotations().collect(toSet())));
	}

	static int hashCode(AnnotatedSignature<?> signature) {
		return signature.getAnnotations().mapToInt(Objects::hashCode).reduce(0, (a, b) -> a ^ b);
	}
}
