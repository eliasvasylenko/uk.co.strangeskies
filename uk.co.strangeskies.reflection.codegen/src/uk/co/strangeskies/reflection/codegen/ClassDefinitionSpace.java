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

import static uk.co.strangeskies.reflection.codegen.CodeGenerationException.CODEGEN_PROPERTIES;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * TODO generate byte code early for everything except method definitions!!!! at
 * this point all those parts should never change since they're derived only
 * from the immutable {@link ClassDeclaration}s rather than the
 * {@link ClassDefinition}s
 * 
 * ... By doing this we can load these proto-classes into a class loader and
 * reflect over them, so we get all the type checking provided by
 * {@link TypeToken} without support for special {@link Type}
 * implementations!!!!!
 * 
 * 
 * 
 * Think about how we can achieve clever re-definition abilities here by loading
 * existing classes into the class definition space and then modifying them.
 * Remember that we can then use instrumentation to reload the classes into the
 * same class loader if we really need to.
 * 
 * 
 * Load classes into ClassDefinitionSpace so we can reuse their methods: -
 * ClassDefinition.withMethodsFrom(Class) -
 * ClassDefinition.withMethodFrom(Class, String)
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * @author Elias N Vasylenko
 */
public class ClassDefinitionSpace {
	class ClassDeclarationContext {
		ClassDeclaration<?, ?> getClassDeclaration(String className) {
			return getClassDeclaration(classRegister.getClassSignature(className));
		}

		ClassDeclaration<?, ?> getClassDeclaration(ClassSignature<?> signature) {
			ClassDeclaration<?, ?> declaration = classDeclarations.get(signature);
			if (declaration == null) {
				declaration = new ClassDeclaration<>(this, signature);
			}
			return declaration;
		}

		void addClassDeclaration(ClassDeclaration<?, ?> declaration) {
			classDeclarations.put(declaration.getSignature(), declaration);
		}
	}

	private final ClassRegister classRegister;
	private final Map<ClassSignature<?>, ClassDeclaration<?, ?>> classDeclarations;

	private final Map<MethodDeclaration<?, ?>, MethodDefinition<?, ?>> methodDefinitions;
	private boolean allowPartialImplementation;
	private final Set<MethodDeclaration<?, ?>> undefinedMethods;

	private final ClassLoader parentClassLoader;
	private final ClassLoaderStrategy classLoadingStrategy;

	public ClassDefinitionSpace(ClassRegister classRegister) {
		this.classRegister = classRegister;

		classDeclarations = new HashMap<>();
		methodDefinitions = new HashMap<>();
		allowPartialImplementation = false;
		undefinedMethods = new HashSet<>();

		ClassDeclarationContext context = new ClassDeclarationContext();
		classRegister.getClassSignatures().forEach(signature -> {
			context
					.getClassDeclaration(signature)
					.methodDeclarations()
					.filter(m -> !methodDefinitions.keySet().contains(m))
					.forEach(undefinedMethods::add);
		});

		parentClassLoader = getClass().getClassLoader();
		classLoadingStrategy = ClassLoaderStrategy.DERIVE;
	}

	protected ClassDefinitionSpace(
			ClassRegister classRegister,
			Map<ClassSignature<?>, ClassDeclaration<?, ?>> classDeclarations,
			Map<MethodDeclaration<?, ?>, MethodDefinition<?, ?>> methodDefinitions,
			boolean allowPartialImplementation,
			Set<MethodDeclaration<?, ?>> undefinedMethods,
			ClassLoader parentClassLoader,
			ClassLoaderStrategy classLoadingStrategy) {
		this.classRegister = classRegister;
		this.classDeclarations = classDeclarations;
		this.methodDefinitions = methodDefinitions;
		this.allowPartialImplementation = allowPartialImplementation;
		this.undefinedMethods = undefinedMethods;
		this.parentClassLoader = parentClassLoader;
		this.classLoadingStrategy = classLoadingStrategy;
	}

