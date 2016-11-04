package uk.co.strangeskies.scripting;

import static java.lang.reflect.Proxy.newProxyInstance;

import java.util.function.BiFunction;

import javax.script.Invocable;
import javax.script.ScriptException;

/**
 * A partial implementation of {@link Invocable} which implements
 * {@link #getInterface(Class)} and {@link #getInterface(Object, Class)} by
 * proxying the requested class and delegating invocations to
 * {@link #invokeFunction(String, Object...)} and
 * {@link #invokeMethod(Object, String, Object...)} respectively.
 * <p>
 * This may not be as well optimized as would be possible with a specialized
 * implementation for a given engine, but it will probably be pretty close.
 * 
 * @author Elias N Vasylenko
 */
public interface InvocableBase extends Invocable {
	@Override
	default <T> T getInterface(Class<T> clasz) {
		return getInterface(null, clasz);
	}

	@SuppressWarnings("unchecked")
	@Override
	default <T> T getInterface(Object thiz, Class<T> clasz) {
		BiFunction<String, Object[], Object> invocation;

		if (thiz == null) {
			invocation = (name, args) -> {
				try {
					return invokeFunction(name, args);
				} catch (NoSuchMethodException | ScriptException e) {
					throw new RuntimeException(e);
				}
			};
		} else {
			invocation = (name, args) -> {
				try {
					return invokeMethod(thiz, name, args);
				} catch (NoSuchMethodException | ScriptException e) {
					throw new RuntimeException(e);
				}
			};
		}

		return (T) newProxyInstance(getClass().getClassLoader(), new Class<?>[] { clasz }, (proxy, method, args) -> {
			return invocation.apply(method.getName(), args);
		});
	}
}
