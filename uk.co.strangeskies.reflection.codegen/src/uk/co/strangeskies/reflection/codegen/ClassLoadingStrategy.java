package uk.co.strangeskies.reflection.codegen;

/**
 * The strategy for loading generated classes into the runtime environment.
 * 
 * @author Elias N Vasylenko
 */
public enum ClassLoadingStrategy {
	/**
	 * Inject loaded classes directly into the parent class loader of the
	 * {@link ClassSpace class space}.
	 */
	INJECT,

	/**
	 * Derive a new class loader based on the parent class loader of the
	 * {@link ClassSpace class space}.
	 */
	DERIVE
}
