package uk.co.strangeskies.reflection.token;

import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.co.strangeskies.collection.stream.StreamUtilities.zip;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.Visibility.forModifiers;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.Visibility;

public class MethodMatcher<O, T> implements Predicate<ExecutableToken<?, ?>> {
	public static MethodMatcher<Object, Object> allMethods() {
		return new MethodMatcher<>(empty(), empty(), empty(), empty());
	}

	private final Optional<String> name;
	private final Optional<Visibility> visibility;
	private final Optional<TypeToken<?>> returnType;
	private final Optional<List<TypeToken<?>>> argumentTypes;

	protected MethodMatcher(
			Optional<String> name,
			Optional<Visibility> visibility,
			Optional<TypeToken<?>> returnType,
			Optional<List<TypeToken<?>>> argumentTypes) {
		this.name = name;
		this.visibility = visibility;
		this.returnType = returnType;
		this.argumentTypes = argumentTypes;
	}

	@SuppressWarnings("unchecked")
	public Optional<ExecutableToken<O, T>> match(ExecutableToken<?, ?> executable) {
		if (test(executable))
			return of((ExecutableToken<O, T>) executable);
		else
			return empty();
	}

	@Override
	public boolean test(ExecutableToken<?, ?> executable) {
		return testImpl(
				executable.getName(),
				executable.getVisibility(),
				executable.getReturnType().getType(),
				executable.getParameters().map(ExecutableParameter::getType));
	}

	public boolean test(Executable executable) {
		return testImpl(
				executable.getName(),
				forModifiers(executable.getModifiers()),
				executable.getAnnotatedReturnType().getType(),
				stream(executable.getGenericParameterTypes()));
	}

	private boolean testImpl(
			String name,
			Visibility visibility,
			Type returnType,
			Stream<Type> argumentTypes) {
		return this.name.map(name::equals).orElse(true)
				&& this.visibility.map(visibility::visibilityIsAtLeast).orElse(true)
				&& this.returnType.map(type -> type.satisfiesConstraintFrom(SUBTYPE, returnType)).orElse(
						true)
				&& this.argumentTypes
						.map(
								arguments -> zip(
										arguments.stream(),
										argumentTypes,
										(a, b) -> a.satisfiesConstraintTo(SUBTYPE, b)).reduce((a, b) -> a && b).get())
						.orElse(true);
	}

	public MethodMatcher<O, T> constructor() {
		return named("<init>");
	}

	public MethodMatcher<O, T> staticInitializer() {
		return named("<cinit>");
	}

	public MethodMatcher<O, T> named(String name) {
		return new MethodMatcher<>(of(name), visibility, returnType, argumentTypes);
	}

	public MethodMatcher<O, T> visibleTo(Visibility visibility) {
		return new MethodMatcher<>(name, of(visibility), returnType, argumentTypes);
	}

	public <U> MethodMatcher<O, U> returning(TypeToken<U> returnType) {
		return new MethodMatcher<>(name, visibility, of(returnType), argumentTypes);
	}

	public <U> MethodMatcher<O, U> returning(Class<U> type) {
		return returning(forClass(type));
	}

	public <U> MethodMatcher<U, T> receiving(TypeToken<U> type) {
		return null;
	}

	public <U> MethodMatcher<U, T> receiving(Class<U> type) {
		return receiving(forClass(type));
	}

	public MethodMatcher<O, T> parameters() {
		// TODO Auto-generated method stub
		return null;
	}

	// TODO "accepting(...)" by parameter types and by parameter count
}
