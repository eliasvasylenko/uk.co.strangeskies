package uk.co.strangeskies.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;

public class Invokable<T, R> implements GenericTypeContainer<Executable> {
	private final Resolver resolver;

	private final TypeLiteral<T> receiverType;
	private final TypeLiteral<R> returnType;
	private final Executable executable;
	private final List<Type> parameters;

	Invokable(TypeLiteral<T> receiverType, TypeLiteral<R> returnType,
			Executable executable) {
		this(new Resolver(), receiverType, returnType, executable, null);
	}

	Invokable(Resolver resolver, TypeLiteral<T> receiverType,
			TypeLiteral<R> returnType, Executable executable, List<Type> arguments) {
		this.receiverType = receiverType;
		this.returnType = returnType;
		this.executable = executable;

		this.resolver = resolver;
		resolver.capture(getGenericDeclaration());

		TypeSubstitution substitution = new TypeSubstitution();
		for (InferenceVariable<?> variable : this.resolver
				.getInferenceVariables(getGenericDeclaration())) {
			substitution = substitution.where(variable.getTypeVariable(), variable);
		}

		parameters = Arrays.stream(getGenericDeclaration().getParameters())
				.map(Parameter::getParameterizedType).map(substitution::resolve)
				.collect(Collectors.toList());
	}

	public static <T> Invokable<T, T> of(Constructor<T> constructor) {
		TypeLiteral<T> type = new TypeLiteral<>(constructor.getDeclaringClass());
		return new Invokable<>(type, type, constructor);
	}

	public static Invokable<?, ?> of(Method method) {
		TypeLiteral<?> type = new TypeLiteral<>(method.getDeclaringClass());
		return new Invokable<>(type, new TypeLiteral<>(Object.class), method);
	}

	public static Invokable<?, ?> of(Executable executable) {
		if (executable instanceof Method)
			return of((Method) executable);
		else
			return of((Constructor<?>) executable);
	}

	@Override
	public Executable getGenericDeclaration() {
		return executable;
	}

	@Override
	public Type getDeclaringType() {
		return receiverType.getType();
	}

	public TypeLiteral<T> getReceiverType() {
		return receiverType;
	}

	public TypeLiteral<R> getReturnType() {
		return returnType;
	}

	public <U extends T> Invokable<U, ? extends R> withReceiverType(
			TypeLiteral<U> type) {
		throw new UnsupportedOperationException(); // TODO
	}

	public <S extends R> Invokable<T, S> withTargetType(TypeLiteral<S> type) {
		throw new UnsupportedOperationException(); // TODO
	}

	/*
	 * If no arguments passed, but parameters expected, infer types when we are
	 * already partially parameterized from applicability verification, or
	 * assuming all parameters are passed as null.
	 */
	public <U extends R> Invokable<T, U> withInferredTypes(
			TypeLiteral<U> targetType, TypeLiteral<?>... parameters) {
		throw new UnsupportedOperationException(); // TODO
	}

	/*
	 * If the given parameters are not compatible with this invokable in a strict
	 * compatibility invocation context, we return null. Otherwise, we infer a
	 * partial parameterisation where necessary and return the resulting
	 * invokable.
	 */
	public Invokable<T, R> withStrictApplicability(Type... arguments) {
		// TODO && make sure no boxing/unboxing occurs!

		return withLooseApplicability(arguments);
	}

	/*
	 * If the given parameters are not compatible with this invokable in a loose
	 * compatibility invocation context, we return null. Otherwise, we infer a
	 * partial parameterisation where necessary and return the resulting
	 * invokable.
	 */
	public Invokable<T, R> withLooseApplicability(Type... arguments) {
		return withLooseApplicability(false, arguments);
	}

	/*
	 * If the given parameters are not compatible with this invokable in a loose
	 * compatibility invocation context, and with variable arity, we return null.
	 * Otherwise, we infer a partial parameterisation where necessary and return
	 * the resulting invokable.
	 */
	public Invokable<T, R> withVariableArityApplicability(Type... arguments) {
		return withLooseApplicability(true, arguments);
	}

	private Invokable<T, R> withLooseApplicability(boolean variableArity,
			Type... arguments) {
		if (variableArity) {
			if (parameters.size() >= arguments.length)
				return null;
		} else if (parameters.size() != arguments.length)
			return null;

		Resolver resolver = new Resolver(this.resolver);

		Iterator<Type> parameters = this.parameters.iterator();
		Type nextParameter = parameters.next();
		Type parameter = nextParameter;
		for (Type argument : arguments) {
			if (nextParameter != null) {
				parameter = nextParameter;
				if (parameters.hasNext())
					nextParameter = parameters.next();
				else if (variableArity) {
					parameter = Types.getComponentType(parameter);
					nextParameter = null;
				}
			}
			resolver.incorporateConstraint(new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY,
					argument, parameter));
		}

		System.out.println(resolver.infer());

		if (!resolver.validate())
			return null;

		return new Invokable<>(resolver, receiverType, returnType, executable,
				Arrays.asList(arguments));
	}

	@SuppressWarnings("unchecked")
	public List<TypeVariable<? extends Executable>> getTypeParameters() {
		return Arrays.asList(executable.getTypeParameters()).stream()
				.map(v -> (TypeVariable<? extends Executable>) v)
				.collect(Collectors.toList());
	}

	public List<Type> getTypeArguments() {
		throw new UnsupportedOperationException(); // TODO
	}

	public Invokable<T, ? extends R> withTypeArgument(
			TypeVariable<? extends Executable> variable, Type instantiation) {
		if (Arrays.stream(executable.getTypeParameters())
				.anyMatch(variable::equals)) {
			Resolver resolver = new Resolver(this.resolver);
			resolver.incorporateInstantiation(variable, instantiation);
		}

		throw new UnsupportedOperationException(); // TODO
	}

	public Invokable<T, ? extends R> withTypeArguments(Type... typeArguments) {
		return withTypeArguments(Arrays.asList(typeArguments));
	}

	public Invokable<T, ? extends R> withTypeArguments(List<Type> typeArguments) {
		throw new UnsupportedOperationException(); // TODO
	}

	public R invoke(T receiver, Object... arguments) {
		return invoke(receiver, Arrays.asList(arguments));
	}

	public R invoke(T receiver, List<? extends Object> arguments) {
		throw new UnsupportedOperationException(); // TODO
	}

	public R invokeSafely(T receiver, TypedObject<?>... arguments) {
		return invokeSafely(receiver, Arrays.asList(arguments));
	}

	public R invokeSafely(T receiver, List<? extends TypedObject<?>> arguments) {
		throw new UnsupportedOperationException(); // TODO
	}
}
