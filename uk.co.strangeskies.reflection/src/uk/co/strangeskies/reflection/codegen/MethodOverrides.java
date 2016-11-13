package uk.co.strangeskies.reflection.codegen;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static uk.co.strangeskies.reflection.codegen.MethodDeclaration.declareMethod;
import static uk.co.strangeskies.reflection.token.ExecutableToken.overMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.token.ExecutableParameter;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.utilities.collection.StreamUtilities;

public class MethodOverrides<T> {
	private final ClassDeclaration<?, T> classDeclaration;
	private final Map<Method, ExecutableToken<?, ?>> invocables;
	private final Map<ErasedMethodSignature, MethodOverride<T>> methods;

	public MethodOverrides(ClassDeclaration<?, T> classDeclaration) {
		this.classDeclaration = classDeclaration;
		this.invocables = new HashMap<>();
		this.methods = new HashMap<>();

		concat(interfaceMethods(), classMethods())
				.filter(method -> !isStatic(method.getModifiers()))
				.forEach(this::inheritMethod);

		classDeclaration.getSignature().getMethodSignatures().forEach(this::overrideMethod);

		methods.values().stream().forEach(MethodOverride::overrideIfNecessary);
	}

	private Stream<Method> interfaceMethods() {
		return classDeclaration.getSuperTypes().flatMap(t -> t.getRawTypes().stream()).flatMap(t -> stream(t.getMethods()));
	}

	private Stream<Method> classMethods() {
		return StreamUtilities.<Class<?>>iterate(classDeclaration.getSuperClass(), Class::getSuperclass).flatMap(
				c -> stream(c.getDeclaredMethods()));
	}

	protected ExecutableToken<?, ?> getInvocable(Method method) {
		ExecutableToken<?, ?> token = invocables
				.computeIfAbsent(method, m -> overMethod(method, classDeclaration.getSuperType()));

		return token;
	}

	protected void overrideMethod(MethodSignature<?> methodSignature) {
		MethodDeclaration<T, ?> methodDeclaration = declareMethod(classDeclaration, methodSignature);

		MethodOverride<T> override = methods
				.computeIfAbsent(methodDeclaration.getErasedSignature(), k -> new MethodOverride<>(this, k));

		override.override(methodDeclaration);
	}

	protected void inheritMethod(Method method) {
		ErasedMethodSignature overridingSignature = new ErasedMethodSignature(
				method.getName(),
				getInvocable(method).getParameters().stream().map(ExecutableParameter::getType).map(Types::getRawType).toArray(
						Class<?>[]::new));

		MethodOverride<T> override = methods
				.computeIfAbsent(overridingSignature, k -> new MethodOverride<>(this, overridingSignature));

		override.inherit(method);

		/*
		 * The actual erased method signature before parameterization of the
		 * declaring class may be different, in which case it would be overridden by
		 * a synthetic bridge method.
		 */
		methods.put(new ErasedMethodSignature(method), override);
	}

	public Stream<? extends MethodDeclaration<T, ?>> getDeclarations() {
		return methods.values().stream().map(d -> d.getOverride()).filter(Optional::isPresent).map(Optional::get);
	}

	public ClassDeclaration<?, T> getClassDeclaration() {
		return classDeclaration;
	}
}
