package uk.co.strangeskies.reflection;

import static java.util.Arrays.asList;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeParameter;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ParameterizedToken<S extends ParameterizedToken<S>> {
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * TODO surely this would create a more sensible replacement for the current
	 * #determineContainerTypeArguments method in AbstractMemberToken
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	ParameterizedToken<?> getContainingToken();

	/**
	 * @return All generic type parameters of the wrapped {@link Executable}.
	 */
	Stream<TypeVariable<?>> getAllTypeParameters();

	/**
	 * @return All generic type parameter instantiations of the wrapped
	 *         {@link Executable}, or their inference variables if not yet
	 *         instantiated.
	 */
	Stream<Map.Entry<TypeVariable<?>, Type>> getAllTypeArguments();

	/**
	 * @return The generic type parameter instantiations of the wrapped
	 *         {@link Executable}, or their inference variables if not yet
	 *         instantiated.
	 */
	Stream<TypeVariable<?>> getTypeParameters();

	/**
	 * @return The generic type parameter instantiations of the wrapped
	 *         {@link Executable}, or their inference variables if not yet
	 *         instantiated.
	 */
	Stream<Map.Entry<TypeVariable<?>, Type>> getTypeArguments();

	/**
	 * Derive a new {@link ExecutableToken} instance from this, with the given
	 * instantiation substituted for the given {@link TypeVariable}.
	 * 
	 * <p>
	 * The substitution will only succeed if it is compatible with the bounds on
	 * that type variable, and if it is more specific than the current type of the
	 * type variable, whether it is an {@link InferenceVariable}, a
	 * {@link TypeVariableCapture}, or another class of {@link Type}.
	 * 
	 * <p>
	 * For example, the following method could be used to derive instances of
	 * TypeToken over different parameterizations of {@code List<?>} at runtime.
	 * 
	 * <pre>
	 * <code>
	 * public List&lt;T&gt; getList(TypeToken&lt;T&gt; clazz)} {
	 * 	 return new TypeToken&lt;T&gt;()} {}.withTypeArgument(new TypeParameter&lt;T&gt;() {}, clazz);
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param <U>
	 *          the type variable on the generic declaration which is the
	 *          {@link Executable} wrapped by this {@link ExecutableToken}
	 * @param parameter
	 *          the type variable on the generic declaration which is the
	 *          {@link Executable} wrapped by this {@link ExecutableToken}
	 * @param argument
	 *          the type with which to instantiate the given type variable
	 * @return a new derived {@link ExecutableToken} instance with the given
	 *         instantiation substituted for the given type variable
	 */
	default <U> S withTypeArgument(TypeParameter<U> parameter, TypeToken<U> argument) {
		return withTypeArgument(parameter.getType(), argument.getType());
	}

	/**
	 * Derive a new {@link ExecutableToken} instance from this, with the given
	 * instantiation substituted for the given {@link TypeVariable}.
	 * 
	 * <p>
	 * The substitution will only succeed if it is compatible with the bounds on
	 * that type variable, and if it is more specific than the current type of the
	 * type variable, whether it is an {@link InferenceVariable}, a
	 * {@link TypeVariableCapture}, or another class of {@link Type}.
	 * 
	 * @param parameter
	 *          the type variable on the generic declaration which is the
	 *          {@link Executable} wrapped by this {@link ExecutableToken}
	 * @param argument
	 *          the type with which to instantiate the given type variable
	 * @return a new derived {@link ExecutableToken} instance with the given
	 *         instantiation substituted for the given type variable
	 */
	S withTypeArgument(TypeVariable<?> parameter, Type argument);

	/**
	 * As with {@link TypeToken#withTypeArgument(TypeParameter, TypeToken)}.
	 * 
	 * @param <V>
	 *          The parameter to make a substitution for.
	 * @param parameter
	 *          The parameter to make a substitution for.
	 * @param argument
	 *          The argument to substitute for that parameter.
	 * @return A new TypeToken instance over the type resulting from the
	 *         substitution.
	 */
	default <V> S withTypeArgument(TypeParameter<V> parameter, Class<V> argument) {
		return withTypeArgument(parameter.getType(), argument);
	}

	<U> TypeToken<U> resolveTypeArgument(TypeParameter<U> parameter);

	Type resolveTypeArgument(TypeVariable<?> parameter);

	/**
	 * Derive a new {@link ExecutableToken} instance with the given generic type
	 * argument substitutions, as per the behavior of
	 * {@link #withTypeArgument(TypeVariable, Type)}, but with every argument
	 * provided in order.
	 * 
	 * @param typeArguments
	 *          a list of arguments for each generic type parameter of the
	 *          underlying {@link Executable}
	 * @return a new derived {@link ExecutableToken} instance with the given
	 *         instantiations substituted for each generic type parameter, in
	 *         order
	 */
	default S withTypeArguments(Type... typeArguments) {
		return withTypeArguments(asList(typeArguments));
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with the given generic type
	 * argument substitutions, as per the behavior of
	 * {@link #withTypeArgument(TypeVariable, Type)}, but with every argument
	 * provided in order.
	 * 
	 * @param methodTypeArguments
	 *          a list of arguments for each generic type parameter of the
	 *          underlying {@link Executable}
	 * @return a new derived {@link ExecutableToken} instance with the given
	 *         instantiations substituted for each generic type parameter, in
	 *         order
	 */
	S withTypeArguments(List<Type> methodTypeArguments);
}
