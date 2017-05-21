package uk.co.strangeskies.reflection.token;

import static uk.co.strangeskies.reflection.token.MethodMatcher.Builder.matchTrue;

import java.lang.reflect.Executable;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.Visibility;

public interface MethodMatcher<O, T> {
	public default boolean match(Executable executable) {
		return match(ExecutableToken.forExecutable(executable)).isPresent();
	}

	public Optional<ExecutableToken<O, T>> match(ExecutableToken<?, ?> executable);

	public static Builder<Object, Object> matchMethod() {
		return new Builder<>(matchTrue(), matchTrue(), matchTrue(), matchTrue());
	}

	public class Builder<O, T> implements MethodMatcher<O, T> {
		static <T> Predicate<T> matchTrue() {
			return o -> true;
		}

		private final Predicate<String> name;
		private final Predicate<TypeToken<?>> returnType;
		private final Predicate<Visibility> visibility;
		private final Predicate<Stream<ExecutableParameter>> arguments;

		protected Builder(
				Predicate<String> name,
				Predicate<TypeToken<?>> returnType,
				Predicate<Visibility> visibility,
				Predicate<Stream<ExecutableParameter>> arguments) {
			this.name = name;
			this.returnType = returnType;
			this.visibility = visibility;
			this.arguments = arguments;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Optional<ExecutableToken<O, T>> match(ExecutableToken<?, ?> executable) {
			if (matchImpl(executable)) {
				return Optional.of((ExecutableToken<O, T>) executable);
			} else {
				return Optional.empty();
			}
		}

		private boolean matchImpl(ExecutableToken<?, ?> executable) {
			return name.test(executable.getName())
					&& returnType.test(executable.getReturnType())
					&& visibility.test(executable.getVisibility())
					&& arguments.test(executable.getParameters());
		}

		public Builder<O, T> constructor() {
			return named("<init>");
		}

		public Builder<O, T> staticInitializer() {
			return named("<cinit>");
		}

		public Builder<O, T> named(String name) {
			return new Builder<>(this.name.and(name::equals), returnType, visibility, arguments);
		}

		public <U> Builder<O, U> returning(TypeToken<U> returnType) {
			return new Builder<>(
					name,
					this.returnType.and(t -> returnType.satisfiesConstraintFrom(Kind.SUBTYPE, t)),
					visibility,
					arguments);
		}

		public <U> Builder<O, U> returning(Class<U> type) {
			return returning(TypeToken.forClass(type));
		}

		public <U> Builder<U, T> receiving(TypeToken<U> type) {
			return null;
		}

		public <U> Builder<U, T> receiving(Class<U> type) {
			return receiving(TypeToken.forClass(type));
		}

		public Builder<O, T> parameters() {
			// TODO Auto-generated method stub
			return null;
		}

		// TODO "accepting(...)" by parameter types and by parameter count
	}
}
