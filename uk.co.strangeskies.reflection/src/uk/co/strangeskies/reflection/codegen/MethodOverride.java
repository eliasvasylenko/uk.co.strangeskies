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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.Types;

public class MethodOverride<T> {
	private final ClassDefinition<T> classDefinition;
	private final MethodSignature signature;
	private final Set<Method> interfaceMethods;
	private Method classMethod;
	private InstanceMethodDefinition<T, ?> override;

	public MethodOverride(ClassDefinition<T> classDefinition, MethodSignature signature) {
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
				Set<Method> defaultMethods = interfaceMethods.stream().filter(m -> m.isDefault()).collect(Collectors.toSet());
				if (defaultMethods.size() != 1) {
					throw new CodeGenerationException(p -> p.mustOverrideMethods(interfaceMethods));
				}

				Method defaultMethod = defaultMethods.iterator().next();

				validateOverride(defaultMethod);
			} else {
				if (Modifier.isAbstract(classMethod.getModifiers())) {
					throw new CodeGenerationException(p -> p.mustOverrideMethods(Arrays.asList(classMethod)));
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
				throw new CodeGenerationException(p -> p.incompatibleReturnTypes(returnType, inherited));
			}

			for (int i = 0; i < parameterTypes.length; i++) {
				if (!Types.isAssignable(classDefinition.getInvocable(inherited).getParameters().get(i), parameterTypes[i])) {
					throw new CodeGenerationException(p -> p.incompatibleParameterTypes(parameterTypes, inherited));
				}
			}
		}
	}

	public void override(InstanceMethodDefinition<T, ?> override) {
		if (this.override != null) {
			throw new CodeGenerationException(p -> p.duplicateMethodSignature(override));
		}

		if (classMethod != null
				&& (Modifier.isPrivate(classMethod.getModifiers()) || Modifier.isFinal(classMethod.getModifiers()))) {
			throw new CodeGenerationException(p -> p.cannotOverrideMethod(classMethod));
		}

		this.override = override;
		validateOverride(override.getReturnType().getType(),
				override.getParameters().stream().map(v -> v.getType().getType()).toArray(Type[]::new));
	}

	public Optional<InstanceMethodDefinition<T, ?>> getOverride() {
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
