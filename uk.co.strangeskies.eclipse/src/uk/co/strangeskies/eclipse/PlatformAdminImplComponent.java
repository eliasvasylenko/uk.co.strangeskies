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

import java.lang.reflect.Method;

import org.eclipse.osgi.compatibility.state.PlatformAdminImpl;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Wrapper for {@link PlatformAdminImpl}, which is not properly exported.
 * 
 * @author Elias N Vasylenko
 */
@SuppressWarnings("restriction")
@Component(service = PlatformAdmin.class)
public class PlatformAdminImplComponent extends PlatformAdminImpl {
	/**
	 * Activate the {@link PlatformAdmin} instance.
	 * 
	 * @param context
	 *            {@link BundleContext} instance from the framework
	 * @throws Exception
	 *             Exception from reflective {@code start(BundleContext)}
	 *             invocation
	 */
	@Activate
	public void activate(BundleContext context) throws Exception {
		Method start = getClass().getMethod("start", BundleContext.class);
		start.setAccessible(true);
		start.invoke(this, context);
	}

	/**
	 * Deactivate the {@link PlatformAdmin} instance.
	 * 
	 * @param context
	 *            {@link BundleContext} instance from the framework
	 * @throws Exception
	 *             Exception from reflective {@code start(BundleContext)}
	 *             invocation
	 */
	@Deactivate
	public void deactivate(BundleContext context) throws Exception {
		Method stop = getClass().getMethod("stop", BundleContext.class);
		stop.setAccessible(true);
		stop.invoke(this, context);
	}
}
