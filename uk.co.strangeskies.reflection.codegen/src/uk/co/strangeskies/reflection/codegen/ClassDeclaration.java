package uk.co.strangeskies.reflection.codegen;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.of;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.Types.getRawType;
import static uk.co.strangeskies.reflection.Types.isInterface;
import static uk.co.strangeskies.reflection.codegen.ErasedMethodSignature.erasedConstructorSignature;
import static uk.co.strangeskies.reflection.codegen.ErasedMethodSignature.erasedMethodSignature;
import static uk.co.strangeskies.reflection.codegen.MethodDeclaration.declareConstructor;
import static uk.co.strangeskies.reflection.codegen.MethodDeclaration.declareStaticMethod;
import static uk.co.strangeskies.reflection.token.TypeToken.overType;
import static uk.co.strangeskies.utilities.collection.StreamUtilities.entriesToMap;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.codegen.ClassSpace.ClassDeclarationContext;
import uk.co.strangeskies.reflection.codegen.ExpressionVisitor.ValueExpressionVisitor;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.utilities.collection.StreamUtilities;

public class ClassDeclaration<E, T> extends ParameterizedDeclaration<ClassSignature<T>>
		implements Declaration<ClassSignature<T>> {
	static class Reference implements Type {
		private final String name;

		public Reference(String name) {
			this.name = name;
		}

		@Override
		public String getTypeName() {
			return name;
		}
	}

	private final ClassDeclaration<?, E> enclosingClass;
	private final ClassSignature<T> signature;

	private final Class<? super T> superClass;
	private final List<TypeToken<? super T>> superTypes;
	private final TypeToken<T> superType;

	private final Map<ErasedMethodSignature, MethodDeclaration<E, T>> constructorDeclarations;
	private final Map<ErasedMethodSignature, MethodDeclaration<E, ?>> staticMethodDeclarations;
	private final Map<ErasedMethodSignature, MethodDeclaration<T, ?>> methodDeclarations;

	private final ValueExpression<T> receiverExpression;

	@SuppressWarnings("unchecked")
	protected ClassDeclaration(ClassDeclarationContext context, ClassSignature<T> signature) {
		super(signature);

		this.enclosingClass = (ClassDeclaration<?, E>) signature
				.getEnclosingClassName()
				.map(context::getClassDeclaration)
				.orElse(null);
		this.signature = signature;

		context.addClassDeclaration(this);

		this.superTypes = unmodifiableList(
				signature
						.getSuperTypes()
						.map(this::substituteTypeVariableSignatures)
						.map(TypeToken::overAnnotatedType)
						.map(t -> (TypeToken<? super T>) t)
						.collect(toList()));

		Type superType = intersectionOf(superTypes.stream().map(TypeToken::getType).collect(toList()));
		this.superType = (TypeToken<T>) overType(superType);
		this.superClass = (Class<? super T>) of(getRawType(superType)).filter(t -> !isInterface(t)).orElse(null);

		this.constructorDeclarations = signature.getConstructorSignatures().map(s -> declareConstructor(this, s)).collect(
				toMap(d -> d.getSignature().erased(), identity()));

		this.staticMethodDeclarations = signature
				.getStaticMethodSignatures()
				.map(s -> declareStaticMethod(this, (MethodSignature<?>) s))
				.collect(toMap(d -> d.getSignature().erased(), identity()));

		this.methodDeclarations = new MethodOverrides<>(this).getSignatureDeclarations().collect(entriesToMap());

		this.receiverExpression = new ValueExpression<T>() {
			@Override
			public void accept(ValueExpressionVisitor<T> visitor) {
				visitor.visitReceiver(ClassDeclaration.this);
			};

			@Override
			public TypeToken<T> getType() {
				/*
				 * TODO this needs to be the actual Type
				 */
				return (TypeToken<T>) getSuperType();
			}
		};
	}

	public static <T> ClassDeclaration<?, T> declareClass(ClassDeclarationContext context, ClassSignature<T> signature) {
		return new ClassDeclaration<>(context, signature);
	}

	public static Type referenceClassDeclaration(String name) {
		return new Reference(name);
	}

	public ValueExpression<T> receiver() {
		return receiverExpression;
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

	public Stream<MethodDeclaration<E, T>> constructorDeclarations() {
		return constructorDeclarations.values().stream();
	}

	public Stream<MethodDeclaration<E, ?>> staticMethodDeclarations() {
		return staticMethodDeclarations.values().stream();
	}

	public Stream<MethodDeclaration<T, ?>> methodDeclarations() {
		return methodDeclarations.values().stream();
	}

	public MethodDeclaration<E, T> getConstructorDeclaration(Class<?>... erasedParameters) {
		ErasedMethodSignature erasedSignature = erasedConstructorSignature(erasedParameters);

		MethodDeclaration<E, T> declaration = constructorDeclarations.get(erasedSignature);
		if (declaration == null) {
			throw new CodeGenerationException(p -> p.cannotFindMethodOn(superClass, erasedSignature));
		}
		return declaration;
	}

	public MethodDeclaration<E, ?> getStaticMethodDeclaration(String name, Class<?>... erasedParameters) {
		ErasedMethodSignature erasedSignature = erasedMethodSignature(name, erasedParameters);

		MethodDeclaration<E, ?> declaration = staticMethodDeclarations.get(erasedSignature);
		if (declaration == null) {
			throw new CodeGenerationException(p -> p.cannotFindMethodOn(superClass, erasedSignature));
		}
		return declaration;
	}

	public MethodDeclaration<T, ?> getMethodDeclaration(String name, Class<?>... erasedParameters) {
		ErasedMethodSignature erasedSignature = erasedMethodSignature(name, erasedParameters);

		MethodDeclaration<T, ?> declaration = methodDeclarations.get(erasedSignature);
		if (declaration == null) {
			throw new CodeGenerationException(p -> p.cannotFindMethodOn(superClass, erasedSignature));
		}
		return declaration;
	}

	@SuppressWarnings("unchecked")
	public MethodDeclaration<E, T> getConstructorDeclaration(ConstructorSignature signature) {
		MethodDeclaration<E, ?> declaration = getConstructorDeclaration(
				signature
						.getParameters()
						.map(VariableSignature::getType)
						.map(AnnotatedType::getType)
						.map(Types::getRawType)
						.toArray(Class<?>[]::new));

		if (!StreamUtilities.equals(
				signature.getParameters().map(VariableSignature::getType),
				declaration.getSignature().getParameters().map(VariableSignature::getType))) {
			throw new CodeGenerationException(p -> p.cannotFindMethodOn(superClass, signature.erased()));
		}

		return (MethodDeclaration<E, T>) declaration;
	}

	@SuppressWarnings("unchecked")
	public <U> MethodDeclaration<E, U> getStaticMethodDeclaration(MethodSignature<U> signature) {
		MethodDeclaration<E, ?> declaration = getStaticMethodDeclaration(
				signature.getName(),
				signature
						.getParameters()
						.map(VariableSignature::getType)
						.map(AnnotatedType::getType)
						.map(Types::getRawType)
						.toArray(Class<?>[]::new));

		if (!StreamUtilities.equals(
				signature.getParameters().map(VariableSignature::getType),
				declaration.getSignature().getParameters().map(VariableSignature::getType))) {
			throw new CodeGenerationException(p -> p.cannotFindMethodOn(superClass, signature.erased()));
		}

		return (MethodDeclaration<E, U>) declaration;
	}

	@SuppressWarnings("unchecked")
	public <U> MethodDeclaration<T, U> getMethodDeclaration(MethodSignature<U> signature) {
		MethodDeclaration<T, ?> declaration = getMethodDeclaration(
				signature.getName(),
				signature
						.getParameters()
						.map(VariableSignature::getType)
						.map(AnnotatedType::getType)
						.map(Types::getRawType)
						.toArray(Class<?>[]::new));

		if (!StreamUtilities.equals(
				signature.getParameters().map(VariableSignature::getType),
				declaration.getSignature().getParameters().map(VariableSignature::getType))) {
			throw new CodeGenerationException(p -> p.cannotFindMethodOn(superClass, signature.erased()));
		}

		return (MethodDeclaration<T, U>) declaration;
	}

	public TypeToken<T> asToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		/*-
		if (isPrivate())
			builder.append("private ");
		else if (isProtected())
			builder.append("protected ");
		else if (isPublic())
			builder.append("public ");
		
		if (isNative())
			builder.append("native ");
		if (isStatic())
			builder.append("static ");
		if (isStrict())
			builder.append("strictfp ");
		if (isSynchronized())
			builder.append("synchronized ");
		
		if (isAbstract())
			builder.append("abstract ");
		else if (isFinal())
			builder.append("final ");
		 */

		builder.append(signature.getClassName());

		if (isParameterized()) {
			builder.append("<").append(getTypeVariables().map(Objects::toString).collect(joining(", "))).append("> ");
		}

		return builder.toString();
	}
}
