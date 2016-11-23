package uk.co.strangeskies.reflection.token;

import java.lang.reflect.Type;
import java.util.Objects;

public class ExecutableParameter {
	private final String name;
	private final Type type;
	private final Class<?> erasure;

	protected ExecutableParameter(String name, Type type, Class<?> erasure) {
		this.name = name;
		this.type = type;
		this.erasure = erasure;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public Class<?> getErasure() {
		return erasure;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ExecutableParameter))
			return false;

		ExecutableParameter that = (ExecutableParameter) obj;

		return Objects.equals(this.name, that.name) && Objects.equals(this.type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public String toString() {
		return getType() + " " + getName();
	}
}
