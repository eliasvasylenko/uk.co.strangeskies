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
	/**
	 * @param accessor
	 *          the accessor to localise
	 * @return the accessor class must be an interface
	 */
	LocalizedString mustBeInterface(Class<?> accessor);

	/**
	 * @param accessor
	 *          the accessor to localise
	 * @param method
	 *          the method with the illegal return type
	 * @param stringClass
	 *          the expected return type
	 * @return the method must return the expected type
	 */
	LocalizedString illegalReturnType(Class<?> accessor, Method method, Class<LocalizedString> stringClass);

	/**
	 * @param accessor
	 *          the accessor to localise
	 * @param method
	 *          the method with the illegal return type
	 * @return the method must return the expected type
	 */
	default LocalizedString illegalReturnType(Class<?> accessor, Method method) {
		return illegalReturnType(accessor, method, LocalizedString.class);
	}

	/**
	 * @param method
	 *          the method to find a translation for
	 * @return no translation found for the given method
	 */
	LocalizedString translationNotFound(String method);

	/**
	 * @param method
	 *          the method to find a translation for
	 * @return no translation found for the given method
	 */
	default LocalizedString translationNotFound(Method method) {
		return translationNotFound(Localizer.getKey(method));
	}
}
