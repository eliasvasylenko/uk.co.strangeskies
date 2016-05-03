/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.text;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;
import uk.co.strangeskies.utilities.collection.computingmap.CacheComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

/**
 * This class represents a simple but powerful system for internationalisation.
 * Instances of this class provide automatic implementations of sub-interfaces
 * of {@link LocalizedText} according to a {@link Locale} setting, which
 * delegate method invocations to be fetched from {@link ResourceBundle resource
 * bundles}.
 * 
 * @author Elias N Vasylenko
 */
public class Localizer {
	static class MethodSignature {
		private final Method method;
		private final Class<?>[] type;

		public MethodSignature(Method method) {
			this.method = method;
			this.type = method.getParameterTypes();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (!(obj instanceof MethodSignature))
				return false;

			MethodSignature other = (MethodSignature) obj;

			return name().equals(other.name()) && Arrays.equals(type(), other.type());
		}

		public Method method() {
			return method;
		}

		public String name() {
			return method.getName();
		}

		public Class<?>[] type() {
			return type;
		}

		@Override
		public int hashCode() {
			return name().hashCode() ^ Arrays.hashCode(type());
		}
	}

	static class Localizable<T extends LocalizedText<T>> {
		public final Class<T> accessor;
		public final LocalizedResourceBundle bundle;

		public Localizable(Class<T> accessor, LocalizedResourceBundle bundle) {
			this.accessor = accessor;
			this.bundle = bundle;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (!(obj instanceof Localizable))
				return false;

			Localizable<?> other = (Localizable<?>) obj;

			return accessor.equals(other.accessor) && bundle.equals(other.bundle);
		}

		@Override
		public int hashCode() {
			return accessor.hashCode() ^ bundle.hashCode();
		}
	}

	private static final String TEXT_POSTFIX = "Text";
	private static final Set<MethodSignature> LOCALIZATION_HELPER_METHODS = new HashSet<>();
	private static final String CANNOT_INITIALISE = "Cannot initialise Localizer instance";

	{
		for (Method method : LocalizedText.class.getMethods()) {
			LOCALIZATION_HELPER_METHODS.add(new MethodSignature(method));
		}
		for (Method method : Object.class.getMethods()) {
			LOCALIZATION_HELPER_METHODS.add(new MethodSignature(method));
		}
	}

	private final ComputingMap<Localizable<?>, LocalizedText<?>> LOCALIZATION_CACHE;
	private final Constructor<MethodHandles.Lookup> methodHandleConstructor;

	private Log log;
	private Locale locale;
	private final ObservableImpl<Locale> localeChanges;
	private final LocalizerText text;

	/**
	 * Create a new {@link Localizer} instance for the JVM default locale.
	 */
	public Localizer() {
		this(Locale.getDefault(), null);
	}

	/**
	 * Create a new {@link Localizer} instance for the given initial locale.
	 * 
	 * @param locale
	 *          the initial locale
	 */
	public Localizer(Locale locale) {
		this(locale, null);
	}

	/**
	 * Create a new {@link Localizer} instance for the given initial locale.
	 * 
	 * @param locale
	 *          the initial locale
	 * @param log
	 *          the log for localisation
	 */
	public Localizer(Locale locale, Log log) {
		try {
			methodHandleConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw log(Level.ERROR, CANNOT_INITIALISE, new RuntimeException(e));
		}
		if (!methodHandleConstructor.isAccessible()) {
			methodHandleConstructor.setAccessible(true);
		}

		LOCALIZATION_CACHE = new CacheComputingMap<Localizable<?>, LocalizedText<?>>(this::localize, true);

		localeChanges = new ObservableImpl<>();
		setLocale(locale);
		this.log = log;

		text = getLocalization(LocalizerText.class);
	}

	/**
	 * @return the current log
	 */
	public Log getLog() {
		return log;
	}

	/**
	 * @param log
	 *          the log for localisation
	 */
	public void setLog(Log log) {
		this.log = log;
	}

	void log(Level level, String message) {
		if (log != null) {
			log.log(level, message);
		}
	}

	<E extends Throwable> E log(Level level, E exception) {
		if (log != null) {
			log.log(level, exception);
		}
		return exception;
	}

