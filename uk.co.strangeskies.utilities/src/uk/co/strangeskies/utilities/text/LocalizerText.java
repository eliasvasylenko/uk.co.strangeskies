package uk.co.strangeskies.utilities.text;

import java.lang.reflect.Method;

public interface LocalizerText extends LocalizationText<LocalizerText> {
	LocalizedString mustBeInterface(Class<?> accessor);

	LocalizedString illegalReturnType(Class<?> accessor, Method method, Class<LocalizedString> stringClass);

	default LocalizedString illegalReturnType(Class<?> accessor, Method method) {
		return illegalReturnType(accessor, method, LocalizedString.class);
	}

	LocalizedString translationNotFound(String method);

	default LocalizedString translationNotFound(Method method) {
		return translationNotFound(Localizer.getKey(method));
	}
}