	ClassDefinitionSpace withMethodDefinition(
			MethodDeclaration<?, ?> declaration,
			MethodDefinition<?, ?> definition) {
		Map<MethodDeclaration<?, ?>, MethodDefinition<?, ?>> methodDefinitions = new HashMap<>(
				this.methodDefinitions);
		methodDefinitions.put(declaration, definition);

		Set<MethodDeclaration<?, ?>> undefinedMethods = new HashSet<>(this.undefinedMethods);
		undefinedMethods.remove(declaration);

		return new ClassDefinitionSpace(
				classRegister,
				classDeclarations,
				methodDefinitions,
				allowPartialImplementation,
				undefinedMethods,
				parentClassLoader,
				classLoadingStrategy);
	}

	@SuppressWarnings("unchecked")
	public <T> ClassDeclaration<Void, T> getClassDeclaration(ClassSignature<T> classSignature) {
		return (ClassDeclaration<Void, T>) classDeclarations.get(classSignature);
	}

	public void validate() {
		undefinedMethods
				.stream()
				.filter(m -> m.isConstructor() || m.isStatic() || !m.isDefault())
				.findAny()
				.ifPresent(m -> {
					throw new CodeGenerationException(CODEGEN_PROPERTIES.mustImplementMethod(m));
				});
	}

	public boolean isFullyImplemented() {
		return undefinedMethods.isEmpty();
	}

	/**
	 * Is the class space not {@link #isFullyImplemented() fully implemented}, and
	 * is partial implementation {@link #withPartialImplementation(boolean)
	 * allowed} and possible.
	 * 
	 * @return true if the class space is partially implemented, false otherwise
	 */
	public boolean isPartiallyImplemented() {
		return allowPartialImplementation && !isFullyImplemented();
	}

	/**
	 * {@code partialImplementation} defaults to true.
	 *
	 * @see #withPartialImplementation(boolean)
	 */
	@SuppressWarnings("javadoc")
	public ClassDefinitionSpace asPartialImplementation() {
		return withPartialImplementation(true);
	}

	/**
	 * Derive a class space allowing for partial implementation.
	 * 
	 * <p>
	 * Partial implementation will attempt to still generate valid classes when
	 * some method implementations are not provided. This is achieved by providing
	 * default implementations to throw an error on invocation.
	 * 
	 * @param allowPartialImplementation
	 *          true if partial implementation should be allowed, false otherwise
	 * @return the derived class space
	 */
	public ClassDefinitionSpace withPartialImplementation(boolean allowPartialImplementation) {
		return new ClassDefinitionSpace(
				classRegister,
				classDeclarations,
				methodDefinitions,
				allowPartialImplementation,
				undefinedMethods,
				parentClassLoader,
				classLoadingStrategy);
	}

	/**
	 * Derive a new class space with the given parent class loader.
	 * 
	 * <p>
	 * To be able to load generated classes, this class loader should give access
	 * to all classes mentioned in the class definitions.
	 * 
	 * @param parentClassLoader
	 *          the parent class loader
	 * @return the derived class space
	 */
	public ClassDefinitionSpace withParentClassLoader(ClassLoader parentClassLoader) {
		return new ClassDefinitionSpace(
				classRegister,
				classDeclarations,
				methodDefinitions,
				allowPartialImplementation,
				undefinedMethods,
				parentClassLoader,
				classLoadingStrategy);
	}

	/**
	 * Derive a new class space with the given class loading strategy.
	 * 
	 * @param classLoadingStrategy
	 *          the class loading strategy
	 * @return the derived class space
	 */
	public ClassDefinitionSpace withClassLoadingStrategy(ClassLoaderStrategy classLoadingStrategy) {
		return new ClassDefinitionSpace(
				classRegister,
				classDeclarations,
				methodDefinitions,
				allowPartialImplementation,
				undefinedMethods,
				parentClassLoader,
				classLoadingStrategy);
	}

	public ClassDefinitionSpace generateClasses() {
		return this;
	}

	/**
	 * Generate the classes and load them into the runtime.
	 * 
	 * @return the class loader containing, or allowing the loading of, the
	 *         generated classes
	 */
	public ClassLoader loadClasses() {
		return parentClassLoader;
	}

	@SuppressWarnings("unchecked")
	public <C, T> MethodDefinition<C, T> getMethodDefinition(MethodDeclaration<C, T> override) {
		return (MethodDefinition<C, T>) methodDefinitions.get(override);
	}
}