	<E extends Throwable> E log(Level level, String message, E exception) {
		if (log != null) {
			log.log(level, message, exception);
		}
		return exception;
	}

	/**
	 * @return the current locale of all localised texts implemented by this
	 *         {@link Localizer}
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale
	 *          the new locale, to be applied to all existing and future localised
	 *          texts implemented by this {@link Localizer}
	 */
	public void setLocale(Locale locale) {
		if (this.locale != locale) {
			this.locale = locale;
			localeChanges.fire(locale);
		}
	}

	/**
	 * @return an observable over changes to the locale
	 */
	public Observable<Locale> locale() {
		return localeChanges;
	}

	@SuppressWarnings("unchecked")
	protected <T extends LocalizedText<T>> T localize(Localizable<T> localizable) {
		LocalizerText text;
		if (this.text == null) {
			text = new DefaultLocalizerText();
		} else {
			text = this.text;
		}

		if (!localizable.accessor.isInterface()) {
			throw log(Level.ERROR, new IllegalArgumentException(text.mustBeInterface(localizable.accessor).toString()));
		}

		for (Method method : localizable.accessor.getMethods()) {
			MethodSignature signature = new MethodSignature(method);

			if (!LOCALIZATION_HELPER_METHODS.contains(signature) && !method.isDefault()) {
				/*
				 * ensure return type of method is String
				 */
				if (!method.getReturnType().equals(LocalizedString.class)) {
					throw log(Level.ERROR,
							new IllegalArgumentException(text.illegalReturnType(localizable.accessor, method).toString()));
				}
			}
		}

		IdentityProperty<LocalizationTextDelegate<T>> helper = new IdentityProperty<>();

		T proxy = (T) Proxy.newProxyInstance(localizable.accessor.getClassLoader(), new Class<?>[] { localizable.accessor },
				(Object p, Method method, Object[] args) -> {
					MethodSignature signature = new MethodSignature(method);

					if (LOCALIZATION_HELPER_METHODS.contains(signature)) {
						return method.invoke(helper.get(), args);
					}

					if (method.isDefault()) {
						return methodHandleConstructor.newInstance(method.getDeclaringClass(), MethodHandles.Lookup.PRIVATE)
								.unreflectSpecial(method, method.getDeclaringClass()).bindTo(p).invokeWithArguments(args);
					}

					return helper.get().getTranslation(signature, args);
				});

		helper.set(new LocalizationTextDelegate<>(this, proxy, localizable.bundle, text));

		return proxy;
	}

	private static String removeTextPostfix(String string) {
		if (string.endsWith(TEXT_POSTFIX) && !string.equals(TEXT_POSTFIX)) {
			string = string.substring(0, string.length() - TEXT_POSTFIX.length());
		}
		return string;
	}

	/**
	 * The default translation scheme to generate resource keys from methods.
	 * <p>
	 * The class name and method name are split into words according to camel-case
	 * rules and put into lower-case, then the word {@code "text"} is removed from
	 * the end of the class name if present, then each word from the class and
	 * each word from the method are joined in order about the {@code "."}
	 * character.
	 * <p>
	 * For example the method
	 * {@link LocalizerText#illegalReturnType(Class, java.lang.reflect.Method)}
	 * would generate the key {@code "localizer.illegal.return.type"}. The key
	 * generation rules can be overridden for a given method by annotating with
	 * {@link LocalizationKey}.
	 * 
	 * @param method
	 *          the method for which we wish to determine an associated resource
	 *          key
	 * @return the default resource key for the given method
	 */
	public static String getKey(Method method) {
		LocalizationKey keyAnnotation = method.getAnnotation(LocalizationKey.class);
		if (keyAnnotation != null) {
			return keyAnnotation.value();
		}

		String className = removeTextPostfix(method.getDeclaringClass().getSimpleName());

		return getKeyText(className) + '.' + getKeyText(method.getName());
	}

