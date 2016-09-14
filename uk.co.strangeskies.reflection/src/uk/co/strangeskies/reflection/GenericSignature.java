package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.utilities.Isomorphism;

public class GenericSignature {
	private final List<TypeVariableSignature> typeVariableSignatures;

	private Isomorphism isomorphism;
	private GenericDeclaration declaration;

	private final TypeSubstitution typeSubstitution;
	private final AnnotatedTypeSubstitution boundSubstitution;

	public GenericSignature() {
		this.typeVariableSignatures = new ArrayList<>();

		isomorphism = new Isomorphism();

		typeSubstitution = new TypeSubstitution().where(t -> t instanceof TypeVariableSignature,
				t -> substituteTypeVariableSignature((TypeVariableSignature) t));

		boundSubstitution = new AnnotatedTypeSubstitution().where(

				t -> t.getType() instanceof TypeVariableSignature,

				t -> substituteAnnotatedTypeVariableSignature(t));
	}

	@SuppressWarnings("unchecked")
	private <T extends GenericDeclaration> TypeVariable<T> substituteTypeVariableSignature(
			TypeVariableSignature signature) {
		return TypeVariables.upperBounded((T) declaration, signature.getTypeName(),

				signature.getBounds().stream().map(b -> boundSubstitution.resolve(b, isomorphism))
						.collect(Collectors.toList()));
	}

	private <T extends GenericDeclaration> AnnotatedTypeVariable substituteAnnotatedTypeVariableSignature(
			AnnotatedType annotatedSignature) {
		return AnnotatedTypeVariables.over(
				(TypeVariable<?>) typeSubstitution.resolve(annotatedSignature.getType(), isomorphism),
				annotatedSignature.getAnnotations());
	}

	public TypeVariableSignature addTypeVariable() {
		TypeVariableSignature typeVariable = new TypeVariableSignature(typeVariableSignatures.size());
		typeVariableSignatures.add(typeVariable);
		return typeVariable;
	}

	public GenericSignature withTypeVariable() {
		addTypeVariable().withUpperBounds(new Type[] {});
		return this;
	}

	public GenericSignature withTypeVariable(Type... bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public GenericSignature withTypeVariable(AnnotatedType... bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public GenericSignature withTypeVariable(TypeToken<?>... bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public GenericSignature withTypeVariable(List<AnnotatedType> bounds) {
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	public GenericSignature withTypeVariable(Collection<? extends Annotation> annotations,
			Collection<? extends AnnotatedType> bounds) {
		addTypeVariable().withAnnotations(annotations);
		addTypeVariable().withUpperBounds(bounds);
		return this;
	}

	@SuppressWarnings("unchecked")
	public synchronized <T extends GenericDeclaration> List<TypeVariable<T>> getTypeVariables(T declaration) {
		List<TypeVariable<T>> typeVariables = new ArrayList<>(typeVariableSignatures.size());

		isomorphism = new Isomorphism();
		this.declaration = declaration;

		for (TypeVariableSignature signature : typeVariableSignatures) {
			typeVariables.add((TypeVariable<T>) typeSubstitution.resolve(signature, isomorphism));
		}

		isomorphism = null;
		this.declaration = null;

		return typeVariables;
	}
}
