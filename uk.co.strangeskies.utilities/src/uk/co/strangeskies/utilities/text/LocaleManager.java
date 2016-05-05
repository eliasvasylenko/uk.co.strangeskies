package uk.co.strangeskies.utilities.text;

import java.util.Locale;

import uk.co.strangeskies.utilities.ObservableImpl;

/**
 * Management interface over and associate {@link Localizer localiser instance},
 * allowing the locale of that instance to be changed.
 * <p>
 * A locale manager is observable over changes to its locale.
 * 
 * @author Elias N Vasylenko
 */
public interface LocaleManager extends LocaleProvider {
	/**
	 * @param locale
	 *          the new locale
	 */
	void setLocale(Locale locale);

	/**
	 * @return a simple mutable locale manager, with its locale initialised to the
	 *         system default
	 */
	static LocaleManager getManager() {
		return getManager(Locale.getDefault());
	}

	/**
	 * @param locale
	 *          the initial locale
	 * @return a simple mutable locale manager
	 */
	static LocaleManager getManager(Locale locale) {
		return new LocaleManagerImpl(locale);
	}

	/**
	 * Create a locale manager based on the system default locale, as returned by
	 * {@link Locale#getDefault()}. Changes made to this manager will be forwarded
	 * to the system locale setting, and vice versa.
	 * <p>
	 * Unfortunately there is no standard mechanism to listen for changes to the
	 * system locale, so the manager may not stay up to date until it is refreshed
	 * by a call to {@link #setLocale(Locale)} or {@link #getLocale()}.
	 * 
	 * @return a locale manager backed by the system locale
	 */
	static LocaleManager getDefaultManager() {
		return new DefaultLocaleManagerImpl();
	}

}

class LocaleManagerImpl extends ObservableImpl<Locale> implements LocaleManager {
	private Locale currentLocale;

	public LocaleManagerImpl(Locale locale) {
		currentLocale = locale;
	}

	@Override
	public synchronized void setLocale(Locale locale) {
		if (!locale.equals(currentLocale)) {
			currentLocale = locale;
			fire(locale);
		}
	}

	@Override
	public synchronized Locale getLocale() {
		return currentLocale;
	}
}

class DefaultLocaleManagerImpl extends LocaleManagerImpl {
	public DefaultLocaleManagerImpl() {
		super(Locale.getDefault());
	}

	@Override
	public synchronized void setLocale(Locale locale) {
		Locale.setDefault(locale);
		super.setLocale(locale);
	}

	@Override
	public synchronized Locale getLocale() {
		Locale locale = Locale.getDefault();

		/*
		 * Actual system locale may have changed, since we can't listen to events,
		 * so make sure we're synced here.
		 */
		setLocale(locale);

		return locale;
	}
}
