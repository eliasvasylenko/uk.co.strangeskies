/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.p2.
 *
 * uk.co.strangeskies.p2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.p2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.p2.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.p2.impl;

import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.p2.P2RepositoryFactory;
import uk.co.strangeskies.utilities.Log;

@SuppressWarnings("javadoc")
@Component
public class P2RepositoryFactoryImpl implements P2RepositoryFactory {
	@Reference
	private IProvisioningAgentProvider agentProvider;
	private BundleContext bundleContext;

	@Override
	public P2Repository create() {
		return new P2RepositoryImpl(agentProvider, bundleContext, null);
	}

	@Override
	public P2Repository create(Log log) {
		return new P2RepositoryImpl(agentProvider, bundleContext, log);
	}

	@Activate
	public void activate(BundleContext context) {
		this.bundleContext = context;
	}
}