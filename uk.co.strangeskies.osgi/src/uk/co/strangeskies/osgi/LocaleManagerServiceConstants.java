package uk.co.strangeskies.osgi;

import java.util.Locale;

/**
 * Constants relating to locale management.
 * 
 * @author Elias N Vasylenko
 */
public final class LocaleManagerServiceConstants {
	private LocaleManagerServiceConstants() {}

	/**
	 * Configuration pid for OSGi configuration.
	 */
	public static final String CONFIGURATION_PID = "uk.co.strangeskies.text.locale.manager";
	/**
	 * Key for locale setting string, in the format specified by
	 * {@link Locale#forLanguageTag(String)}.
	 */
	public static final String LOCALE_KEY = "locale";
}
