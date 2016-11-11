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

import static java.util.Arrays.asList;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.codegen.ExpressionVisitor.ValueExpressionVisitor;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.utilities.collection.StreamUtilities;

/**
 * A class definition is a description of a class implementation. It may extend
 * an existing class, and implement existing interfaces, as well as provide
 * {@link MethodDefinition method implementations} and overrides for those
 * supertypes.
 * 
 * <p>
 * A class definition may be implemented reflectively via {@link Proxy},
 * delegating invocations to direct evaluation of {@link MethodDefinition method
 * definition bodies}, and the{@link Statement statements} therein. A class
 * definition also contains enough information to generate the bytecode for a
 * true implementation, with performance comparable to a regular compiled Java
 * class, though this is not yet implemented.
 * 
 * <p>
 * Aside from this than this, there are currently a number of significant
 * limitations to class definitions:
 * 
 * <ul>
 * <li>Though supertypes may be parameterized generic types, it is not supported
 * for the class definition itself to be a generic type.</li>
 * 
 * <li>It is not supported to declare new methods which are not simply overrides
 * of existing methods. Variance is technically supported where appropriate in
 * method declarations, but the information is not available reflectively so
 * this provides limited utility.</li>
 * 
 * <li>It is possible to define an implementation for a default constructor if
 * necessary, but it is not supported to define constructors with
 * arguments.</li>
 * </ul>
 * 
 * <p>
 * These limitations help to keep class definitions simple, as their type can be
 * effectively reflected over via {@link TypeToken} with no special
 * consideration. Also the need for interdependencies between class definitions
 * should be largely avoided.
 * 
 * <p>
 * There are a few potential paths to evolving this class beyond some or all of
 * these limitations ... But they are likely to require significant work, and
 * may challenge some pervasive assumptions made by the existing library design,
 * for example that the raw type of any type is a {@link Class}.
 * 
 * <p>
 * Every implementation generated via a class definition also be cast to
 * {@link Reified} to reflect over its exact type. The implementation of all
 * relevant methods will be generated automatically <em>where possible</em>. If
 * the {@link Reified} class is overridden explicitly however, or if any method
 * in {@link Reified} is shadowed by an explicitly overridden class, the user
 * should provide their own implementations for these methods.
 * 
 * Note that if supported is added for generation of generic classes in the
 * future, the exact type of such classes may not be determined statically so
 * they will not implement reified by default.
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * TODO model the type using existing types so it can participate in inference
 * etc.
 * 
 * each definition holds a secret type variable capture with no bounds
 * 
 * For a non-generic type this is simple enough: model as the intersection type
 * of all the super types of the definition and the secret capture
 * 
 * For a generic type, the raw can be modeled as the intersection type of all
 * the raw types of all the super types of the definition and the secret capture
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          the intersection of the supertypes of the described class
 */
public class ClassDefinition<T> {
	class ReflectiveInstanceImpl implements ReflectiveInstance<T> {
		private T instance;
		private final Map<FieldDefinition<?, ?>, Object> fieldValues = new HashMap<>();

		@Override
		public ClassDefinition<T> getReflectiveClassDefinition() {
			return ClassDefinition.this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <U> U getReflectiveFieldValue(FieldDefinition<? super T, U> field) {
			return (U) fieldValues.get(field);
		}

		@Override
		public <U> void setReflectiveFieldValue(FieldDefinition<? super T, U> field, U value) {
			fieldValues.put(field, value);
		}

		@Override
		public T cast() {
			return instance;
		}
	}

	private final String typeName;

	private final Map<Method, ExecutableToken<?, ?>> invocables;
	private final Map<ErasedMethodSignature, MethodOverride<T>> methods;

	private final ValueExpression<T> receiverExpression;

	@SuppressWarnings("unchecked")
	protected ClassDefinition(ClassSignature<? super T> signature) {
		typeName = signature.getTypeName();

		invocables = new HashMap<>();
		methods = new HashMap<>();
		getSuperTypes()
				.flatMap(t -> t.getRawTypes().stream())
				.flatMap(t -> stream(t.getMethods()))
				.forEach(this::inheritMethod);
		StreamUtilities
				.<Class<?>> iterate(getSuperClass(), Class::getSuperclass)
				.flatMap(c -> stream(c.getDeclaredMethods()))
				.forEach(this::inheritMethod);

		this.receiverExpression = new ValueExpression<T>() {
			@Override
			public void accept(ValueExpressionVisitor<T> visitor) {
				visitor.visitReceiver(ClassDefinition.this);
			};

			@Override
			public TypeToken<T> getType() {
				/*
				 * TODO this needs to be the actual Type
				 */
				return (TypeToken<T>) getSuperType();
			}
		};
	}

	/**
	 * @return the fully qualified class name
	 */
	public String getName() {
		return typeName;
	}

