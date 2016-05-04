package uk.co.strangeskies.osgi.text;

import java.util.Locale;
import java.util.function.Consumer;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import uk.co.strangeskies.utilities.text.LocaleManager;
import uk.co.strangeskies.utilities.text.Localizer;

/**
 * An implementation of {@link LocaleManager} which manages a locale state
 * configurable through the config admin service.
 * 
 * @author Elias N Vasylenko
 */
@Component(scope = ServiceScope.PROTOTYPE)
public class LocaleManagerService implements LocaleManager {
	@Reference
	LocaleManagerServiceImpl manager;
	private ComponentContext context;

	@Override
	public boolean addObserver(Consumer<? super Locale> observer) {
		return manager.addObserver(observer);
	}

	@Override
	public boolean removeObserver(Consumer<? super Locale> observer) {
		return manager.removeObserver(observer);
	}

	@Override
	public void setLocale(Locale locale) {
		manager.setLocale(locale);
	}

	@Override
	public Locale getLocale() {
		return manager.getLocale();
	}

	@Override
	public Localizer getLocalizer() {
		LocalizerService localizer = new LocalizerService(this);
		return localizer;
	}

	@Activate
	void activate(ComponentContext context) {
		this.context = context;
	}

	/**
	 * @return the {@link LocaleManagerServiceImpl} instance backing this service
	 */
	LocaleManagerServiceImpl getBackingManager() {
		return manager;
	}

	/**
	 * @return the component context this service was initialised with
	 */
	ComponentContext getComponentContext() {
		return context;
	}
}
