package uk.co.strangeskies.reflection.codegen;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class InjectionClassLoader extends ClassLoader {
	private final Map<Class<?>, byte[]> injectedClasses = new HashMap<>();

	public InjectionClassLoader(ClassLoader parent) {
		super(parent);
	}

	public Class<?> injectClass(String name, byte[] bytecode) {
		Class<?> injectedClass = defineClass(name, bytecode, 0, bytecode.length);
		injectedClasses.put(injectedClass, bytecode);
		return injectedClass;
	}

	public Stream<Class<?>> getInjectedClasses() {
		return injectedClasses.keySet().stream();
	}

	public byte[] getInjectedBytes(Class<?> injectedClass) {
		if (!injectedClasses.containsKey(injectedClass))
			throw new IllegalArgumentException();
		return injectedClasses.get(injectedClass);
	}
}
