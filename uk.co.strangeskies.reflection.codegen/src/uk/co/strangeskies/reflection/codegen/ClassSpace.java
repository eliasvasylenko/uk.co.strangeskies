package uk.co.strangeskies.reflection.codegen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassSpace {
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
	private final ClassLoadingStrategy classLoadingStrategy;

	public ClassSpace(ClassRegister classRegister) {
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
		classLoadingStrategy = ClassLoadingStrategy.DERIVE;
	}

	protected ClassSpace(
			ClassRegister classRegister,
			Map<ClassSignature<?>, ClassDeclaration<?, ?>> classDeclarations,
			Map<MethodDeclaration<?, ?>, MethodDefinition<?, ?>> methodDefinitions,
			boolean allowPartialImplementation,
			Set<MethodDeclaration<?, ?>> undefinedMethods,
			ClassLoader parentClassLoader,
			ClassLoadingStrategy classLoadingStrategy) {
		this.classRegister = classRegister;
		this.classDeclarations = classDeclarations;
		this.methodDefinitions = methodDefinitions;
		this.allowPartialImplementation = allowPartialImplementation;
		this.undefinedMethods = undefinedMethods;
		this.parentClassLoader = parentClassLoader;
		this.classLoadingStrategy = classLoadingStrategy;
	}

	ClassSpace withMethodDefinition(MethodDeclaration<?, ?> declaration, MethodDefinition<?, ?> definition) {
		Map<MethodDeclaration<?, ?>, MethodDefinition<?, ?>> methodDefinitions = new HashMap<>(this.methodDefinitions);
		methodDefinitions.put(declaration, definition);

		Set<MethodDeclaration<?, ?>> undefinedMethods = new HashSet<>(this.undefinedMethods);
		undefinedMethods.remove(declaration);

		return new ClassSpace(
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
		undefinedMethods.stream().filter(m -> m.isConstructor() || m.isStatic() || !m.isDefault()).findAny().ifPresent(
				m -> {
					throw new CodeGenerationException(o -> o.mustImplementMethod(m));
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
	public ClassSpace asPartialImplementation() {
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
	public ClassSpace withPartialImplementation(boolean allowPartialImplementation) {
		return new ClassSpace(
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
	public ClassSpace withParentClassLoader(ClassLoader parentClassLoader) {
		return new ClassSpace(
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
	public ClassSpace withClassLoadingStrategy(ClassLoadingStrategy classLoadingStrategy) {
		return new ClassSpace(
				classRegister,
				classDeclarations,
				methodDefinitions,
				allowPartialImplementation,
				undefinedMethods,
				parentClassLoader,
				classLoadingStrategy);
	}

	public ClassSpace generateClasses() {
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
