package uk.co.strangeskies.reflection.token;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.Visibility.forModifiers;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Predicate;

import uk.co.strangeskies.reflection.Visibility;

public class VariableMatcher<O, T> implements Predicate<FieldToken<?, ?>> {
	public static VariableMatcher<Object, Object> allVariables() {
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
		if (test(field))
			return of((FieldToken<O, T>) field);
		else
			return empty();
	}

	@Override
	public boolean test(FieldToken<?, ?> field) {
		return testImpl(field.getName(), field.getVisibility(), field.getFieldType().getType());
	}

	public boolean test(Field field) {
		return testImpl(field.getName(), forModifiers(field.getModifiers()), field.getGenericType());
	}

	private boolean testImpl(String name, Visibility visibility, Type type) {
		return this.name.map(name::equals).orElse(true)
				&& this.visibility.filter(visibility::equals).isPresent()
				&& this.assignableTo.filter(t -> t.satisfiesConstraintFrom(SUBTYPE, type)).isPresent()
				&& this.assignableFrom.filter(t -> t.satisfiesConstraintTo(SUBTYPE, type)).isPresent();
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
