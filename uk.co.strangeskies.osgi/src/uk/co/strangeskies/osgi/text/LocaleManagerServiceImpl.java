package uk.co.strangeskies.osgi.text;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.text.LocaleManager;
import uk.co.strangeskies.utilities.text.Localizer;

@Component(service = LocaleManagerServiceImpl.class)
class LocaleManagerServiceImpl implements LocaleManager {
	private final LocaleManager component;
	private Log log;

	public LocaleManagerServiceImpl() {
		component = LocaleManager.getManager(Locale.getDefault(), new Log() {
			@Override
			public void log(Level level, String message) {
				if (log != null) {
					log.log(level, message);
				}
			}

			@Override
			public void log(Level level, String message, Throwable exception) {
				if (log != null) {
					log.log(level, message, exception);
				}
			}
		});
	}

	@Override
	public Locale getLocale() {
		return component.getLocale();
	}

	@Override
	public void setLocale(Locale locale) {
		if (!locale.equals(getLocale())) {
			setLocaleImpl(locale);

			// TODO update config admin service
		}
	}

	private void setLocaleImpl(Locale locale) {
		component.setLocale(locale);
	}

	@Modified
	void update(Map<String, String> configuration) {
		// TODO configure locale
		// setLocaleImpl(newLocale);
	}

	@Override
	public Localizer getLocalizer() {
		return component.getLocalizer();
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	void setLog(Log log) {
		this.log = log;
	}

	void unsetLog(Log log) {
		if (log == this.log) {
			setLog(null);
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
