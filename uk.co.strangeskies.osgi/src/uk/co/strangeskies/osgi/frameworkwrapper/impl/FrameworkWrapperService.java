/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi.frameworkwrapper.impl;

import static java.util.stream.Collectors.toSet;

import java.util.Dictionary;
import java.util.LinkedHashSet;
import java.util.Set;

import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ServiceScope;

import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.utilities.Log;

/**
 * A {@link FrameworkWrapper} implementation for consumption via OSGi.
 * 
 * @author Elias N Vasylenko
 */
@Component(scope = ServiceScope.BUNDLE, service = FrameworkWrapper.class)
public class FrameworkWrapperService extends FrameworkWrapperImpl {
	private static final String PACKAGE_WIRING = "osgi.wiring.package";
	private Log log;

	/**
	 * Activate based on the requesting bundle manifest and class loader.
	 * 
	 * @param context
	 *          the requesting component context
	 */
	@Activate
	public void activate(ComponentContext context) {
		Dictionary<String, String> headers = context.getUsingBundle().getHeaders();

		BundleWiring wiring = context.getUsingBundle().adapt(BundleWiring.class);

		setFrameworkJars(toUrls(wiring.getClassLoader(), headers.get(EMBEDDED_FRAMEWORK)));
		setBundleJars(toUrls(wiring.getClassLoader(), headers.get(EMBEDDED_RUNPATH)));
		setPackageVersions(getVersionedPackages(wiring, headers.get(EMBEDDED_CLASSPATH)));

		setBaseClassLoader(wiring.getClassLoader());

		super.setLog(log, true);
	}

	private Set<VersionedPackage> getVersionedPackages(BundleWiring wiring, String packagesString) {
		Set<BundleWire> versionedPackages = new LinkedHashSet<>();
		versionedPackages.addAll(wiring.getRequiredWires(PACKAGE_WIRING));
		versionedPackages.addAll(wiring.getProvidedWires(PACKAGE_WIRING));

		return versionedPackages.stream().map(w -> w.getCapability().getAttributes())
				.map(a -> new VersionedPackage(a.get(PACKAGE_WIRING).toString(), a.get(VERSION_PROPERTY).toString()))
				.collect(toSet());
	}

	@SuppressWarnings("javadoc")
	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	public void setLog(Log log) {
		this.log = log;
	}
}
