package uk.co.strangeskies.reflection;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPrivate;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MethodOverride {
	private final ClassDefinition<?> classDefinition;
	private final MethodOverrideSignature signature;
	private final Set<Method> interfaceMethods;
	private Method classMethod;
	private MethodDefinition<?, ?> override;

	public MethodOverride(ClassDefinition<?> classDefinition, MethodOverrideSignature signature) {
		this.classDefinition = classDefinition;
		this.signature = signature;
		interfaceMethods = new HashSet<>();
	}

	public void inherit(Method method) {
		if (method.getDeclaringClass().isInterface()) {
			interfaceMethods.add(method);
		} else {
			classMethod = method;
		}
	}

	public void validate() {
		if (override == null) {
			if (classMethod == null) {
				if (!interfaceMethods.isEmpty()) {
					Set<Method> defaultMethods = interfaceMethods.stream().filter(m -> m.isDefault()).collect(Collectors.toSet());
					if (defaultMethods.size() != 1) {
						throw new ReflectionException(p -> p.mustOverrideMethods(interfaceMethods));
					}

					Method defaultMethod = defaultMethods.iterator().next();

					validateOverride(defaultMethod);
				}
			} else {
				if (Modifier.isAbstract(classMethod.getModifiers())) {
					throw new ReflectionException(p -> p.mustOverrideMethods(Arrays.asList(classMethod)));
				}

				validateOverride(classMethod);
			}
		} else {
			override.validate();
		}
	}

	private void validateOverride(Method method) {
		validateOverride(classDefinition.getInvocable(method).getReturnType().getType(),
				classDefinition.getInvocable(method).getParameters().stream().toArray(Type[]::new));
	}

	private void validateOverride(Type returnType, Type[] parameterTypes) {
		for (Method inherited : getMethods()) {

			if (!Types.isAssignable(returnType,
					classDefinition.getInvocable(inherited).getReturnType().resolve().getType())) {
				throw new ReflectionException(p -> p.incompatibleReturnTypes(returnType, inherited));
			}

			for (int i = 0; i < parameterTypes.length; i++) {
				if (!Types.isAssignable(classDefinition.getInvocable(inherited).getParameters().get(i), parameterTypes[i])) {
					throw new ReflectionException(p -> p.incompatibleParameterTypes(parameterTypes, inherited));
				}
			}
		}
	}

	public void override(MethodDefinition<?, ?> override) {
		if (this.override != null) {
			throw new ReflectionException(p -> p.duplicateMethodSignature(override));
		}

		if (classMethod != null && (isPrivate(classMethod.getModifiers()) || isFinal(classMethod.getModifiers()))) {
			throw new ReflectionException(p -> p.cannotOverrideMethod(classMethod));
		}

		this.override = override;
		validateOverride(override.getReturnType().getType(),
				override.getParameters().stream().map(v -> v.getType().getType()).toArray(Type[]::new));
	}

	public Optional<MethodDefinition<?, ?>> getOverride() {
		return Optional.ofNullable(override);
	}

	public Optional<Method> getClassMethod() {
		return Optional.ofNullable(classMethod);
	}

	public Set<Method> getInterfaceMethods() {
		return interfaceMethods;
	}

	public Set<Method> getMethods() {
		HashSet<Method> methods = new HashSet<>(interfaceMethods.size() + 1);
		methods.addAll(interfaceMethods);
		if (classMethod != null) {
			methods.add(classMethod);
		}
		return methods;
	}
}
