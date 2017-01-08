package uk.co.strangeskies.reflection.codegen;

import static java.util.Objects.hash;

import java.lang.reflect.Modifier;

import uk.co.strangeskies.reflection.Visibility;

public class Modifiers {
	private final int modifiers;
	private final boolean isDefault;

	private Modifiers() {
		modifiers = 0;
		isDefault = false;
	}

	public Modifiers(int modifiers, boolean isDefault) {
		this.modifiers = modifiers;
		this.isDefault = isDefault;
	}

	public static Modifiers modifiers(int modifiers) {
		return new Modifiers(modifiers, false);
	}

	public static Modifiers emptyModifiers() {
		return new Modifiers();
	}

	private Modifiers with(int modifier, boolean present) {
		if (present) {
			return new Modifiers(modifiers | modifier, isDefault);
		} else {
			return new Modifiers(modifiers & ~modifier, isDefault);
		}
	}

	public Visibility getVisibility() {
		return Visibility.forModifiers(modifiers);
	}

	public Modifiers withVisibility(Visibility visibility) {
		return new Modifiers(visibility.withModifiers(modifiers), isDefault);
	}

	public boolean isStatic() {
		return Modifier.isStatic(modifiers);
	}

	public Modifiers withStatic(boolean isStatic) {
		return with(Modifier.STATIC, isStatic);
	}

	public boolean isFinal() {
		return Modifier.isFinal(modifiers);
	}

	public Modifiers withFinal(boolean isFinal) {
		return with(Modifier.FINAL, isFinal);
	}

	public boolean isTransient() {
		return Modifier.isTransient(modifiers);
	}

	public Modifiers withTransient(boolean isTransient) {
		return with(Modifier.TRANSIENT, isTransient);
	}

	public boolean isVolatile() {
		return Modifier.isVolatile(modifiers);
	}

	public Modifiers withVolatile(boolean isVolatile) {
		return with(Modifier.VOLATILE, isVolatile);
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(modifiers);
	}

	public Modifiers withAbstract(boolean isAbstract) {
		return with(Modifier.ABSTRACT, isAbstract);
	}

	public boolean isSynchronized() {
		return Modifier.isSynchronized(modifiers);
	}

	public Modifiers withSynchronized(boolean isSynchronized) {
		return with(Modifier.SYNCHRONIZED, isSynchronized);
	}

	public boolean isStrict() {
		return Modifier.isStrict(modifiers);
	}

	public Modifiers withStrict(boolean isStrict) {
		return with(Modifier.STRICT, isStrict);
	}

	public boolean isNative() {
		return Modifier.isNative(modifiers);
	}

	public Modifiers withNative(boolean isNative) {
		return with(Modifier.NATIVE, isNative);
	}

	public boolean isDefault() {
		return isDefault;
	}

	public Modifiers withDefault(boolean isDefault) {
		return new Modifiers(modifiers, isDefault);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Modifiers))
			return false;

		Modifiers that = (Modifiers) obj;

		return this.isDefault == that.isDefault && this.modifiers == that.modifiers;
	}

	@Override
	public int hashCode() {
		return hash(isDefault, modifiers);
	}
}
