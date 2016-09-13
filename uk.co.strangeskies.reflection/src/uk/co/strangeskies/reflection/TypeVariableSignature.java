package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Take care not to allow instances of this class to leak out into the wider
 * program outside the context of declaration and definition of
 * {@link GenericSignature mutable declarations}.
 * 
 * @author Elias N Vasylenko
 */
public class TypeVariableSignature implements Type {
	private final List<TypeToken<?>> bounds;

	public TypeVariableSignature() {
		bounds = new ArrayList<>();
	}

	public List<TypeToken<?>> getBounds() {
		return bounds;
	}

	public TypeVariableSignature withUpperBounds(Type... bounds) {
		return withUpperBounds(Arrays.stream(bounds).map(TypeToken::over).collect(Collectors.toList()));
	}

	public TypeVariableSignature withUpperBounds(TypeToken<?>... bounds) {
		return withUpperBounds(Arrays.asList(bounds));
	}

	public TypeVariableSignature withUpperBounds(List<TypeToken<?>> bounds) {
		this.bounds.addAll(bounds);

		return this;
	}
}
