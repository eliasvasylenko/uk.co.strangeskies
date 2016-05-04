package uk.co.strangeskies.osgi;

import java.util.Locale;

import uk.co.strangeskies.utilities.text.LocalizedRuntimeException;
import uk.co.strangeskies.utilities.text.LocalizedString;

/**
 * A localised exception class for dealing with general service wiring and
 * provision issues.
 * 
 * @author Elias N Vasylenko
 */
public class ServiceWiringException extends LocalizedRuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Build with the given developer locale
	 * 
	 * @param message
	 *          the localised message string
	 * @param developerLocale
	 *          the developer's locale
	 * @param cause
	 *          the cause
	 */
	protected ServiceWiringException(LocalizedString message, Locale developerLocale, Throwable cause) {
		super(message, developerLocale, cause);
	}

	/**
	 * Build with the given developer locale
	 * 
	 * @param message
	 *          the localised message string
	 * @param developerLocale
	 *          the developer's locale
	 */
	protected ServiceWiringException(LocalizedString message, Locale developerLocale) {
		super(message, developerLocale);
	}

	/**
	 * Build with an English developer locale
	 * 
	 * @param message
	 *          the localised message string
	 * @param cause
	 *          the cause
	 */
	public ServiceWiringException(LocalizedString message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Build with an English developer locale
	 * 
	 * @param message
	 *          the localised message string
	 */
	public ServiceWiringException(LocalizedString message) {
		super(message);
	}
}
