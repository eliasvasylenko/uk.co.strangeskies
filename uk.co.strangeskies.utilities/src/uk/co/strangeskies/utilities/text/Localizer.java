package uk.co.strangeskies.utilities.text;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.ObservableImpl;
import uk.co.strangeskies.utilities.collection.computingmap.CacheComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

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

	static class Localizable<T extends LocalizationText<T>> {
		public final Class<T> accessor;
		public final ClassLoader classLoader;
		public final URL[] locations;

		public Localizable(Class<T> accessor, ClassLoader classLoader, URL... locations) {
			this.accessor = accessor;
			this.classLoader = classLoader;
			this.locations = locations;
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

			return accessor.equals(other.accessor) && classLoader.equals(other.classLoader)
					&& Arrays.equals(locations, other.locations);
		}

		@Override
		public int hashCode() {
			return accessor.hashCode() ^ classLoader.hashCode() ^ Arrays.hashCode(locations);
		}
	}

	static final String TEXT_POSTFIX = "Text";
	static final Set<MethodSignature> LOCALIZATION_HELPER_METHODS = new HashSet<>();
	{
		for (Method method : LocalizationText.class.getMethods()) {
			LOCALIZATION_HELPER_METHODS.add(new MethodSignature(method));
		}
		for (Method method : Object.class.getMethods()) {
			LOCALIZATION_HELPER_METHODS.add(new MethodSignature(method));
		}
	}

	private final ComputingMap<Localizable<?>, LocalizationText<?>> LOCALIZATION_CACHE;
	private final Constructor<MethodHandles.Lookup> methodHandleConstructor;

	Locale locale;
	final ObservableImpl<Locale> localeChanges;
	private final LocalizerText text;

	public Localizer() {
		this(Locale.getDefault());
	}

	public Localizer(Locale defaultLocale) {
		try {
			methodHandleConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		if (!methodHandleConstructor.isAccessible()) {
			methodHandleConstructor.setAccessible(true);
		}

		LOCALIZATION_CACHE = new CacheComputingMap<Localizable<?>, LocalizationText<?>>(this::localize, true);

		localeChanges = new ObservableImpl<>();
		setLocale(defaultLocale);

		text = getLocalization(LocalizerText.class);
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		if (this.locale != locale) {
			this.locale = locale;
			localeChanges.fire(locale);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T extends LocalizationText<T>> T localize(Localizable<T> localizable) {
		LocalizerText text;
		if (this.text == null) {
			text = new DefaultLocalizerText();
		} else {
			text = this.text;
		}

		if (!localizable.accessor.isInterface()) {
			throw new IllegalArgumentException(text.mustBeInterface(localizable.accessor).toString());
		}

		for (Method method : localizable.accessor.getMethods()) {
			MethodSignature signature = new MethodSignature(method);

			if (!LOCALIZATION_HELPER_METHODS.contains(signature) && !method.isDefault()) {
				/*
				 * ensure return type of method is String
				 */
				if (!method.getReturnType().equals(LocalizedString.class)) {
					throw new IllegalArgumentException(text.illegalReturnType(localizable.accessor, method).toString());
				}
			}
		}

		IdentityProperty<LocalizationTextDelegate<T>> helper = new IdentityProperty<>();

		T proxy = (T) Proxy.newProxyInstance(localizable.classLoader, new Class<?>[] { localizable.accessor },
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

		helper.set(new LocalizationTextDelegate<>(this, proxy, localizable, text));

		return proxy;
	}

	public static String removeTestPostfix(String string) {
		if (string.endsWith(TEXT_POSTFIX) && !string.equals(TEXT_POSTFIX)) {
			string = string.substring(0, string.length() - TEXT_POSTFIX.length());
		}
		return string;
	}

	public static String getKey(Method method) {
		LocalizationKey keyAnnotation = method.getAnnotation(LocalizationKey.class);
		if (keyAnnotation != null) {
			return keyAnnotation.value();
		}

		String className = removeTestPostfix(method.getDeclaringClass().getSimpleName());

		return getKeyText(className) + '.' + getKeyText(method.getName());
	}

	public static String getKeyText(String string) {
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

	public URL getDefaultLocation(Class<?> accessor) {
		return getDefaultLocation(accessor, accessor.getClassLoader());
	}

	public URL getDefaultLocation(Class<?> accessor, ClassLoader classLoader) {
		return classLoader.getResource(
				accessor.getPackage().getName().replaceAll(".", "/") + "/" + removeTestPostfix(accessor.getSimpleName()));
	}

	@SuppressWarnings("unchecked")
	private <T extends LocalizationText<T>> T getLocalization(Localizable<T> localizable) {
		return (T) LOCALIZATION_CACHE.putGet(localizable);
	}

	public <T extends LocalizationText<T>> T getLocalization(Class<T> accessor, ClassLoader classLoader,
			URL... location) {
		return getLocalization(new Localizable<>(accessor, classLoader, location));
	}

	public <T extends LocalizationText<T>> T getLocalization(Class<T> accessor, ClassLoader classLoader) {
		return getLocalization(accessor, classLoader, getDefaultLocation(accessor));
	}

	public <T extends LocalizationText<T>> T getLocalization(Class<T> accessor, URL... location) {
		return getLocalization(accessor, accessor.getClassLoader(), location);
	}

	public <T extends LocalizationText<T>> T getLocalization(Class<T> accessor) {
		return getLocalization(accessor, accessor.getClassLoader());
	}
}
