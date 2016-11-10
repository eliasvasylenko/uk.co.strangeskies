package uk.co.strangeskies.reflection.codegen;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * An immutable base class for any source declaration objects which are
 * annotated.
 * 
 * <p>
 * Fields such as {@link #annotations} in this class, and in subclasses, may be
 * unguarded to mutation by subclasses so as to avoid unnecessary allocation
 * when deriving new instances of annotated declarations. Implementations should
 * take care to avoid this.
 * 
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          a self-bound over the type of the implementing class
 */
public abstract class AnnotatedSignature<S extends AnnotatedSignature<S>> implements Signature<S> {
	protected final Collection<? extends Annotation> annotations;

	public AnnotatedSignature() {
		annotations = emptySet();
	}

	protected AnnotatedSignature(Collection<? extends Annotation> annotations) {
		this.annotations = annotations;
	}

	/**
	 * @return the annotations on this declaration
	 */
	public Stream<? extends Annotation> getAnnotations() {
		return annotations.stream();
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
	public S withAnnotations(Annotation... annotations) {
		return withAnnotatedDeclarationData(asList(annotations));
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
	public S withAnnotations(Collection<? extends Annotation> annotations) {
		return withAnnotatedDeclarationData(annotations);
	}

	protected abstract S withAnnotatedDeclarationData(Collection<? extends Annotation> annotations);
}
