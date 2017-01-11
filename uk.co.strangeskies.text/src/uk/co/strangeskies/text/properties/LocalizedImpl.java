package uk.co.strangeskies.text.properties;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import uk.co.strangeskies.utilities.ObservablePropertyImpl;
import uk.co.strangeskies.utilities.ObservableValue;
import uk.co.strangeskies.utilities.Observer;

/*
 * Implementation of localized property
 */
class LocalizedImpl<A> extends ObservablePropertyImpl<Object, Object> implements Localized<Object>, Observer<Locale> {
	private final PropertyAccessorDelegate<A> propertyAccessorDelegate;

	private final PropertyAccessorConfiguration<A> source;
	private final String key;
	private final AnnotatedType propertyType;
	private final List<Object> arguments;
	private final Map<Locale, Object> cache;

	public LocalizedImpl(
			PropertyAccessorDelegate<A> propertyAccessorDelegate,
			PropertyAccessorConfiguration<A> source,
			String key,
			AnnotatedType propertyType,
			List<?> arguments) {
		super((r, t) -> r, Objects::equals, null);

		this.propertyAccessorDelegate = propertyAccessorDelegate;

		this.source = source;
		this.key = key;
		this.propertyType = getElementType(propertyType);
		this.arguments = new ArrayList<>(arguments);
		this.cache = new ConcurrentHashMap<>();

		locale().addWeakObserver(this);
		updateText(locale().get());
	}

	private AnnotatedType getElementType(AnnotatedType propertyType) {
		return ((AnnotatedParameterizedType) propertyType).getAnnotatedActualTypeArguments()[0];
	}

	private synchronized void updateText(Locale locale) {
		set(get(locale));
	}

	@Override
	public String toString() {
		return get().toString();
	}

	@Override
	public Object get(Locale locale) {
		return cache.computeIfAbsent(locale, l -> {
			return this.propertyAccessorDelegate.parseValueString(source, propertyType, key, locale).apply(arguments);
		});
	}

	@Override
	public void notify(Locale locale) {
		updateText(locale);
	}

	@Override
	public ObservableValue<Locale> locale() {
		return this.propertyAccessorDelegate.getLoader().locale();
	}
}
