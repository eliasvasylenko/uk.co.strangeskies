package uk.co.strangeskies.utilities.text;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.ObservableImpl;
import uk.co.strangeskies.utilities.text.Localizer.MethodSignature;

/*
 * Delegate implementation object for proxy instances of LocalizationText classes
 */
public class LocalizationTextDelegate<T extends LocalizedText<T>> extends ObservableImpl<T>
		implements LocalizedText<T> {
	/*
	 * Implementation of localised string
	 */
	class LocalizedStringImpl extends ObservableImpl<String> implements LocalizedString, Consumer<T> {
		private final MethodSignature signature;
		private final Object[] args;

		private String string;

		public LocalizedStringImpl(MethodSignature signature, Object[] args) {
			this.signature = signature;
			this.args = args;

			updateText();

			LocalizationTextDelegate.this.addWeakObserver(this);
		}

		private void updateText() {
			String translationText = translations.get(signature);
			if (translationText == null) {
				translationText = loadTranslation(Localizer.getKey(signature.method()));
				if (translationText != null) {
					translations.put(signature, translationText);
				}
			}

			if (translationText == null) {
				if (signature.method().getDeclaringClass().equals(LocalizerText.class)) {
					try {
						translationText = ((LocalizedString) signature.method().invoke(text, args)).toString();
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
				}

				if (translationText == null) {
					translationText = text.translationNotFound(signature.method()).toString();
				}
			}

			translationText = String.format(locale, translationText, args);

			if (!translationText.equals(string)) {
				string = translationText;
				fire(string);
			}
		}

		@Override
		public String toString() {
			return string;
		}

		@Override
		public void accept(T t) {
			updateText();
		}

		@Override
		public Locale locale() {
			return locale;
		}
	}

	private Locale locale;
	private LocalizedResourceBundle bundle;

	private final Map<MethodSignature, String> translations;

	private final T proxy;
	private final LocalizerText text;

	private final Consumer<Locale> observer;

	public LocalizationTextDelegate(Localizer localizer, T proxy, LocalizedResourceBundle bundle, LocalizerText text) {
		locale = localizer.getLocale();
		this.proxy = proxy;
		this.bundle = bundle;
		this.text = text;

		translations = new ConcurrentHashMap<>();

		observer = l -> {
			setLocale(l);
			translations.clear();
			fire(proxy);
		};
		localizer.localeChanges.addWeakObserver(observer);
		setLocale(locale);
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
		bundle = bundle.withLocale(locale);
	}

	private String loadTranslation(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {}

		return null;
	}

	public LocalizedString getTranslation(MethodSignature signature, Object[] args) {
		return new LocalizedStringImpl(signature, args);
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public T copy() {
		return proxy;
	}
}
