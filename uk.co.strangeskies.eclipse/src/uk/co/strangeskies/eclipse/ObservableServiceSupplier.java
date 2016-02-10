/*******************************************************************************
 * Copyright (c) 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package uk.co.strangeskies.eclipse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.eclipse.fx.core.di.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import uk.co.strangeskies.fx.FXUtilities;

/**
 * Supplier for {@link Service}
 *
 * @since 1.2
 */
@Component(service = ExtendedObjectSupplier.class, property = "dependency.injection.annotation:String=uk.co.strangeskies.eclipse.ObservableService")
public class ObservableServiceSupplier extends ExtendedObjectSupplier {
	public class ServiceUpdateListener<T> implements ServiceListener {
		private final BundleContext context;
		private final ObservableList<ServiceReference<T>> references;
		private final Class<T> elementType;

		public ServiceUpdateListener(BundleContext context, Type elementType) throws InvalidSyntaxException {
			this.context = context;
			this.references = FXCollections.observableArrayList();
			this.elementType = elementType instanceof ParameterizedType
					? (Class<T>) ((ParameterizedType) elementType).getRawType() : (Class<T>) elementType;

			context.addServiceListener(this, "(" + Constants.OBJECTCLASS + "=" + this.elementType.getName() + ")");
		}

		@Override
		public void serviceChanged(ServiceEvent event) {
			System.out.println("ev: " + event);
			System.out.println("ev: " + event);
			System.out.println("ev: " + event);
			try {
				List<ServiceReference<T>> newReferences = new ArrayList<>(context.getServiceReferences(elementType, null));
				Collections.sort(newReferences);

				references.retainAll(newReferences);

				int index = 0;
				for (ServiceReference<T> newReference : newReferences) {
					if (!references.contains(newReference)) {
						references.add(index, newReference);
					}
					index++;
				}
			} catch (InvalidSyntaxException e) {
				throw new AssertionError();
			}
		}

		public ObservableList<T> getServiceList() {
			return FXUtilities.map(references, context::getService);
		}

		public ObservableSet<T> getServiceSet() {
			return FXUtilities.asSet(getServiceList());
		}

		public ObservableValue<T> getServiceValue() {
			SimpleObjectProperty<T> value = new SimpleObjectProperty<>();

			references.addListener((ListChangeListener<ServiceReference<T>>) c -> {
				value.set(context.getService(references.get(0)));
			});

			return value;
		}
	}

	@Override
	public Object get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group) {
		try {
			Type collectionType = descriptor.getDesiredType();
			Bundle bundle = FrameworkUtil.getBundle(requestor.getRequestingObjectClass());

			if (collectionType instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) collectionType;

				ServiceUpdateListener<?> listener = new ServiceUpdateListener<>(bundle.getBundleContext(),
						parameterizedType.getActualTypeArguments()[0]);

				if (parameterizedType.getRawType() == ObservableList.class) {
					return listener.getServiceList();
				}
				if (parameterizedType.getRawType() == ObservableSet.class) {
					return listener.getServiceSet();
				}
				if (parameterizedType.getRawType() == ObservableValue.class) {
					return listener.getServiceValue();
				}
			}

			throw new IllegalArgumentException("The " + ObservableService.class.getSimpleName()
					+ " annotation should be used with injection targets of parameterized type "
					+ ObservableList.class.getSimpleName() + ", " + ObservableSet.class.getSimpleName() + " or "
					+ ObservableValue.class.getSimpleName());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