	private static String getKeyText(String string) {
		StringBuilder builder = new StringBuilder();

		int copiedToIndex = 0;
		boolean isPreviousCharacterUpperCase = false;

		for (int i = 0; i < string.length(); i++) {
			boolean isCharacterUpperCase = Character.isUpperCase(string.charAt(i));

			int copyToIndex = copiedToIndex;

			if (isCharacterUpperCase && !isPreviousCharacterUpperCase) {
				copyToIndex = i;
			} else if (!isCharacterUpperCase && isPreviousCharacterUpperCase) {
				copyToIndex = i - 1;
			}

			if (copyToIndex > copiedToIndex) {
				if (builder.length() > 0) {
					builder.append('.');
				}
				builder.append(string.substring(copiedToIndex, copyToIndex));
				copiedToIndex = copyToIndex;
			}

			isPreviousCharacterUpperCase = isCharacterUpperCase;
		}

		if (builder.length() > 0) {
			builder.append('.');
		}
		builder.append(string.substring(copiedToIndex));

		return builder.toString().toLowerCase();
	}

	@SuppressWarnings("unchecked")
	private <T extends LocalizedText<T>> T getLocalization(Localizable<T> localizable) {
		return (T) LOCALIZATION_CACHE.putGet(localizable);
	}

	/**
	 * Generate an implementing instance of the given accessor interface class,
	 * according to the rules described by {@link LocalizedText}, and with
	 * resource values taken from the given resource bundle.
	 * 
	 * @param <T>
	 *          the type of the localisation text accessor interface
	 * @param accessor
	 *          the sub-interface of {@link LocalizedText} we wish to implement
	 * @param bundle
	 *          the resource bundle with which to load resources
	 * @return an implementation of the accessor interface
	 */
	public <T extends LocalizedText<T>> T getLocalization(Class<T> accessor, LocalizedResourceBundle bundle) {
		return getLocalization(new Localizable<>(accessor, bundle));
	}

	/**
	 * Generate an implementing instance of the given accessor interface class,
	 * according to the rules described by {@link LocalizedText}. Resources are
	 * loading according to the {@link LocalizedResourceBundle} returned from
	 * {@link LocalizedResourceBundle#getBundle(ClassLoader, Locale, String[])}
	 * invoked with the given class loader and locations, and the current locale.
	 * 
	 * @param <T>
	 *          the type of the localisation text accessor interface
	 * @param accessor
	 *          the sub-interface of {@link LocalizedText} we wish to implement
	 * @param classLoader
	 *          the class loader to fetch resource bundles from
	 * @param locations
	 *          the base names of all backing resource bundles
	 * @return an implementation of the accessor interface
	 */
	public <T extends LocalizedText<T>> T getLocalization(Class<T> accessor, ClassLoader classLoader,
			String... locations) {
		return getLocalization(accessor, LocalizedResourceBundle.getBundle(classLoader, locale, locations));
	}

	/**
	 * Generate an implementing instance of the given accessor interface class,
	 * according to the rules described by {@link LocalizedText}. Resources are
	 * loading according to the {@link LocalizedResourceBundle} returned from
	 * {@link LocalizedResourceBundle#getBundle(ClassLoader, Locale, String[])}
	 * invoked with the class loader of the given class, and the given locations.
	 * 
	 * @param <T>
	 *          the type of the localisation text accessor interface
	 * @param accessor
	 *          the sub-interface of {@link LocalizedText} we wish to implement
	 * @param locations
	 *          the base names of all backing resource bundles
	 * @return an implementation of the accessor interface
	 */
	public <T extends LocalizedText<T>> T getLocalization(Class<T> accessor, String... locations) {
		return getLocalization(accessor, accessor.getClassLoader(), locations);
	}

	/**
	 * Generate an implementing instance of the given accessor interface class,
	 * according to the rules described by {@link LocalizedText}. Resources are
	 * loading according to the {@link LocalizedResourceBundle} returned from
	 * {@link LocalizedResourceBundle#getBundle(ClassLoader, Locale, String[])}
	 * invoked with the class loader of the given class, and the location derived
	 * by taking the given class name, and removing {@code Text} from the end if
	 * it is present.
	 * 
	 * @param <T>
	 *          the type of the localisation text accessor interface
	 * @param accessor
	 *          the sub-interface of {@link LocalizedText} we wish to implement
	 * @return an implementation of the accessor interface
	 */
	public <T extends LocalizedText<T>> T getLocalization(Class<T> accessor) {
		return getLocalization(accessor, removeTextPostfix(accessor.getName()));
	}
}
