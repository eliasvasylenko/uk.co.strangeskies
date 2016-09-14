package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Take care not to allow instances of this class to leak out into the wider
 * program outside the context of declaration and definition of
 * {@link GenericSignature mutable declarations}.
 * 
 * @author Elias N Vasylenko
 */
public class TypeVariableSignature implements Type {
	private final int index;
	private final List<AnnotatedType> bounds;
	private final List<Annotation> annotations;

	/**
	 * @param index
	 *          the index of the type variable signature within its generic
	 *          declaration
	 */
	public TypeVariableSignature(int index) {
		this.index = index;
		bounds = new ArrayList<>();
		annotations = new ArrayList<>();
	}

	/**
	 * @return the index of the type variable signature within its generic
	 *         declaration
	 */
	public int getIndex() {
		return index;
	}

	@Override
	public String getTypeName() {
		return "T" + index;
	}

	public List<AnnotatedType> getBounds() {
		return bounds;
	}

	public TypeVariableSignature withUpperBounds(TypeToken<?>... bounds) {
		return withUpperBounds(Arrays.stream(bounds).map(TypeToken::getAnnotatedDeclaration).collect(Collectors.toList()));
	}

	public TypeVariableSignature withUpperBounds(Type... bounds) {
		return withUpperBounds(Arrays.stream(bounds).map(AnnotatedTypes::over).collect(Collectors.toList()));
	}

	public TypeVariableSignature withUpperBounds(AnnotatedType... bounds) {
		return withUpperBounds(Arrays.asList(bounds));
	}

	public TypeVariableSignature withUpperBounds(Collection<? extends AnnotatedType> bounds) {
		this.bounds.addAll(bounds);

		return this;
	}

	public TypeVariableSignature withAnnotations(Annotation... annotations) {
		return withAnnotations(Arrays.asList(annotations));
	}

	public TypeVariableSignature withAnnotations(Collection<? extends Annotation> annotations) {
		this.annotations.addAll(annotations);

		return this;
	}
}
