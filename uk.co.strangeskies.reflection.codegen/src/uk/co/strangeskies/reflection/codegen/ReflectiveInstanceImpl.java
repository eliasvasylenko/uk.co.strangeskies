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

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This interface is not for outside consumption. It allows instances of
 * {@link ClassDefinition class definitions} which are created reflectively to
 * simulate normal class state by providing access to {@link FieldDeclaration
 * defined fields}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the actual type of the instance
 */
public class ReflectiveInstanceImpl<E, T> implements ReflectiveInstance<E, T> {
	private final ClassDefinition<E, T> classDefinition;

	private T instance;
	private final Map<FieldDeclaration<?, ?>, Object> fieldValues = new HashMap<>();

	private ReflectiveInstanceImpl(ClassDefinition<E, T> classDefinition) {
		this.classDefinition = classDefinition;
	}

	@Override
	public ClassDefinition<E, T> getClassDefinition() {
		return classDefinition;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> U getReflectiveFieldValue(FieldDeclaration<? super T, U> field) {
		return (U) fieldValues.get(field);
	}

	@Override
	public <U> void setReflectiveFieldValue(FieldDeclaration<? super T, U> field, U value) {
		fieldValues.put(field, value);
	}

	@Override
	public T cast() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	static <E, T> ReflectiveInstance<E, T> instantiate(
			ClassDefinition<E, T> classDefinition,
			ClassLoader classLoader,
			Collection<? extends Object> arguments) {
		classDefinition.getClassSpace().validate();

		Set<Class<?>> rawTypes = classDefinition.getDeclaration().getSuperTypes().flatMap(t -> t.getRawTypes()).collect(
				Collectors.toCollection(LinkedHashSet::new));

		for (Class<?> rawType : rawTypes) {
			if (!rawType.isInterface()) {
				throw new CodeGenerationException(
						p -> p.cannotInstantiateClassDefinition(classDefinition, classDefinition.getDeclaration().getSuperType()));
			}
		}

		rawTypes.add(ReflectiveInstance.class);
		ReflectiveInstanceImpl<E, T> reflectiveInstance = new ReflectiveInstanceImpl<E, T>(classDefinition);

		ReflectiveInstance<E, T> instance = (ReflectiveInstance<E, T>) Proxy
				.newProxyInstance(classLoader, rawTypes.toArray(new Class<?>[rawTypes.size()]), (proxy, method, args) -> {
					if (method.getDeclaringClass().equals(ReflectiveInstance.class)
							|| method.getDeclaringClass().equals(Object.class)) {
						return method.invoke(reflectiveInstance, args);
					}

					MethodDeclaration<T, ?> override = classDefinition
							.getDeclaration()
							.getMethodDeclaration(method.getName(), method.getParameterTypes());

					StatementExecutor executor = new StatementExecutor((ReflectiveInstance<E, T>) proxy);

					return classDefinition.getClassSpace().getMethodDefinition(override).invoke(executor, args);
				});
		reflectiveInstance.instance = (T) instance;

		return instance;
	}
}
