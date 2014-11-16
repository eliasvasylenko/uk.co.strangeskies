package uk.co.strangeskies.utilities.proxy;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.osgi.service.component.annotations.Component;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class PartialOverrideProxyFactory<T> {
	private final Class<? extends T> baseClass;
	private final Function<T, Class<? extends T>> partialImplementationFactory;

	public PartialOverrideProxyFactory(Class<? extends T> baseClass,
			Class<? extends T> partialImplementation) {
		this.baseClass = baseClass;
		this.partialImplementationFactory = s -> partialImplementation;
	}

	public PartialOverrideProxyFactory(Class<? extends T> baseClass,
			Function<T, Class<? extends T>> partialImplementationFactory) {
		this.baseClass = baseClass;
		this.partialImplementationFactory = partialImplementationFactory;
	}

	@SuppressWarnings("unchecked")
	public T override(T base) {
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
			return (T) e.create(new Class[] { baseClass }, new Object[] { base });
		}
	}

	public static void main(String... args) {
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

		abstract class O implements TestInterface {
			public String firstMethod() {
				return "firstOverride -> " + base.firstMethod();
			}
		}
		TestInterface override = new PartialOverrideProxyFactory<>(
				TestInterface.class, O.class).override(base);

		System.out.println(override.firstMethod() + ", " + override.secondMethod());
	}

	interface TestInterface {
		String firstMethod();

		String secondMethod();
	}
}
