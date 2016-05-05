package uk.co.strangeskies.utilities.text;

import java.util.Locale;

import uk.co.strangeskies.utilities.Observable;

/**
 * Management interface over and associate {@link Localizer localiser instance},
 * allowing the locale of that instance to be changed.
 * <p>
 * A locale manager is observable over changes to its locale.
 * 
 * @author Elias N Vasylenko
 */
public interface LocaleProvider extends Observable<Locale> {
	/**
	 * @return the current locale
	 */
	Locale getLocale();
}
