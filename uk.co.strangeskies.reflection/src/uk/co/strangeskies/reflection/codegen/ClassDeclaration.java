package uk.co.strangeskies.reflection.codegen;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.Types.getRawType;
import static uk.co.strangeskies.reflection.Types.isInterface;
import static uk.co.strangeskies.reflection.codegen.MethodDeclaration.declareConstructor;
import static uk.co.strangeskies.reflection.codegen.MethodDeclaration.declareStaticMethod;
import static uk.co.strangeskies.reflection.token.TypeToken.overType;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.TypeToken;

public class ClassDeclaration<E, T> extends ParameterizedDeclaration<ClassSignature<T>>
		implements Declaration<ClassSignature<T>> {
	private final ClassDeclaration<?, E> enclosingClass;
	private final ClassSignature<T> signature;

	private final Class<? super T> superClass;
	private final List<TypeToken<? super T>> superTypes;
	private final TypeToken<T> superType;

	private final List<MethodDeclaration<E, T>> constructorDeclarations;
	private final List<MethodDeclaration<E, ?>> staticMethodDeclarations;
	private final List<MethodDeclaration<T, ?>> methodDeclarations;

	@SuppressWarnings("unchecked")
	public ClassDeclaration(ClassDeclaration<?, E> enclosingClass, ClassSignature<T> signature) {
		super(signature);

		this.enclosingClass = enclosingClass;
		this.signature = signature;

		superTypes = unmodifiableList(
				signature
						.getSuperTypes()
						.map(this::substituteTypeVariableSignatures)
						.map(TypeToken::overAnnotatedType)
						.map(t -> (TypeToken<? super T>) t)
						.collect(toList()));

		Type superType = intersectionOf(superTypes.stream().map(TypeToken::getType).collect(toList()));
		this.superType = (TypeToken<T>) overType(superType);
		superClass = (Class<? super T>) of(getRawType(superType)).filter(t -> !isInterface(t)).orElse(null);

		constructorDeclarations = signature
				.getConstructorSignatures()
				.map(s -> declareConstructor(this, s))
				.collect(toList());

		staticMethodDeclarations = signature
				.getStaticMethodSignatures()
				.map(s -> declareStaticMethod(this, (MethodSignature<?>) s))
				.collect(toList());

		methodDeclarations = new MethodOverrides<>(this).getDeclarations().collect(toList());
	}

	/**
	 * @return the declared supertypes of the class definition
	 */
	public Stream<? extends TypeToken<? super T>> getSuperTypes() {
		return superTypes.stream();
	}

	/**
	 * @return the intersection of the declared supertypes of the class definition
	 */
	public TypeToken<? super T> getSuperType() {
		return superType;
	}

	/**
	 * @return the non-interface superclass of the class definition, which will be
	 *         {@link Object} if none is explicitly given
	 */
	public Class<? super T> getSuperClass() {
		return superClass;
	}

	public ClassDeclaration<?, E> getEnclosingClass() {
		return enclosingClass;
	}

	@Override
	public ClassSignature<T> getSignature() {
		return signature;
	}

	public Stream<? extends MethodDeclaration<E, T>> constructorDeclarations() {
		return constructorDeclarations.stream();
	}

	public Stream<? extends MethodDeclaration<E, ?>> staticMethodDeclarations() {
		return staticMethodDeclarations.stream();
	}

	public Stream<? extends MethodDeclaration<T, ?>> methodDeclarations() {
		return methodDeclarations.stream();
	}

	public ClassDefinition<E, ? extends T> define() {
		return new ClassDefinition<>(this);
	}

	public TypeToken<T> asToken() {
		// TODO Auto-generated method stub
		return null;
	}
}
