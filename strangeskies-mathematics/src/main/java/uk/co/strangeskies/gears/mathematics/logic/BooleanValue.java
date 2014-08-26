package uk.co.strangeskies.gears.mathematics.logic;

import uk.co.strangeskies.gears.mathematics.expression.MutableExpression;
import uk.co.strangeskies.gears.utilities.Self;

public class BooleanValue extends MutableExpression<BooleanValue>
		implements BooleanCombinationBehaviour<BooleanValue, BooleanValue>,
		NOTable<BooleanValue, BooleanValue>, Self<BooleanValue> {
	private boolean value;

	public BooleanValue() {
	}

	public BooleanValue(Boolean value) {
		this.value = value;
	}

	public BooleanValue(BooleanValue expression) {
		value = expression.getValue().getBooleanValue();
	}

	@Override
	public final BooleanValue getValue() {
		return this;
	}

	public final BooleanValue getDecoupledValue() {
		return copy();
	}

	@Override
	public final BooleanValue copy() {
		return new BooleanValue(this);
	}

	public final BooleanValue getConst() {
		return this;
	}

	public final boolean getBooleanValue() {
		return value;
	}

	public final void setValue(boolean value) {
		this.value = value;
	}

	public final void setValue(Boolean value) {
		this.value = value;
	}

	public final void setValue(BooleanValue value) {
		this.value = value.getValue().getBooleanValue();
	}

	public final void setValue(Condition condition) {
		value = condition.isFulfilled();
	}

	@Override
	public final BooleanValue and(BooleanValue expression) {
		value = value && expression.getValue().getBooleanValue();

		return this;
	}

	@Override
	public final BooleanValue getAnd(BooleanValue expression) {
		return copy().and(expression);
	}

	@Override
	public final BooleanValue getOr(BooleanValue expression) {
		return copy().or(expression);
	}

	@Override
	public final BooleanValue getXor(BooleanValue expression) {
		return copy().xor(expression);
	}

	@Override
	public final BooleanValue getNot() {
		return copy().not();
	}

	@Override
	public final BooleanValue getNand(BooleanValue expression) {
		return copy().nand(expression);
	}

	@Override
	public final BooleanValue getNor(BooleanValue expression) {
		return copy().nor(expression);
	}

	@Override
	public final BooleanValue getXnor(BooleanValue expression) {
		return copy().xnor(expression);
	}

	@Override
	public final BooleanValue or(BooleanValue expression) {
		value = value || expression.getValue().getBooleanValue();

		return this;
	}

	@Override
	public final BooleanValue xor(BooleanValue expression) {
		value = value ^ expression.getValue().getBooleanValue();

		return this;
	}

	@Override
	public final BooleanValue not() {
		value = !value;

		return this;
	}

	@Override
	public final BooleanValue nand(BooleanValue expression) {
		value = !(value && expression.getValue().getBooleanValue());

		return this;
	}

	@Override
	public final BooleanValue nor(BooleanValue expression) {
		value = !(value || expression.getValue().getBooleanValue());

		return this;
	}

	@Override
	public final BooleanValue xnor(BooleanValue expression) {
		value = value == expression.getValue().getBooleanValue();

		return this;
	}
}
