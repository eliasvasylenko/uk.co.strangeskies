package uk.co.strangeskies.reflection;

import java.util.Arrays;
import java.util.Objects;

/**
 * The signature of a method according to Java language override rules. In other
 * words, the identity of a method as given by its name and erased parameter
 * types.
 * 
 * @author Elias N Vasylenko
 */
public class MethodOverrideSignature {
	private final String name;
	private final Class<?>[] parameterClasses;

	/**
	 * @param name
	 *          the name of the method signature
	 * @param parameterClasses
	 *          the erased type of the method signature
	 */
	public MethodOverrideSignature(String name, Class<?>[] parameterClasses) {
		this.name = name;
		this.parameterClasses = parameterClasses;
	}

	public String getName() {
		return name;
	}

	public Class<?>[] getParameterClasses() {
		return parameterClasses;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		else if (!(obj instanceof MethodOverrideSignature))
			return false;

		MethodOverrideSignature that = (MethodOverrideSignature) obj;

		return Arrays.equals(this.parameterClasses, that.parameterClasses) && Objects.equals(this.name, that.name);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(parameterClasses) ^ name.hashCode();
	}

	@Override
	public String toString() {
		return name + "/" + Arrays.toString(parameterClasses);
	}
}
