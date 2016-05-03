package uk.co.strangeskies.eclipse;

import uk.co.strangeskies.utilities.text.LocalizedString;
import uk.co.strangeskies.utilities.text.LocalizedText;

/**
 * Text resource accessor for Eclipse OSGi utilities
 * 
 * @author Elias N Vasylenko
 */
public interface EclipseUtilitiesText extends LocalizedText<EclipseUtilitiesText> {
	/**
	 * @return invalid type was annotated with {@link Localize} for localisation
	 *         supplier
	 */
	default LocalizedString invalidTypeForLocalizationSupplier() {
		return invalidTypeForLocalizationSupplier(Localize.class, LocalizedText.class);
	}

	/**
	 * @param localizeClass
	 *          the {@link Localize} class for formatting
	 * @param localizedTextClass
	 *          the {@link LocalizedText} class for formatting
	 * @return invalid type was annotated with {@link Localize} for localisation
	 *         supplier
	 */
	LocalizedString invalidTypeForLocalizationSupplier(Class<Localize> localizeClass,
			@SuppressWarnings("rawtypes") Class<LocalizedText> localizedTextClass);
}
