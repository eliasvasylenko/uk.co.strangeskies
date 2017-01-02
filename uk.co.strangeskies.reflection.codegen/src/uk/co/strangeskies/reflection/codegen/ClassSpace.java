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
	}

	protected ClassSpace(
			ClassRegister classRegister,
			Map<ClassSignature<?>, ClassDeclaration<?, ?>> classDeclarations,
			Map<MethodDeclaration<?, ?>, MethodDefinition<?, ?>> methodDefinitions,
			boolean allowPartialImplementation,
			Set<MethodDeclaration<?, ?>> undefinedMethods) {
		this.classRegister = classRegister;
		this.classDeclarations = classDeclarations;
		this.methodDefinitions = methodDefinitions;
		this.allowPartialImplementation = allowPartialImplementation;
		this.undefinedMethods = undefinedMethods;
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
				undefinedMethods);
	}

	@SuppressWarnings("unchecked")
	public <T> ClassDeclaration<Void, T> getClassDeclaration(ClassSignature<T> classSignature) {
		return (ClassDeclaration<Void, T>) classDeclarations.get(classSignature);
	}

	public void validate() {
		undefinedMethods.stream().filter(m -> m.isConstructor() ).findAny().ifPresent(m -> {
			throw new CodeGenerationException(o -> o.mustImplementMethod(m));
		});
	}

	public boolean isFullyImplemented() {
		return undefinedMethods.isEmpty();
	}

	/**
	 * Is the class space not {@link #isFullyImplemented() fully implemented}, and
	 * is partial implementation {@link #asPartialImplementation(boolean) allowed}
	 * and possible.
	 * 
	 * @return true if the class space is partially implemented, false otherwise
	 */
	public boolean isPartiallyImplemented() {
		return allowPartialImplementation && !isFullyImplemented();
	}

	/**
	 * {@code partialImplementation} defaults to true.
	 *
	 * @see #asPartialImplementation(boolean)
	 */
	public ClassSpace asPartialImplementation() {
		return asPartialImplementation(true);
	}

	/**
	 * Derive a class space allowing for partial implementation.
	 * 
	 * <p>
	 * Partial implementation will attempt to still generate valid classes when
	 * some method implementations are not provided. This is achieved by providing
	 * default implementations to throw an error on invocation.
	 * 
	 * @param partialImplementation
	 *          true if partial implementation should be allowed, false otherwise
	 * @return the derived class space
	 */
	public ClassSpace asPartialImplementation(boolean partialImplementation) {
		return new ClassSpace(classRegister, classDeclarations, methodDefinitions, partialImplementation, undefinedMethods);
	}

	/**
	 * Create a new class loader with the given parent and load all class
	 * definitions into it.
	 * 
	 * @param parentClassLoader
	 * @return
	 */
	public ClassLoader loadClasses(ClassLoader parentClassLoader) {
		return loadClassesInto(new ClassLoader(parentClassLoader) {});
	}

	/**
	 * Inject all class definitions into the given class loader.
	 * 
	 * @param classLoader
	 * @return
	 */
	public ClassLoader loadClassesInto(ClassLoader classLoader) {
		return classLoader;
	}

	@SuppressWarnings("unchecked")
	public <C, T> MethodDefinition<C, T> getMethodDefinition(MethodDeclaration<C, T> override) {
		return (MethodDefinition<C, T>) methodDefinitions.get(override);
	}
}
