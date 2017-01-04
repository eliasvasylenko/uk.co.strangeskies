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

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static uk.co.strangeskies.reflection.codegen.ErasedMethodSignature.erasedMethodSignature;
import static uk.co.strangeskies.reflection.codegen.MethodDeclaration.declareMethod;
import static uk.co.strangeskies.reflection.token.ExecutableToken.overMethod;
import static uk.co.strangeskies.utilities.collection.StreamUtilities.streamOptional;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
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

		inheritMethods();

		overrideMethods();
	}

	private void inheritMethods() {
		concat(interfaceMethods(), classMethods()).filter(method -> !isStatic(method.getModifiers())).forEach(
				this::inheritMethod);
	}

	private Stream<Method> interfaceMethods() {
		return classDeclaration.getSuperTypes().flatMap(t -> t.getRawTypes()).flatMap(t -> stream(t.getMethods()));
	}

	private Stream<Method> classMethods() {
		return StreamUtilities.<Class<?>>iterate(classDeclaration.getSuperClass(), Class::getSuperclass).flatMap(
				c -> stream(c.getDeclaredMethods()));
	}

	private void overrideMethods() {
		classDeclaration.getSignature().getMethodSignatures().forEach(this::overrideMethod);

		methods.values().stream().forEach(MethodOverride::overrideIfNecessary);
	}

	protected ExecutableToken<?, ?> getInvocable(Method method) {
		ExecutableToken<?, ?> token = invocables
				.computeIfAbsent(method, m -> overMethod(method, classDeclaration.getSuperType()));

		return token;
	}

	protected void overrideMethod(MethodSignature<?> methodSignature) {
		MethodDeclaration<T, ?> methodDeclaration = declareMethod(classDeclaration, methodSignature);

		MethodOverride<T> override = methods
				.computeIfAbsent(methodDeclaration.getSignature().erased(), k -> new MethodOverride<>(this));

		override.override(methodDeclaration);
	}

	protected void inheritMethod(Method method) {
		ErasedMethodSignature overridingSignature = erasedMethodSignature(
				method.getName(),
				getInvocable(method).getParameters().map(ExecutableParameter::getType).map(Types::getRawType).toArray(
						Class<?>[]::new));

		MethodOverride<T> override = methods.computeIfAbsent(overridingSignature, k -> new MethodOverride<>(this));

		override.inherit(method);

		MethodOverride<T> mergeOverride = methods
				.put(erasedMethodSignature(method.getName(), method.getParameterTypes()), override);
		if (mergeOverride != null && mergeOverride != override) {
			for (Method mergeMethod : mergeOverride.getMethods()) {
				override.inherit(mergeMethod);
			}
		}
	}

	public Stream<MethodDeclaration<T, ?>> getDeclarations() {
		return methods.values().stream().flatMap(d -> streamOptional(d.getOverride())).distinct();
	}

	public Stream<ErasedMethodSignature> getSignatures() {
		return methods.keySet().stream();
	}

	public Stream<Map.Entry<ErasedMethodSignature, ? extends MethodDeclaration<T, ?>>> getSignatureDeclarations() {
		return methods.entrySet().stream().filter(e -> e.getValue().getOverride().isPresent()).map(
				e -> new SimpleEntry<>(e.getKey(), e.getValue().getOverride().get()));
	}

	public ClassDeclaration<?, T> getClassDeclaration() {
		return classDeclaration;
	}
}
