package uk.co.strangeskies.osgi.text;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.osgi.LocaleManagerServiceConstants;
import uk.co.strangeskies.utilities.text.LocaleManager;
import uk.co.strangeskies.utilities.text.LocaleProvider;

/**
 * A locale manager configurable via the config admin service.
 * 
 * @author Elias N Vasylenko
 */
@Component(configurationPid = LocaleManagerServiceConstants.CONFIGURATION_PID)
public class LocaleManagerService implements LocaleManager, LocaleProvider {
	private final LocaleManager component;
	@Reference
	ConfigurationAdmin configurationAdmin;

	/**
	 * Create empty manager
	 */
	public LocaleManagerService() {
		component = LocaleManager.getManager(Locale.getDefault());
	}

	@Override
	public Locale getLocale() {
		return component.getLocale();
	}

	@Override
	public void setLocale(Locale locale) {
		if (!locale.equals(getLocale())) {
			setLocaleImpl(locale);

			try {
				Configuration configuration = configurationAdmin
						.getConfiguration(LocaleManagerServiceConstants.CONFIGURATION_PID);

				Dictionary<String, Object> properties = configuration.getProperties();
				properties.put(LocaleManagerServiceConstants.LOCALE_KEY, locale.toLanguageTag());
				configuration.update(properties);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void setLocaleImpl(Locale locale) {
		component.setLocale(locale);
	}

	@Modified
	void update(Map<String, String> configuration) {
		if (configuration != null) {
			String locale = configuration.get(LocaleManagerServiceConstants.LOCALE_KEY);
			if (locale != null) {
				setLocaleImpl(Locale.forLanguageTag(locale));
			}
		}
	}

	@Override
	public boolean addObserver(Consumer<? super Locale> observer) {
		return component.addObserver(observer);
	}

	@Override
	public boolean removeObserver(Consumer<? super Locale> observer) {
		return component.removeObserver(observer);
	}
}
