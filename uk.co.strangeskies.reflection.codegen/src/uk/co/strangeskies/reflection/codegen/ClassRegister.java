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
import static uk.co.strangeskies.reflection.codegen.MethodDeclaration.Kind.CONSTRUCTOR;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * TODO generate byte code early for everything except method definitions!!!! at
 * this point all those parts should never change since they're derived only
 * from the immutable {@link ClassDeclaration}s rather than the
 * {@link ClassDefinition}s
 * 
 * ... By doing this we can load these proto-classes into a class loader and
 * reflect over them, so we get all the type checking provided by
 * {@link TypeToken} without support for special {@link Type} implementations.
 * 
 * Loading stub-classes first then overriding with full implementations as a
 * two-step process means we can inject into existing class loaders without any
 * of the circularity restrictions present in other solutions.
 * 
 * Think about how we can achieve clever re-definition abilities here by loading
 * existing classes into the class definition space and then modifying them.
 * Remember that we can then use instrumentation to reload the classes into the
 * same class loader if we really need to.
 * 
 * Load classes into ClassDefinitionSpace so we can reuse their methods:
 * 
 * - ClassDefinition.withMethodsFrom(Class)
 * 
 * - ClassDefinition.withMethodFrom(Class, String)
 * 
 * @author Elias N Vasylenko
 */
public class ClassRegister {
	class ClassDeclarationContext {
		private final ClassLoader stubClassLoader = new ByteArrayClassLoader(getParentClassLoader());

		ClassDeclaration<?, ?> getClassDeclaration(String className) {
			return getClassDeclaration(getClassSignature(className));
		}

		ClassDeclaration<?, ?> getClassDeclaration(ClassSignature<?> signature) {
			return classDeclarations.computeIfAbsent(signature, s -> new ClassDeclaration<>(this, s));
		}

		void addClassDeclaration(ClassDeclaration<?, ?> declaration) {
			classDeclarations.put(declaration.getSignature(), declaration);
		}

		public ClassLoader getStubClassLoader() {
			return stubClassLoader;
		}
	}

	private final Map<ClassSignature<?>, ClassDeclaration<?, ?>> classDeclarations;

	private final Map<MethodDeclaration<?, ?>, MethodDefinition<?, ?>> methodDefinitions;
	private boolean allowPartialImplementation;
	private final Set<MethodDeclaration<?, ?>> undefinedMethods;

	private final ClassLoader parentClassLoader;
	private final ClassLoaderStrategy classLoaderStrategy;

	public ClassRegister() {
		this(ClassRegister.class.getClassLoader());
	}

	public ClassRegister(ClassLoader parentClassLoader) {
		this(parentClassLoader, ClassLoaderStrategy.DERIVE);
	}

	public ClassRegister(ClassLoader parentClassLoader, ClassLoaderStrategy classLoaderStrategy) {
		this.parentClassLoader = parentClassLoader;
		this.classLoaderStrategy = classLoaderStrategy;
		this.classDeclarations = new HashMap<>();
		this.methodDefinitions = new HashMap<>();
		this.allowPartialImplementation = false;
		this.undefinedMethods = new HashSet<>();
	}

	public <T> ClassDefinition<Void, T> withClassSignature(ClassSignature<T> classSignature) {
		ClassRegister register = withClassSignatures(classSignature);
		return new ClassDefinition<>(register.getClassDeclaration(classSignature), register);
	}

	public ClassRegister withClassSignatures(ClassSignature<?>... classSignatures) {
		Map<MethodDeclaration<?, ?>, MethodDefinition<?, ?>> methodDefinitions = this.methodDefinitions;
		Set<MethodDeclaration<?, ?>> undefinedMethods = this.undefinedMethods;

		ClassDeclarationContext context = new ClassDeclarationContext();
		Arrays.stream(classSignatures).forEach(signature -> {
			context
					.getClassDeclaration(signature)
					.methodDeclarations()
					.filter(m -> !methodDefinitions.keySet().contains(m))
					.forEach(undefinedMethods::add);
		});

		return new ClassRegister(
				classDeclarations,
				methodDefinitions,
				allowPartialImplementation,
				undefinedMethods,
				parentClassLoader,
				classLoaderStrategy);
	}

	protected ClassRegister(
			Map<ClassSignature<?>, ClassDeclaration<?, ?>> classDeclarations,
			Map<MethodDeclaration<?, ?>, MethodDefinition<?, ?>> methodDefinitions,
			boolean allowPartialImplementation,
			Set<MethodDeclaration<?, ?>> undefinedMethods,
			ClassLoader parentClassLoader,
			ClassLoaderStrategy classLoaderStrategy) {
		this.classDeclarations = classDeclarations;
		this.methodDefinitions = methodDefinitions;
		this.allowPartialImplementation = allowPartialImplementation;
		this.undefinedMethods = undefinedMethods;
		this.parentClassLoader = parentClassLoader;
		this.classLoaderStrategy = classLoaderStrategy;
	}

	ClassRegister withMethodDefinition(
			MethodDeclaration<?, ?> declaration,
			MethodDefinition<?, ?> definition) {
		Map<MethodDeclaration<?, ?>, MethodDefinition<?, ?>> methodDefinitions = new HashMap<>(
				this.methodDefinitions);
		methodDefinitions.put(declaration, definition);

		Set<MethodDeclaration<?, ?>> undefinedMethods = new HashSet<>(this.undefinedMethods);
		undefinedMethods.remove(declaration);

		return new ClassRegister(
				classDeclarations,
				methodDefinitions,
				allowPartialImplementation,
				undefinedMethods,
				parentClassLoader,
				classLoaderStrategy);
	}

	public ClassLoader getParentClassLoader() {
		return parentClassLoader;
	}

	public ClassLoaderStrategy getClassLoaderStrategy() {
		return classLoaderStrategy;
	}

	private ClassSignature<?> getClassSignature(String className) {
		/*
		 * TODO Auto-generated method stub
		 * 
		 * replace with something akin to TypeVariableSignature.Reference for Types?
		 * 
		 * or a more general type look-up thing by string
		 */
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> ClassDeclaration<Void, T> getClassDeclaration(ClassSignature<T> classSignature) {
		return (ClassDeclaration<Void, T>) classDeclarations.get(classSignature);
	}

	public void validate() {
		undefinedMethods
				.stream()
				.filter(
						m -> m.getKind().equals(CONSTRUCTOR)
								|| m.getSignature().getModifiers().isStatic()
								|| !m.getSignature().getModifiers().isDefault())
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
	public ClassRegister asPartialImplementation() {
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
	public ClassRegister withPartialImplementation(boolean allowPartialImplementation) {
		return new ClassRegister(
				classDeclarations,
				methodDefinitions,
				allowPartialImplementation,
				undefinedMethods,
				parentClassLoader,
				classLoaderStrategy);
	}

	public ClassRegister generateClasses() {
		return this;
	}

	/**
	 * Generate the classes and load them into the runtime.
	 * 
	 * @return the class loader containing, or allowing the loading of, the
	 *         generated classes
	 */
	public ClassLoader loadClasses() {
		return getParentClassLoader();
	}

	@SuppressWarnings("unchecked")
	public <C, T> MethodDefinition<C, T> getMethodDefinition(MethodDeclaration<C, T> override) {
		return (MethodDefinition<C, T>) methodDefinitions.get(override);
	}
}
