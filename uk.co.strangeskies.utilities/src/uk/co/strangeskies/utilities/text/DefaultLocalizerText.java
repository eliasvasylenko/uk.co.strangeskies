package uk.co.strangeskies.utilities.text;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.Consumer;

final class DefaultLocalizerText implements LocalizerText {
	static final String TRANSLATION_NOT_FOUND = "Translation not found for method";
	static final String MUST_BE_INTERFACE = "Localization accessor must be interface";
	static final String ILLEGAL_RETURN_TYPE = "Return type must be " + LocalizedString.class.getName();

	@Override
	public Locale getLocale() {
		return Locale.ENGLISH;
	}

	@Override
	public LocalizedString translationNotFound(String method) {
		return englishString(TRANSLATION_NOT_FOUND + ": " + method);
	}

	@Override
	public LocalizedString mustBeInterface(Class<?> accessor) {
		return englishString(MUST_BE_INTERFACE + ": " + accessor);
	}

	@Override
	public LocalizedString illegalReturnType(Class<?> accessor, Method method, Class<LocalizedString> stringClass) {
		return englishString(ILLEGAL_RETURN_TYPE + ": " + method);
	}

	private LocalizedString englishString(String text) {
		return new LocalizedString() {
			@Override
			public String toString() {
				return text;
			}

			@Override
			public boolean removeObserver(Consumer<? super String> observer) {
				return true;
			}

			@Override
			public boolean addObserver(Consumer<? super String> observer) {
				return true;
			}

			@Override
			public Locale locale() {
				return Locale.ENGLISH;
			}
		};
	}

	@Override
	public boolean addObserver(Consumer<? super LocalizerText> observer) {
		return true;
	}

	@Override
	public boolean removeObserver(Consumer<? super LocalizerText> observer) {
		return true;
	}
}
