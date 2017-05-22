package uk.co.strangeskies.reflection.token;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.Visibility.forModifiers;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.lang.reflect.Field;
import java.util.Optional;

import uk.co.strangeskies.reflection.Visibility;

public class VariableMatcher<O, T> {
	public static VariableMatcher<Object, Object> matchVariable() {
		return new VariableMatcher<>(empty(), empty(), empty(), empty());
	}

	private final Optional<String> name;
	private final Optional<Visibility> visibility;
	private final Optional<TypeToken<?>> assignableTo;
	private final Optional<TypeToken<?>> assignableFrom;

	protected VariableMatcher(
			Optional<String> name,
			Optional<Visibility> visibility,
			Optional<TypeToken<?>> assignableTo,
			Optional<TypeToken<?>> assignableFrom) {
		this.name = name;
		this.visibility = visibility;
		this.assignableTo = assignableTo;
		this.assignableFrom = assignableFrom;
	}

	@SuppressWarnings("unchecked")
	public Optional<FieldToken<O, T>> match(FieldToken<?, ?> field) {
		return match(field.getMember()) ? of((FieldToken<O, T>) field) : empty();
	}

	public boolean match(Field field) {
		return name.filter(field.getName()::equals).isPresent()
				&& visibility.filter(forModifiers(field.getModifiers())::equals).isPresent()
				&& assignableTo
						.filter(t -> t.satisfiesConstraintFrom(SUBTYPE, field.getGenericType()))
						.isPresent()
				&& assignableFrom
						.filter(t -> t.satisfiesConstraintTo(SUBTYPE, field.getGenericType()))
						.isPresent();
	}

	public VariableMatcher<O, T> named(String name) {
		return new VariableMatcher<>(of(name), visibility, assignableTo, assignableFrom);
	}

	public VariableMatcher<O, T> visibleTo(Visibility visibility) {
		return new VariableMatcher<>(name, of(visibility), assignableTo, assignableFrom);
	}

	public VariableMatcher<O, T> typed(TypeToken<T> type) {
		return new VariableMatcher<>(name, visibility, of(type), of(type));
	}

	public VariableMatcher<O, T> typed(Class<T> type) {
		return typed(forClass(type));
	}

	public VariableMatcher<O, T> assignableTo(TypeToken<T> type) {
		return new VariableMatcher<>(name, visibility, of(type), assignableFrom);
	}

	public VariableMatcher<O, T> assignableTo(Class<T> type) {
		return assignableTo(forClass(type));
	}

	public VariableMatcher<O, T> assignableFrom(TypeToken<T> type) {
		return new VariableMatcher<>(name, visibility, assignableTo, of(type));
	}

	public VariableMatcher<O, T> assignableFrom(Class<T> type) {
		return assignableFrom(forClass(type));
	}
}
