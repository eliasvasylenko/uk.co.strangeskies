package uk.co.strangeskies.reflection.token;

import java.util.function.Predicate;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.Visibility;

public class VariableMatcher<O, T> {
	private static <T> Predicate<T> match() {
		return o -> true;
	}

	public static VariableMatcher<Object, Object> matchVariable() {
		return new VariableMatcher<>(match(), match(), match());
	}

	private final Predicate<String> name;
	private final Predicate<TypeToken<?>> type;
	private final Predicate<Visibility> visibility;

	protected VariableMatcher(
			Predicate<String> name,
			Predicate<TypeToken<?>> type,
			Predicate<Visibility> visibility) {
		this.name = name;
		this.type = type;
		this.visibility = visibility;
	}

	public boolean match(FieldToken<?, ?> field) {
		return name.test(field.getName())
				&& type.test(field.getFieldType())
				&& visibility.test(field.getVisibility());
	}

	public VariableMatcher<O, T> named(String name) {
		return new VariableMatcher<>(this.name.and(name::equals), type, visibility);
	}

	public <U> VariableMatcher<O, U> returning(TypeToken<U> type) {
		return new VariableMatcher<>(
				name,
				this.type.and(t -> type.satisfiesConstraintFrom(Kind.SUBTYPE, t)),
				visibility);
	}

	public <U> VariableMatcher<O, U> returning(Class<U> type) {
		return returning(TypeToken.forClass(type));
	}

	public <U> VariableMatcher<U, T> receiving(TypeToken<U> type) {
		return null;
	}

	public <U> VariableMatcher<U, T> receiving(Class<U> type) {
		return receiving(TypeToken.forClass(type));
	}

	// TODO "accepting(...)" by parameter types and by parameter count
}
