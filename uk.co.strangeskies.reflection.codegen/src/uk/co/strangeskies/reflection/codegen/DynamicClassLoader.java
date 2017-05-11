package uk.co.strangeskies.reflection.codegen;

public class DynamicClassLoader extends ClassLoader {
	public DynamicClassLoader(ClassLoader parent) {
		super(parent);
	}

	public Class<?> injectClass(String name, byte[] bytecode) {
		return defineClass(name, bytecode, 0, bytecode.length);
	}
}