	/**
	 * Verify that the class definition describes a valid and complete class such
	 * that implementation/compilation or instantiation is possible.
	 */
	public void validate() {
		/*
		 * TODO check we have a default super constructor, or a valid constructor
		 * defined explicitly.
		 */

		for (MethodOverride<T> override : methods.values()) {
			override.validate();
		}
	}

	public ReflectiveInstance<T> instantiate(Object... arguments) {
		return instantiate(getClass().getClassLoader(), arguments);
	}

	public ReflectiveInstance<T> instantiate(Collection<? extends Object> arguments) {
		return instantiate(getClass().getClassLoader(), arguments);
	}

	public ReflectiveInstance<T> instantiate(ClassLoader classLoader, Object... arguments) {
		return instantiate(classLoader, Arrays.asList(arguments));
	}

	@SuppressWarnings("unchecked")
	public ReflectiveInstance<T> instantiate(ClassLoader classLoader, Collection<? extends Object> arguments) {
		validate();

		Set<Class<?>> rawTypes = superTypes
				.stream()
				.flatMap(t -> t.getRawTypes().stream())
				.collect(Collectors.toCollection(LinkedHashSet::new));

		for (Class<?> rawType : rawTypes) {
			if (!rawType.isInterface()) {
				throw new CodeGenerationException(p -> p.cannotInstantiateClassDefinition(this, superType));
			}
		}

		rawTypes.add(ReflectiveInstance.class);
		ReflectiveInstanceImpl reflectiveInstance = new ReflectiveInstanceImpl();

		ReflectiveInstance<T> instance = (ReflectiveInstance<T>) Proxy.newProxyInstance(classLoader,
				rawTypes.toArray(new Class<?>[rawTypes.size()]), (proxy, method, args) -> {
					if (method.getDeclaringClass().equals(ReflectiveInstance.class)) {
						return method.invoke(reflectiveInstance, args);
					}

					MethodOverride<T> override = methods.get(new ErasedMethodSignature(method));

					if (override.getOverride().isPresent()) {
						return override.getOverride().get().invoke((ReflectiveInstance<T>) proxy, args);
					} else {
						try {
							return override.getInterfaceMethods().stream().filter(Method::isDefault).findAny().get().invoke(proxy,
									args);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							throw new ReflectionException(
									p -> p.invalidMethodArguments(method, getSuperType().getType(), asList(args)));
						}
					}
				});
		reflectiveInstance.instance = (T) instance;

		return instance;
	}

	public ValueExpression<T> receiver() {
		return receiverExpression;
	}

	@Override
	public String toString() {
		return getName();
	}

	public FieldSignature<T, ?> declareField(String fieldName, AnnotatedType type) {
		return new FieldSignature<>(this, fieldName, type);
	}

	public FieldSignature<T, ?> declareField(String fieldName, Type type) {
		return declareField(fieldName, AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> FieldSignature<T, U> declareField(String fieldName, Class<U> type) {
		return (FieldSignature<T, U>) declareField(fieldName, AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> FieldSignature<T, U> declareField(String fieldName, TypeToken<U> type) {
		return (FieldSignature<T, U>) declareField(fieldName, type.getAnnotatedDeclaration());
	}

	public MethodSignature<T, Void> declareMethod(String methodName) {
		return MethodSignature.declareMethod(this, methodName);
	}

	public MethodSignature<Void, T> declareConstructor() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public MethodSignature<T, Void> declareMethodOverride(Method method) {
		return declareMethod(method.getName())
				.withParameters(AnnotatedTypes.over(ExecutableToken.overMethod(method).getParameters()));
	}

	@SuppressWarnings("unchecked")
	public MethodSignature<T, Void> declareMethodOverride(Consumer<? super T> methodLambda) {
		ExecutableToken<?, ?> overridden = getSuperType().findInterfaceMethod((Consumer<Object>) methodLambda);

		return declareMethod(overridden.getName()).withParameters(AnnotatedTypes.over(overridden.getParameters()));
	}

	protected void overrideMethod(InstanceMethodDefinition<T, ?> methodDefinition) {
		MethodOverride<T> override = methods.computeIfAbsent(methodDefinition.getOverrideSignature(),
				k -> new MethodOverride<>(this, methodDefinition.getOverrideSignature()));

		override.override(methodDefinition);
	}

	protected void inheritMethod(Method method) {
		if (!Modifier.isStatic(method.getModifiers())) {
			ErasedMethodSignature overridingSignature = new ErasedMethodSignature(method.getName(),
					getInvocable(method).getParameters().stream().map(Types::getRawType).toArray(Class<?>[]::new));

			MethodOverride<T> override = methods.computeIfAbsent(overridingSignature,
					k -> new MethodOverride<>(this, overridingSignature));

			override.inherit(method);

			/*
			 * The actual erased method signature may be different, in which case it
			 * would be overridden by a synthetic bridge method.
			 */
			methods.put(new ErasedMethodSignature(method), override);
		}
	}

	protected ExecutableToken<?, ?> getInvocable(Method method) {
		return invocables.computeIfAbsent(method, m -> ExecutableToken.overMethod(method, superType));
	}
}
