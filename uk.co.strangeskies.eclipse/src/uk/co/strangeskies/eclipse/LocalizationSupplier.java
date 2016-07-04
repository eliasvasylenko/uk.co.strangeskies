/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.eclipse.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.text.properties.PropertyAccessor;
import uk.co.strangeskies.text.properties.PropertyLoader;
import uk.co.strangeskies.text.properties.PropertyLoaderException;

/**
 * Supplier for Eclipse DI contexts, to provide localisation implementations of
 * a requested type via a {@link PropertyLoader}.
 *
 * @since 1.2
 */
@Component(service = ExtendedObjectSupplier.class, property = "dependency.injection.annotation:String=uk.co.strangeskies.eclipse.Localize", immediate = true)
public class LocalizationSupplier extends ExtendedObjectSupplier {
	@Reference
	PropertyLoader generalLocalizer;
	private LocalizationSupplierText text;

	@Activate
	void activate() {
		text = generalLocalizer.getProperties(LocalizationSupplierText.class);
	}

	@Override
	public Object get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group) {
		try {
			Type accessor = descriptor.getDesiredType();

			if (validateAccessorType(accessor)) {
				return localizeAccessor(requestor, (Class<?>) accessor);
			} else {
				throw new PropertyLoaderException(text.illegalInjectionTarget());
			}
		} catch (PropertyLoaderException e) {
			throw e;
		} catch (Exception e) {
			throw new PropertyLoaderException(text.unexpectedError(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends PropertyAccessor<T>> Object localizeAccessor(IRequestor requestor, Class<?> accessor) {
		try {
		BundleContext context = FrameworkUtil.getBundle(accessor).getBundleContext();

		ServiceReference<PropertyLoader> localizerServiceRererence = context.getServiceReference(PropertyLoader.class);
		PropertyLoader localizer = context.getService(localizerServiceRererence);

		T localization = localizer.getProperties((Class<T>) accessor);

		context.addServiceListener(new ServiceListener() {
			@Override
			public void serviceChanged(ServiceEvent event) {
				if (event.getType() == ServiceEvent.UNREGISTERING
						&& event.getServiceReference().equals(localizerServiceRererence)) {
					try {
						requestor.resolveArguments(false);
						requestor.execute();
					} catch (Exception e) {}
				}
			}
		});

		return localization;} catch (Exception e) {e.printStackTrace();throw e;}
	}

	private boolean validateAccessorType(Type accessor) {
		if (!(accessor instanceof Class) || !PropertyAccessor.class.isAssignableFrom((Class<?>) accessor))
			return false;

		List<Type> accessorParameters = TypeToken.over(accessor).resolveSupertypeParameters(PropertyAccessor.class)
				.getAllTypeArgumentsList();

		if (accessorParameters.size() != 1)
			return false;

		return Types.equals(accessorParameters.get(0), accessor);
	}
}
