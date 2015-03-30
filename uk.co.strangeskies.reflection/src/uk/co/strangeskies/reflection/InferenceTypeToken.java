package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utilities.function.collection.SetTransformOnceView;

public class InferenceTypeToken<T> extends TypeToken<T> {
	@SuppressWarnings("unchecked")
	private InferenceTypeToken(InferenceVariable type, Resolver resolver) {
		super(new Resolver(resolver), type, (Class<? super T>) Types
				.getRawType(IntersectionType.from(resolver.getBounds().getUpperBounds(
						type))));
	}

	@SuppressWarnings("unchecked")
	private InferenceTypeToken(Resolver resolver, WildcardType type) {
		super(resolver, resolver.inferWildcardType(type), (Class<? super T>) Types
				.getRawType(type));
	}

	public static InferenceTypeToken<?> of(InferenceVariable type,
			Resolver resolver) {
		return new InferenceTypeToken<>(type, resolver);
	}

	public static InferenceTypeToken<?> of(WildcardType bounds) {
		return new InferenceTypeToken<>(new Resolver(), bounds);
	}

	@Override
	public InferenceVariable getType() {
		return (InferenceVariable) super.getType();
	}

	public InferenceTypeToken<T> withLowerBound(Type type) {
		Resolver resolver = getResolver();
		new ConstraintFormula(Kind.SUBTYPE, type, getType()).reduceInto(resolver
				.getBounds());

		return new InferenceTypeToken<>(getType(), resolver);
	}

	public InferenceTypeToken<T> withUpperBound(Type type) {
		Resolver resolver = getResolver();
		new ConstraintFormula(Kind.SUBTYPE, getType(), type).reduceInto(resolver
				.getBounds());

		return new InferenceTypeToken<>(getType(), resolver);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> TypeToken<? extends U> resolveSupertypeParameters(
			Class<U> superclass) {
		Resolver resolver = getInternalResolver();

		resolver.capture(superclass);
		Type parameterizedType = resolver.resolveType(ParameterizedTypes.from(
				superclass).getType());

		new ConstraintFormula(Kind.SUBTYPE, getType(), parameterizedType)
				.reduceInto(resolver.getBounds());

		return (TypeToken<? extends U>) TypeToken.of(resolver
				.resolveType(parameterizedType));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> TypeToken<? extends U> resolveSubtypeParameters(Class<U> subclass) {
		Resolver resolver = getInternalResolver();

		resolver.capture(subclass);
		Type parameterizedType = resolver.resolveType(ParameterizedTypes.from(
				subclass).getType());

		new ConstraintFormula(Kind.SUBTYPE, parameterizedType, getType())
				.reduceInto(resolver.getBounds());

		return (TypeToken<? extends U>) TypeToken.of(resolver
				.resolveType(parameterizedType));
	}

	@Override
	public Set<? extends InferenceInvokable<? super T, ?>> getInvokables() {
		return new SetTransformOnceView<>(super.getInvokables(),
				InferenceInvokable::new);
	}

	@Override
	public Set<? extends InferenceInvokable<? super T, ?>> getMethods() {
		return new SetTransformOnceView<>(super.getMethods(),
				InferenceInvokable::new);
	}

	@Override
	public Set<? extends InferenceInvokable<T, T>> getConstructors() {
		return new SetTransformOnceView<>(super.getConstructors(),
				InferenceInvokable::new);
	}

	@Override
	public InferenceInvokable<T, ? extends T> resolveConstructorOverload(
			List<? extends Type> parameters) {
		return new InferenceInvokable<>(
				super.resolveConstructorOverload(parameters));
	}

	@Override
	public InferenceInvokable<T, ? extends T> resolveConstructorOverload(
			Type... parameters) {
		return new InferenceInvokable<>(
				super.resolveConstructorOverload(parameters));
	}

	@Override
	public InferenceInvokable<? super T, ?> resolveMethodOverload(String name,
			List<? extends Type> parameters) {
		return new InferenceInvokable<>(super.resolveMethodOverload(name,
				parameters));
	}

	@Override
	public InferenceInvokable<? super T, ?> resolveMethodOverload(String name,
			Type... parameters) {
		return new InferenceInvokable<>(super.resolveMethodOverload(name,
				parameters));
	}

	@SuppressWarnings("unchecked")
	public TypeToken<T> infer() {
		return (TypeToken<T>) TypeToken.of(getResolver().infer(getType()));
	}

	public static class InferenceInvokable<T, R> extends Invokable<T, R> {
		public InferenceInvokable(Invokable<T, R> invokable) {
			super(invokable);
		}

		@Override
		public InferenceTypeToken<T> getReceiverType() {
			return (InferenceTypeToken<T>) super.getReceiverType();
		}
	}
}
