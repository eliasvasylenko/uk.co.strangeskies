package uk.co.strangeskies.gears.mathematics.expression;

import uk.co.strangeskies.gears.utilities.Self;

/**
 * A variable for use in reactive programming. A Variable in this sense is a
 * first class expression, that is to say it is an expression whose value is
 * itself.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <S>
 *          See {@link Self} for more information. This must be self-bounding as
 *          the value of the expression is the variable itself.
 */
public interface Variable<S extends Variable<S>> extends Expression<S>, Self<S> {
}
