/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen;

import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Arrays.asList;
import static uk.co.strangeskies.reflection.Types.isAssignable;
import static uk.co.strangeskies.reflection.codegen.MethodDeclaration.declareMethod;
import static uk.co.strangeskies.reflection.codegen.MethodSignature.methodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.token.ExecutableParameter;
import uk.co.strangeskies.reflection.token.ExecutableToken;

public class MethodOverride<T> {
	private final MethodOverrides<T> methodOverrides;

	private final Set<Method> interfaceMethods;
	private Method classMethod;

	private MethodDeclaration<T, ?> override;

	public MethodOverride(MethodOverrides<T> methodOverrides) {
		this.methodOverrides = methodOverrides;
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
		if (classMethod == null) {
			Set<Method> defaultMethods = interfaceMethods.stream().filter(m -> m.isDefault()).collect(Collectors.toSet());
			if (defaultMethods.size() > 1) {
				throw new CodeGenerationException(p -> p.mustOverrideMethods(interfaceMethods));
			} else if (defaultMethods.isEmpty()) {
				for (Method interfaceMethod : interfaceMethods) {
					try {
						validateOverride(interfaceMethod);
						return;
					} catch (Exception e) {}
				}
				throw new CodeGenerationException(p -> p.mustOverrideMethods(interfaceMethods));
			} else {
				Method defaultMethod = defaultMethods.iterator().next();

				validateOverride(defaultMethod);
			}
		} else {
			if (isAbstract(classMethod.getModifiers())) {
				throw new CodeGenerationException(p -> p.mustOverrideMethods(Arrays.asList(classMethod)));
			}

			validateOverride(classMethod);
		}
	}

	private void validateOverride(Method method) {
		ExecutableToken<?, ?> overriddenToken = methodOverrides.getInvocable(method).withTypeArguments(
				method.getTypeParameters());

		getMethods().stream().filter(m -> m != method).forEach(
				m -> validateOverrideAgainst(
						overriddenToken.getReturnType().getType(),
						overriddenToken.getParameters().map(ExecutableParameter::getType).toArray(Type[]::new),
						method.getTypeParameters(),
						m));
	}

	private void validateOverrideAgainst(
			Type returnType,
			Type[] parameters,
			TypeVariable<?>[] typeParameters,
			Method inherited) {
		ExecutableToken<?, ?> inheritedToken = methodOverrides.getInvocable(inherited).withTypeArguments(
				asList(typeParameters));

		if (!isAssignable(returnType, inheritedToken.getReturnType().resolve().getType())) {
			throw new CodeGenerationException(p -> p.incompatibleReturnTypes(returnType, inherited));
		}

		for (int i = 0; i < parameters.length; i++) {
			if (!isAssignable(inheritedToken.getParameters().skip(i).findFirst().get().getType(), parameters[i])) {
				throw new CodeGenerationException(p -> p.incompatibleParameterTypes(parameters, inherited));
			}
		}
	}

	private void validateOverride() {
		for (Method inherited : getMethods()) {
			validateOverrideAgainst(
					override.getReturnType().getType(),
					override.getParameters().map(v -> v.getType().getType()).toArray(Type[]::new),
					override.getTypeParameters(),
					inherited);
		}
	}

	public void override(MethodDeclaration<T, ?> override) {
		if (this.override != null) {
			throw new CodeGenerationException(p -> p.duplicateMethodDeclaration(override));
		}

		if (classMethod != null
				&& (Modifier.isPrivate(classMethod.getModifiers()) || Modifier.isFinal(classMethod.getModifiers()))) {
			throw new CodeGenerationException(p -> p.cannotOverrideMethod(classMethod));
		}

		this.override = override;
		validateOverride();
	}

	public void overrideIfNecessary() {
		boolean classMethodMustBeOverridden = classMethod == null || isAbstract(classMethod.getModifiers());
		boolean interfaceMethodsMustBeOverridden = classMethod == null
				&& interfaceMethods.stream().allMatch(i -> !i.isDefault());

		if (this.override == null) {
			if (classMethodMustBeOverridden || interfaceMethodsMustBeOverridden) {
				Method overrideSignatureMethod = classMethod;
				if (overrideSignatureMethod == null) {
					overrideSignatureMethod = interfaceMethods.iterator().next();
				}

				ExecutableToken<?, ?> executableToken = methodOverrides.getInvocable(overrideSignatureMethod).withTypeArguments(
						asList(overrideSignatureMethod.getTypeParameters()));

				MethodSignature<?> signature = methodSignature((Method) executableToken.getMember());

				MethodDeclaration<T, ?> declaration = declareMethod(methodOverrides.getClassDeclaration(), signature);

				override(declaration);
			} else {
				validate();
			}
		}
	}

	public Optional<MethodDeclaration<T, ?>> getOverride() {
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
		if (classMethod != null) {
			methods.add(classMethod);
		}
		methods.addAll(interfaceMethods);
		return methods;
	}

	@Override
	public String toString() {
		return getMethods() + " : " + getOverride();
	}
}