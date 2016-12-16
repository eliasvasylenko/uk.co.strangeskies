package uk.co.strangeskies.reflection;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;

public enum Visibility {
	PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE;

	public boolean visibilityIsAtLeast(Visibility visibility) {
		return ordinal() <= visibility.ordinal();
	}

	public boolean visibilityIsAtMost(Visibility visibility) {
		return ordinal() >= visibility.ordinal();
	}

	public Optional<String> getKeyword() {
		switch (this) {
		case PUBLIC:
			return of("public");
		case PROTECTED:
			return of("protected");
		case PACKAGE_PRIVATE:
			return empty();
		case PRIVATE:
			return of("private");
		default:
			throw new AssertionError();
		}
	}
}
