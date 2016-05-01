package uk.co.strangeskies.utilities.text;

import java.lang.reflect.Method;

/**
 * A {@link LocalizedText} interface to provide localized texts for use by the
 * {@link Localizer} class itself, such as for reporting errors for improperly
 * structured localisation classes, etc.
 * 
 * @author Elias N Vasylenko
 */
public interface LocalizerText extends LocalizedText<LocalizerText> {
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
