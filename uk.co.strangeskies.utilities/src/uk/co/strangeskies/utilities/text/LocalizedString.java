package uk.co.strangeskies.utilities.text;

import java.util.Locale;
import java.util.stream.IntStream;

import uk.co.strangeskies.utilities.Observable;

/**
 * A localised string interface which is observable over the string data changes
 * due to updated locale.
 * 
 * @author Elias N Vasylenko
 */
public interface LocalizedString extends Observable<String>, CharSequence {
	/**
	 * @return the current locale of the string
	 */
	Locale locale();

	/**
	 * @return the localised string value according to the current locale
	 */
	@Override
	String toString();

	/**
	 * @param locale
	 *          the locale to translate to
	 * @return the localised string value according to the given locale
	 */
	String toString(Locale locale);

	@Override
	default int length() {
		return toString().length();
	}

	@Override
	default char charAt(int index) {
		return toString().charAt(index);
	}

	@Override
	default IntStream chars() {
		return toString().chars();
	}

	@Override
	default IntStream codePoints() {
		return toString().codePoints();
	}

	@Override
	default CharSequence subSequence(int start, int end) {
		return toString().subSequence(start, end);
	}
}
