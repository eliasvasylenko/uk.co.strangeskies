package uk.co.strangeskies.reflection;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utilities.collection.SelfStreamDecorator;
import uk.co.strangeskies.utilities.tuple.Pair;

public class InvocableMemberStream<I extends ExecutableToken<?, ?>>
		implements SelfStreamDecorator<I, InvocableMemberStream<I>> {
	private final TypeToken<?> type;
	private final Stream<I> members;

	public InvocableMemberStream(TypeToken<?> type, Stream<I> members) {
		this.type = type;
		this.members = members;
	}

	@Override
	public Stream<I> getComponent() {
		return members;
	}

	@Override
	public InvocableMemberStream<I> decorateIntermediate(Function<? super Stream<I>, Stream<I>> transformation) {
		return new InvocableMemberStream<>(type, transformation.apply(getComponent()));
	}

	public <T> I resolveOverload() {
		return resolveOverload(Collections.emptyList());
	}

	public <T> I resolveOverload(Type... arguments) {
		return resolveOverload(Arrays.stream(arguments).map(TypeToken::over).collect(toList()));
	}

	public <T> I resolveOverload(TypeToken<?>... arguments) {
		return resolveOverload(Arrays.asList(arguments));
	}

	@SuppressWarnings("unchecked")
	public <T> I resolveOverload(List<? extends TypeToken<?>> arguments) {
		Set<? extends I> candidates = collect(Collectors.toSet());

		if (candidates.isEmpty())
			throw new IllegalArgumentException(
					"Cannot find any applicable invocable in '" + type + "' for arguments '" + arguments + "'");

		candidates = resolveApplicableExecutableMembers(candidates, arguments);

		return resolveMostSpecificExecutableMember(candidates);
	}

	private static boolean isArgumentCountValid(Executable method, int arguments) {
		return (method.isVarArgs() ? method.getParameterCount() <= arguments + 1 : method.getParameterCount() == arguments);
	}

	/**
	 * Find the set of all given overload candidates which are applicable to
	 * invocation with the given parameters. Strict applicability is considered
	 * first, then if no candidates are found loose applicability is considered,
	 * then if still no candidates are found, variable arity applicability is
	 * considered.
	 * 
	 * @param <I>
	 *          the type of invokable
	 * @param candidates
	 *          The candidates for which we wish to determine applicability.
	 * @param parameters
	 *          The parameters representing the invocation for which we wish to
	 *          determine applicability.
	 * @return The set of all given overload candidates which are most applicable
	 *         to invocation with the given parameters.
	 */
	@SuppressWarnings("unchecked")
	public static <I extends ExecutableToken<?, ?>> Set<? extends I> resolveApplicableExecutableMembers(
			Set<? extends I> candidates, List<? extends TypeToken<?>> parameters) {
		Map<I, RuntimeException> failures = new LinkedHashMap<>();
		BiConsumer<I, RuntimeException> putFailures = failures::put;

		Set<? extends I> compatibleCandidates = filterOverloadCandidates(candidates,
				i -> (I) i.withLooseApplicability(parameters), putFailures);

		if (compatibleCandidates.isEmpty()) {
			compatibleCandidates = new HashSet<>(candidates);
			for (I candidate : candidates)
				if (!candidate.isVariableArityDefinition())
					compatibleCandidates.remove(candidate);

			compatibleCandidates = filterOverloadCandidates(compatibleCandidates,
					i -> (I) i.withVariableArityApplicability(parameters), putFailures);
		} else {
			Set<? extends I> oldCompatibleCandidates = compatibleCandidates;
			compatibleCandidates = filterOverloadCandidates(compatibleCandidates,
					i -> (I) i.withStrictApplicability(parameters), putFailures);
			if (compatibleCandidates.isEmpty())
				compatibleCandidates = oldCompatibleCandidates;
		}

		if (compatibleCandidates.isEmpty())
			throw new ReflectionException(p -> p.cannotResolveApplicable(candidates, parameters),
					failures.get(failures.keySet().stream().findFirst().get()));

		return compatibleCandidates;
	}

	private static <I extends ExecutableToken<?, ?>> Set<? extends I> filterOverloadCandidates(
			Collection<? extends I> candidates, Function<? super I, I> applicabilityFunction,
			BiConsumer<I, RuntimeException> failures) {
		return candidates.stream().map(i -> {
			try {
				return applicabilityFunction.apply(i);
			} catch (RuntimeException e) {
				failures.accept(i, e);
				return null;
			}
		}).filter(o -> o != null).collect(Collectors.toSet());
	}

	/**
	 * Find which of the given overload candidates is the most specific according
	 * to the rules described by the Java 8 language specification.
	 * 
	 * <p>
	 * If no single most specific candidate can be found, the method will throw a
	 * {@link ReflectionException}.
	 * 
	 * @param <I>
	 *          the type of invokable
	 * @param candidates
	 *          The candidates from which to select the most specific.
	 * @return The most specific of the given candidates.
	 */
	public static <I extends ExecutableToken<?, ?>> I resolveMostSpecificExecutableMember(
			Collection<? extends I> candidates) {
		if (candidates.size() == 1)
			return candidates.iterator().next();

		Set<I> mostSpecificSoFar = resolveMostSpecificCandidateSet(candidates);

		/*
		 * Find which of the remaining candidates, which should all have identical
		 * parameter types, is declared in the lowermost class.
		 */
		Iterator<I> overrideCandidateIterator = mostSpecificSoFar.iterator();
		I mostSpecific = overrideCandidateIterator.next();
		while (overrideCandidateIterator.hasNext()) {
			I candidate = overrideCandidateIterator.next();

			if (!candidate.getParameters().equals(mostSpecific.getParameters())
					|| !candidate.getName().equals(mostSpecific.getName())) {
				I mostSpecificFinal = mostSpecific;
				throw new ReflectionException(p -> p.cannotResolveAmbiguity(candidate, mostSpecificFinal));
			}

			mostSpecific = candidate.getMember().getDeclaringClass()
					.isAssignableFrom(mostSpecific.getMember().getDeclaringClass()) ? candidate : mostSpecific;
		}

		return mostSpecific;
	}

	private static <I extends ExecutableToken<?, ?>> Set<I> resolveMostSpecificCandidateSet(
			Collection<? extends I> candidates) {
		List<I> remainingCandidates = new ArrayList<>(candidates);

		/*
		 * For each remaining candidate in the list...
		 */
		for (int first = 0; first < remainingCandidates.size(); first++) {
			I firstCandidate = remainingCandidates.get(first);

			/*
			 * Compare with each other remaining candidate...
			 */
			for (int second = first + 1; second < remainingCandidates.size(); second++) {
				I secondCandidate = remainingCandidates.get(second);

				/*
				 * Determine which of the executable members, if either, are more
				 * specific.
				 */
				Pair<Boolean, Boolean> moreSpecific = compareCandidates(firstCandidate, secondCandidate);

				if (moreSpecific.getLeft()) {
					if (moreSpecific.getRight()) {
						/*
						 * First and second are equally specific.
						 */
					} else {
						/*
						 * First is strictly more specific.
						 */
						remainingCandidates.remove(second--);
					}
				} else if (moreSpecific.getRight()) {
					/*
					 * Second is strictly more specific.
					 */
					remainingCandidates.remove(first--);

					break;
				} else {
					/*
					 * Neither first nor second are more specific.
					 */
					throw new ReflectionException(p -> p.cannotResolveAmbiguity(firstCandidate, secondCandidate));
				}
			}
		}

		return new HashSet<>(remainingCandidates);
	}

	private static Pair<Boolean, Boolean> compareCandidates(ExecutableToken<?, ?> firstCandidate,
			ExecutableToken<?, ?> secondCandidate) {
		boolean firstMoreSpecific = true;
		boolean secondMoreSpecific = true;

		if (firstCandidate.isGeneric())
			secondMoreSpecific = compareGenericCandidate(secondCandidate, firstCandidate);
		if (secondCandidate.isGeneric())
			firstMoreSpecific = compareGenericCandidate(firstCandidate, secondCandidate);

		if (!firstCandidate.isGeneric() || !secondCandidate.isGeneric()) {
			int i = 0;
			for (Type firstParameter : firstCandidate.getParameters()) {
				Type secondParameter = secondCandidate.getParameters().get(i++);

				if (!secondMoreSpecific && !secondCandidate.isGeneric()) {
					if (!Types.isAssignable(firstParameter, secondParameter)) {
						firstMoreSpecific = false;
						break;
					}
				} else if (!firstMoreSpecific && !firstCandidate.isGeneric()) {
					if (!Types.isAssignable(secondParameter, firstParameter)) {
						secondMoreSpecific = false;
						break;
					}
				} else {
					secondMoreSpecific = Types.isAssignable(secondParameter, firstParameter);
					firstMoreSpecific = Types.isAssignable(firstParameter, secondParameter);

					if (!(firstMoreSpecific || secondMoreSpecific))
						break;
				}
			}
		}

		return new Pair<>(firstMoreSpecific, secondMoreSpecific);
	}

	private static boolean compareGenericCandidate(ExecutableToken<?, ?> firstCandidate,
			ExecutableToken<?, ?> genericCandidate) {
		TypeResolver resolver = genericCandidate.getResolver();

		try {
			int parameters = firstCandidate.getParameters().size();
			if (firstCandidate.isVariableArityDefinition()) {
				parameters--;

				ConstraintFormula.reduce(Kind.SUBTYPE, firstCandidate.getParameters().get(parameters),
						genericCandidate.getParameters().get(parameters), resolver.getBounds());
			}

			for (int i = 0; i < parameters; i++) {
				ConstraintFormula.reduce(Kind.SUBTYPE, firstCandidate.getParameters().get(i),
						genericCandidate.getParameters().get(i), resolver.getBounds());
			}

			resolver.infer();
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public InvocableMemberStream<I> named(String name) {
		return filter(m -> m.getName().equals(name));
	}
}
