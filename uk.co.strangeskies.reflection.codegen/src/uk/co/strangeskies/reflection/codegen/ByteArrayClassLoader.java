package uk.co.strangeskies.reflection.codegen;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ByteArrayClassLoader extends ClassLoader {
	private final Map<String, byte[]> injectedClasses = new HashMap<>();

	public ByteArrayClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (injectedClasses.containsKey(name)) {
			byte[] bytecode = injectedClasses.get(name);
			return defineClass(name, bytecode, 0, bytecode.length);
		} else {
			return super.findClass(name);
		}
	}

	public ByteArrayClassLoader addClass(String name, byte[] bytecode) {
		injectedClasses.put(name, bytecode);
		return this;
	}

	public ByteArrayClassLoader addClasses(Map<String, byte[]> bytecodes) {
		bytecodes.entrySet().stream().forEach(e -> addClass(e.getKey(), e.getValue()));
		return this;
	}

	public Class<?> defineClass(String name, byte[] bytecode) {
		Class<?> injectedClass = defineClass(name, bytecode, 0, bytecode.length);
		injectedClasses.put(name, bytecode);
		return injectedClass;
	}

	public ByteArrayClassLoader defineClasses(Map<String, byte[]> bytecodes) {
		bytecodes.entrySet().stream().forEach(e -> defineClass(e.getKey(), e.getValue()));
		return this;
	}

	public Stream<String> getInjectedClasses() {
		return injectedClasses.keySet().stream();
	}

	public byte[] getInjectedBytes(String name) {
		if (!injectedClasses.containsKey(name))
			throw new IllegalArgumentException();
		return injectedClasses.get(name);
	}
}
