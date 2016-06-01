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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.ObservableValue;
import uk.co.strangeskies.utilities.classpath.DelegatingClassLoader;
import uk.co.strangeskies.utilities.collection.computingmap.CacheComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

class LocalizerImpl implements Localizer {
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

	private final LocaleProvider locale;
	private Log log;

	private final LocalizerText text;

	/**
	 * Create a new {@link Localizer} instance for the given initial locale.
	 * 
	 * @param locale
	 *          the initial locale
	 * @param log
	 *          the log for localisation
	 */
	public LocalizerImpl(LocaleProvider locale, Log log) {
		try {
			methodHandleConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw log(Level.ERROR, CANNOT_INITIALISE, new RuntimeException(e));
		}
		if (!methodHandleConstructor.isAccessible()) {
			methodHandleConstructor.setAccessible(true);
		}

		LOCALIZATION_CACHE = new CacheComputingMap<Localizable<?>, LocalizedText<?>>(this::localize, true);

		this.locale = locale;
		this.log = log;

		text = getLocalization(LocalizerText.class);

		if (log != null) {
			locale().addObserver(l -> {
				log.log(Level.INFO, getText().localeChanged(locale, getLocale()).toString());
			});
		}
	}

	public LocalizerText getText() {
		return text;
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

	@Override
	public Locale getLocale() {
		return locale.getLocale();
	}

	@Override
	public ObservableValue<Locale> locale() {
		return locale;
	}

	@SuppressWarnings("unchecked")
	private <U extends LocalizedText<U>> U unsafeLocalize(Class<?> returnType, LocalizedResourceBundle bundle) {
		return localize(new Localizable<>((Class<U>) returnType, bundle));
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
			throw log(Level.ERROR, new LocalizationException(text.mustBeInterface(localizable.accessor)));
		}

		Map<MethodSignature, LocalizedText<?>> nestedMembers = new HashMap<>();

		for (Method method : localizable.accessor.getMethods()) {
			MethodSignature signature = new MethodSignature(method);

			if (!LOCALIZATION_HELPER_METHODS.contains(signature) && !method.isDefault()) {
				/*
				 * ensure return type of method is String
				 */
				Class<?> returnType = method.getReturnType();
				if (LocalizedText.class.isAssignableFrom(returnType)) {
					LocalizedText<?> nestedAccessor = unsafeLocalize(returnType, localizable.bundle);
					nestedMembers.put(signature, nestedAccessor);
				} else if (!LocalizedString.class.equals(returnType)) {
					throw log(Level.ERROR, new LocalizationException(text.illegalReturnType(localizable.accessor, method)));
				}
			}
		}

		ClassLoader classLoader = new DelegatingClassLoader(getClass().getClassLoader(),
				localizable.accessor.getClassLoader());

		IdentityProperty<LocalizationTextDelegate<T>> helper = new IdentityProperty<>();

		T proxy = (T) Proxy.newProxyInstance(classLoader, new Class<?>[] { localizable.accessor },
				(Object p, Method method, Object[] args) -> {
					MethodSignature signature = new MethodSignature(method);

					if (LOCALIZATION_HELPER_METHODS.contains(signature)) {
						return method.invoke(helper.get(), args);
					}

					if (method.isDefault()) {
						return methodHandleConstructor.newInstance(method.getDeclaringClass(), MethodHandles.Lookup.PRIVATE)
								.unreflectSpecial(method, method.getDeclaringClass()).bindTo(p).invokeWithArguments(args);
					}

					if (nestedMembers.containsKey(signature)) {
						return nestedMembers.get(signature);
					}

					return helper.get().getTranslation(signature, args);
				});

		helper.set(new LocalizationTextDelegate<>(this, proxy, localizable.bundle, text));

		return proxy;
	}

	@SuppressWarnings("unchecked")
	private <T extends LocalizedText<T>> T getLocalization(Localizable<T> localizable) {
		return (T) LOCALIZATION_CACHE.putGet(localizable);
	}

	@Override
	public <T extends LocalizedText<T>> T getLocalization(Class<T> accessor, LocalizedResourceBundle bundle) {
		return getLocalization(new Localizable<>(accessor, bundle));
	}
}
