/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.objectweb.asm.Type.getInternalName;
import static uk.co.strangeskies.reflection.Types.getErasedType;
import static uk.co.strangeskies.reflection.codegen.CodeGenerationException.CODEGEN_PROPERTIES;
import static uk.co.strangeskies.reflection.codegen.ErasedMethodSignature.erasedConstructorSignature;
import static uk.co.strangeskies.reflection.codegen.ErasedMethodSignature.erasedMethodSignature;
import static uk.co.strangeskies.reflection.codegen.MethodDeclaration.declareConstructor;
import static uk.co.strangeskies.reflection.codegen.MethodDeclaration.declareMethod;
import static uk.co.strangeskies.reflection.codegen.MethodDeclaration.declareStaticMethod;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Stream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.signature.SignatureWriter;

import uk.co.strangeskies.collection.stream.StreamUtilities;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.codegen.ClassDefinitionSpace.ClassDeclarationContext;

/**
 * 
 * 
 * @author Elias N Vasylenko
 *
 * @param <E>
 * @param <T>
 */
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

	private final Map<ErasedMethodSignature, MethodDeclaration<E, T>> constructorDeclarations;
	private final Map<ErasedMethodSignature, MethodDeclaration<E, ?>> staticMethodDeclarations;
	private final Map<ErasedMethodSignature, MethodDeclaration<T, ?>> methodDeclarations;

	private final Class<T> stubClass;

	protected ClassDeclaration(ClassDeclarationContext context, ClassSignature<T> signature) {
		this(context, signature, new SignatureWriter());
	}

	@SuppressWarnings("unchecked")
	protected ClassDeclaration(
			ClassDeclarationContext context,
			ClassSignature<T> signature,
			SignatureWriter signatureWriter) {
		super(signature, signatureWriter);

		String typeSignature = writeGenericSupertypes(signatureWriter);

		ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES);
		classWriter.visit(
				V1_8,
				getSignature().getModifiers().toInt(),
				getSignature().getClassName().replace('.', '/'),
				typeSignature,
				getInternalName(
						getErasedType(
								getSignature().getSuperClass().map(AnnotatedType::getType).orElse(Object.class))),
				getSignature()
						.getSuperInterfaces()
						.map(AnnotatedType::getType)
						.map(i -> getInternalName(getErasedType(i)))
						.toArray(String[]::new));

		this.enclosingClass = (ClassDeclaration<?, E>) signature
				.getEnclosingClassName()
				.map(context::getClassDeclaration)
				.orElse(null);

		context.addClassDeclaration(this);

		this.constructorDeclarations = signature
				.getConstructors()
				.map(s -> declareConstructor(this, s, classWriter))
				.collect(toMap(d -> d.getSignature().erased(), identity()));

		this.staticMethodDeclarations = signature
				.getMethods()
				.filter(s -> s.getModifiers().isStatic())
				.map(s -> declareStaticMethod(this, (MethodSignature<?>) s, classWriter))
				.collect(toMap(d -> d.getSignature().erased(), identity()));

		this.methodDeclarations = new MethodOverrides<>(signature)
				.getSignatures()
				.map(s -> declareMethod(this, s, classWriter))
				.collect(toMap(d -> d.getSignature().erased(), identity()));

		this.stubClass = generateStubClass(classWriter);
	}

	private String writeGenericSupertypes(SignatureWriter writer) {
		String typeSignature;
		if (getSignature().getTypeVariables().count() > 0) {
			writer.visitSuperclass();
			ClassWritingContext.visitTypeSignature(
					writer,
					getSignature().getSuperClass().map(AnnotatedType::getType).orElse(Object.class));
			getSignature().getSuperInterfaces().map(AnnotatedType::getType).forEach(type -> {
				ClassWritingContext.visitTypeSignature(writer, type);
			});
			typeSignature = writer.toString();
		} else {
			typeSignature = null;
		}
		return typeSignature;
	}

	@SuppressWarnings("unchecked")
	private Class<T> generateStubClass(ClassWriter writer) {
		DynamicClassLoader classLoader = new DynamicClassLoader(getClass().getClassLoader());
		return (Class<T>) classLoader.injectClass(getSignature().getClassName(), writer.toByteArray());
	}

	public ClassDeclaration<?, E> getEnclosingClassDeclaration() {
		return enclosingClass;
	}

	public static Type referenceClassDeclaration(String name) {
		return new Reference(name);
	}

	@Override
	public ClassSignature<T> getSignature() {
		return super.getSignature();
	}

	public Stream<MethodDeclaration<E, T>> constructorDeclarations() {
		return constructorDeclarations.values().stream();
	}

	public Stream<MethodDeclaration<E, ?>> staticMethodDeclarations() {
		return staticMethodDeclarations.values().stream();
	}

	public Stream<MethodDeclaration<T, ?>> methodDeclarations() {
		return methodDeclarations.values().stream().distinct();
	}

	public MethodDeclaration<E, T> getConstructorDeclaration(Class<?>... erasedParameters) {
		ErasedMethodSignature erasedSignature = erasedConstructorSignature(erasedParameters);

		MethodDeclaration<E, T> declaration = constructorDeclarations.get(erasedSignature);
		if (declaration == null) {
			throw new CodeGenerationException(
					CODEGEN_PROPERTIES.cannotFindMethodOn(stubClass, erasedSignature));
		}
		return declaration;
	}

	public MethodDeclaration<E, ?> getStaticMethodDeclaration(
			String name,
			Class<?>... erasedParameters) {
		ErasedMethodSignature erasedSignature = erasedMethodSignature(name, erasedParameters);

		MethodDeclaration<E, ?> declaration = staticMethodDeclarations.get(erasedSignature);
		if (declaration == null) {
			throw new CodeGenerationException(
					CODEGEN_PROPERTIES.cannotFindMethodOn(stubClass, erasedSignature));
		}
		return declaration;
	}

	public MethodDeclaration<T, ?> getMethodDeclaration(String name, Class<?>... erasedParameters) {
		ErasedMethodSignature erasedSignature = erasedMethodSignature(name, erasedParameters);

		MethodDeclaration<T, ?> declaration = methodDeclarations.get(erasedSignature);
		if (declaration == null) {
			throw new CodeGenerationException(
					CODEGEN_PROPERTIES.cannotFindMethodOn(stubClass, erasedSignature));
		}
		return declaration;
	}

	@SuppressWarnings("unchecked")
	public MethodDeclaration<E, T> getConstructorDeclaration(ConstructorSignature signature) {
		MethodDeclaration<E, ?> declaration = getConstructorDeclaration(
				signature
						.getParameters()
						.map(ParameterSignature::getType)
						.map(AnnotatedType::getType)
						.map(Types::getErasedType)
						.toArray(Class<?>[]::new));

		if (!StreamUtilities.equals(
				signature.getParameters().map(ParameterSignature::getType),
				declaration.getSignature().getParameters().map(ParameterSignature::getType))) {
			throw new CodeGenerationException(
					CODEGEN_PROPERTIES.cannotFindMethodOn(stubClass, signature.erased()));
		}

		return (MethodDeclaration<E, T>) declaration;
	}

	@SuppressWarnings("unchecked")
	public <U> MethodDeclaration<E, U> getStaticMethodDeclaration(MethodSignature<U> signature) {
		MethodDeclaration<E, ?> declaration = getStaticMethodDeclaration(
				signature.getName(),
				signature
						.getParameters()
						.map(ParameterSignature::getType)
						.map(AnnotatedType::getType)
						.map(Types::getErasedType)
						.toArray(Class<?>[]::new));

		if (!StreamUtilities.equals(
				signature.getParameters().map(ParameterSignature::getType),
				declaration.getSignature().getParameters().map(ParameterSignature::getType))) {
			throw new CodeGenerationException(
					CODEGEN_PROPERTIES.cannotFindMethodOn(stubClass, signature.erased()));
		}

		return (MethodDeclaration<E, U>) declaration;
	}

	@SuppressWarnings("unchecked")
	public <U> MethodDeclaration<T, U> getMethodDeclaration(MethodSignature<U> signature) {
		MethodDeclaration<T, ?> declaration = getMethodDeclaration(
				signature.getName(),
				signature
						.getParameters()
						.map(ParameterSignature::getType)
						.map(AnnotatedType::getType)
						.map(Types::getErasedType)
						.toArray(Class<?>[]::new));

		if (!StreamUtilities.equals(
				signature.getParameters().map(ParameterSignature::getType),
				declaration.getSignature().getParameters().map(ParameterSignature::getType))) {
			throw new CodeGenerationException(
					CODEGEN_PROPERTIES.cannotFindMethodOn(stubClass, signature.erased()));
		}

		return (MethodDeclaration<T, U>) declaration;
	}

	public Class<T> getStubClass() {
		return stubClass;
	}

	@Override
	public String toString() {
		return stubClass.toString();
	}
}
