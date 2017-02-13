package uk.co.strangeskies.reflection.token;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.empty;
import static uk.co.strangeskies.reflection.token.TypeParameter.forTypeVariable;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeVariableCapture;
import uk.co.strangeskies.utilities.collection.StreamUtilities;

/**
 * A token representing a declaration. If the declaration is generic, it may be
 * raw or parameterized.
 * 
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          the type of declaration
 */
public interface DeclarationToken<S extends DeclarationToken<S>> {
	/**
	 * @return true if the declaration represents a raw type or invocation, false
	 *         otherwise
	 */
	boolean isRaw();

	/**
	 * If the declaration is raw, parameterize it with its own type parameters,
	 * otherwise return the declaration itself.
	 * 
	 * @return the parameterized version of the declaration where applicable, else
	 *         the unmodified declaration
	 */
	S parameterize();

	/**
	 * @return the declaration directly enclosing this declaration
	 */
	Optional<? extends DeclarationToken<?>> getOwningDeclaration();

	/**
	 * @return the count of all generic type parameters of the declaration and any
	 *         enclosing declarations
	 */
	default int getAllTypeParameterCount() {
		if (isRaw())
			return 0;
		else
			return getTypeParameterCount() + getOwningDeclaration().map(DeclarationToken::getAllTypeParameterCount).orElse(0);
	}

	/**
	 * @return all generic type parameters of the declaration and any enclosing
	 *         declarations
	 */
	default Stream<TypeParameter<?>> getAllTypeParameters() {
		if (isRaw())
			return Stream.empty();
		else
			return Stream.concat(
					getTypeParameters(),
					getOwningDeclaration().map(DeclarationToken::getAllTypeParameters).orElse(empty()));
	}

	/**
	 * @return all generic type parameter instantiations of the declaration, or
	 *         their inference variables if not yet instantiated.
	 */
	default Stream<TypeArgument<?>> getAllTypeArguments() {
		if (isRaw())
			return Stream.empty();
		else
			return Stream.concat(
					getTypeArguments(),
					getOwningDeclaration().map(DeclarationToken::getAllTypeArguments).orElse(empty()));
	}

	/**
	 * @return the count of the generic type parameters of the declaration.
	 */
	int getTypeParameterCount();

	/**
	 * @return The generic type parameter instantiations of the wrapped
	 *         {@link Executable}, or their inference variables if not yet
	 *         instantiated.
	 */
	Stream<TypeParameter<?>> getTypeParameters();

	/**
	 * @return The generic type parameter instantiations of the wrapped
	 *         {@link Executable}, or their inference variables if not yet
	 *         instantiated.
	 */
	Stream<TypeArgument<?>> getTypeArguments();

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
	 * public TypeToken&lt;List&lt;T&gt;&gt; getListType(TypeToken&lt;T&gt; elementType)} {
	 * 	 return new TypeToken&lt;T&gt;()} {}.withTypeArguments(new TypeParameter&lt;T&gt;() {}.as(elementType));
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param arguments
	 *          the type variable instantiations
	 * @return a new derived {@link ExecutableToken} instance with the given
	 *         instantiation substituted for the given type variable
	 */
	default S withTypeArguments(TypeArgument<?>... arguments) {
		return withTypeArguments(Arrays.asList(arguments));
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
	 * <p>
	 * For example, the following method could be used to derive instances of
	 * TypeToken over different parameterizations of {@code List<?>} at runtime.
	 * 
	 * <pre>
	 * <code>
	 * public TypeToken&lt;List&lt;T&gt;&gt; getListType(TypeToken&lt;T&gt; elementType)} {
	 * 	 return new TypeToken&lt;T&gt;()} {}.withTypeArguments(new TypeParameter&lt;T&gt;() {}.as(elementType));
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param arguments
	 *          the type variable instantiations
	 * @return a new derived {@link ExecutableToken} instance with the given
	 *         instantiation substituted for the given type variable
	 */
	S withTypeArguments(Collection<? extends TypeArgument<?>> arguments);

	/**
	 * Resolve the instantiation of the given type variable in the context of this
	 * declaration.
	 * 
	 * @param <U>
	 *          the type of the type variable to resolve
	 * @param parameter
	 *          the type parameter
	 * @return the argument of the given parameter with respect to this
	 *         declaration
	 */
	@SuppressWarnings("unchecked")
	default <U> TypeArgument<U> resolveTypeArgument(TypeParameter<U> parameter) {
		return getAllTypeArguments()
				.filter(a -> a.getParameter().equals(parameter))
				.findAny()
				.map(p -> (TypeArgument<U>) p)
				.orElseThrow(() -> new ReflectionException(p -> p.cannotResolveTypeVariable(parameter.getType(), this)));
	}

	/**
	 * @see #resolveTypeArgument(TypeParameter)
	 */
	@SuppressWarnings("javadoc")
	default Type resolveTypeArgument(TypeVariable<?> parameter) {
		return resolveTypeArgument(forTypeVariable(parameter)).getType();
	}

	/**
	 * @see #withTypeArguments(List)
	 */
	@SuppressWarnings("javadoc")
	default S withTypeArguments(Type... typeArguments) {
		return withTypeArguments(asList(typeArguments));
	}

	/**
	 * Derive a new {@link DeclarationToken} instance with the given generic type
	 * argument substitutions, as per the behavior of
	 * {@link #withTypeArguments(TypeArgument[])}, but with the types of each
	 * argument provided in order.
	 * 
	 * @param typeArguments
	 *          a list of arguments for each generic type parameter of the
	 *          underlying declaration
	 * @return a new derived {@link DeclarationToken} instance with the given
	 *         instantiations substituted for each generic type parameter, in
	 *         order
	 */
	default S withTypeArguments(List<Type> typeArguments) {
		Stream<TypeParameter<?>> typeParameters;

		if (typeArguments.size() == getTypeParameterCount()) {
			typeParameters = getTypeParameters();
		} else if (typeArguments.size() == getAllTypeParameterCount()) {
			typeParameters = getAllTypeParameters();
		} else {
			throw new ReflectionException(
					p -> p.incorrectTypeArgumentCount(
							getTypeParameters().map(TypeParameter::getType).collect(Collectors.toList()),
							typeArguments));
		}

		return withTypeArguments(
				StreamUtilities.zip(typeParameters, typeArguments.stream(), (p, a) -> p.asType(a)).collect(toList()));
	}
}
