/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.osgi.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi.frameworkwrapper.impl;

import java.util.Dictionary;

import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ServiceScope;

import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.utilities.Log;

@Component(scope = ServiceScope.BUNDLE, service = FrameworkWrapper.class)
public class FrameworkWrapperService extends FrameworkWrapperImpl {
	private Log log;

	@Activate
	public void activate(ComponentContext context) {
		Dictionary<String, String> headers = context.getUsingBundle().getHeaders();

		ClassLoader classLoader = context.getUsingBundle().adapt(BundleWiring.class).getClassLoader();

		setBaseClassLoader(classLoader);

		setFrameworkJars(toUrls(classLoader, headers.get(EMBEDDED_FRAMEWORK)));
		setBundleJars(toUrls(classLoader, headers.get(EMBEDDED_RUNPATH)));

		super.setLog(log, true);
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	public void setLog(Log log) {
		this.log = log;
	}
}
