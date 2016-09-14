package uk.co.strangeskies.reflection;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.co.strangeskies.utilities.Isomorphism;

public class GenericSignature {
	private final List<TypeVariableSignature> typeVariableSignatures;
	private final List<Annotation> annotations;

	private final TypeSubstitution typeSubstitution;
	private final AnnotatedTypeSubstitution boundSubstitution;

	private Isomorphism isomorphism;
	private GenericDeclaration declaration;

	public GenericSignature() {
		typeVariableSignatures = new ArrayList<>();
		annotations = new ArrayList<>();

		typeSubstitution = new TypeSubstitution().where(t -> t instanceof TypeVariableSignature,
				t -> substituteTypeVariableSignature((TypeVariableSignature) t));

		boundSubstitution = new AnnotatedTypeSubstitution().where(

				t -> t.getType() instanceof TypeVariableSignature,

				t -> substituteAnnotatedTypeVariableSignature(t));
	}

	@SuppressWarnings("unchecked")
	protected <T extends GenericDeclaration> TypeVariable<T> substituteTypeVariableSignature(
			TypeVariableSignature signature) {
		return TypeVariables.upperBounded((T) declaration, signature.getTypeName(),

				signature.getBounds().stream().map(b -> boundSubstitution.resolve(b, isomorphism))
						.collect(Collectors.toList()));
	}

	protected <T extends GenericDeclaration> AnnotatedTypeVariable substituteAnnotatedTypeVariableSignature(
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

	public GenericSignature withAnnotations(Annotation... annotations) {
		return withAnnotations(Arrays.asList(annotations));
	}

	public GenericSignature withAnnotations(Collection<? extends Annotation> annotations) {
		this.annotations.addAll(annotations);

		return this;
	}

	public Map<Class<? extends Annotation>, Annotation> getAnnotations() {
		return unmodifiableMap(annotations.stream().collect(toMap(Annotation::annotationType, identity())));
	}

	@SuppressWarnings("unchecked")
	public synchronized <T extends GenericDeclaration> List<TypeVariable<T>> getTypeVariables(T declaration) {
		List<TypeVariable<T>> typeVariables = new ArrayList<>(typeVariableSignatures.size());

		isomorphism = new Isomorphism();
		this.declaration = declaration;

		for (TypeVariableSignature signature : typeVariableSignatures) {
			typeVariables.add((TypeVariable<T>) resolveType(signature));
		}

		isomorphism = null;
		this.declaration = null;

		for (Type type : typeVariables)
			TypeToken.over(type);

		return Collections.unmodifiableList(typeVariables);
	}

	private TypeVariable<T> resolveType(TypeVariableSignature signature) {
		return typeSubstitution.resolve(signature, isomorphism);
	}
}
