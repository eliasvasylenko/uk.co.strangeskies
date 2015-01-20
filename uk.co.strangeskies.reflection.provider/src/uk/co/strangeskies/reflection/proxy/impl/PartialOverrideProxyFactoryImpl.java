/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.proxy.impl;

import java.lang.reflect.Method;
import java.util.function.Function;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.reflection.proxy.PartialOverrideProxyFactory;

public class PartialOverrideProxyFactoryImpl implements
		PartialOverrideProxyFactory {
	@Override
	public <T> Function<T, T> create(Class<T> baseClass,
			Class<? extends T> partialImplementation) {
		return create(baseClass, base -> partialImplementation);
	}

	@Override
	public <T> Function<T, T> create(Class<T> baseClass,
			Function<T, Class<? extends T>> partialImplementationFactory) {
		return create(new TypeLiteral<>(baseClass), partialImplementationFactory);
	}

	@Override
	public <T> Function<T, T> create(TypeLiteral<T> baseClass,
			Class<? extends T> partialImplementation) {
		return create(baseClass, base -> partialImplementation);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Function<T, T> create(TypeLiteral<T> baseClass,
			Function<T, Class<? extends T>> partialImplementationFactory) {
		return base -> {
			Enhancer e = new Enhancer();
			e.setSuperclass(partialImplementationFactory.apply(base));
			e.setCallback(new MethodInterceptor() {
				@Override
				public Object intercept(Object obj, Method method, Object[] args,
						MethodProxy proxy) throws Throwable {
					try {
						return proxy.invokeSuper(obj, args);
					} catch (AbstractMethodError e) {
						return method.invoke(base, args);
					}
				}
			});

			try {
				return (T) e.create();
			} catch (IllegalArgumentException ex) {
				return (T) e.create(new Class[] { baseClass.getRawType() },
						new Object[] { base });
			}
		};
	}

	public static void main(String... args) {
		abstract class TestInterface {
			public abstract String firstMethod();

			public abstract String secondMethod();
		}

		TestInterface base = new TestInterface() {
			@Override
			public String secondMethod() {
				return "firstBase";
			}

			@Override
			public String firstMethod() {
				return "secondBase";
			}
		};

		abstract class O extends TestInterface {
			@Override
			public String firstMethod() {
				return "firstOverride -> " + base.firstMethod();
			}
		}
		TestInterface override = new PartialOverrideProxyFactoryImpl().create(
				TestInterface.class, O.class).apply(base);

		System.out.println(override.firstMethod() + ", " + override.secondMethod());
	}
}
