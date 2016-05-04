package uk.co.strangeskies.eclipse;

import uk.co.strangeskies.utilities.text.LocalizedString;
import uk.co.strangeskies.utilities.text.LocalizedText;

/**
 * Text resource accessor for Eclipse OSGi utilities
 * 
 * @author Elias N Vasylenko
 */
public interface LocalizationSupplierText extends LocalizedText<LocalizationSupplierText> {
	/**
	 * @return invalid type was annotated with {@link Localize} for localisation
	 *         supplier
	 */
	default LocalizedString illegalInjectionTarget() {
		return illegalInjectionTarget(Localize.class, LocalizedText.class);
	}

	/**
	 * @param localizeClass
	 *          the {@link Localize} class for formatting
	 * @param localizedTextClass
	 *          the {@link LocalizedText} class for formatting
	 * @return invalid type was annotated with {@link Localize} for localisation
	 *         supplier
	 */
	LocalizedString illegalInjectionTarget(Class<Localize> localizeClass,
			@SuppressWarnings("rawtypes") Class<LocalizedText> localizedTextClass);

	/**
	 * @return an unexpected error occurred
	 */
	LocalizedString unexpectedError();
}
