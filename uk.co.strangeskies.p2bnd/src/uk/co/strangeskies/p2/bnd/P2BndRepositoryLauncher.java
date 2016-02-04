/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.p2bnd.
 *
 * uk.co.strangeskies.p2bnd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.p2bnd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.p2bnd.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.p2.bnd;

import java.net.MalformedURLException;
import java.net.URL;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.repository.Repository;

import aQute.bnd.service.RemoteRepositoryPlugin;
import uk.co.strangeskies.utilities.classloader.ContextClassLoaderRunner;

/**
 * This class is not primarily intended to be used within OSGi environments. For
 * an OSGi enabled implementation of {@link RemoteRepositoryPlugin} and
 * {@link Repository} which provides p2 repository support, the
 * {@code uk.co.strangeskies.p2.P2RepositoryImpl} class in the
 * {@code uk.co.strangeskies.p2} project should be used instead. This class is
 * simply a wrapper for that implementation for use in non OSGi environments,
 * and creates a framework internally to host the necessary Eclipse Project
 * bundles.
 * <p>
 *
 * @author Elias N Vasylenko
 */
@Component(immediate = true)
public class P2BndRepositoryLauncher {
	public static void main(String... args) {
		URL frameworkUrl;

		try {
			frameworkUrl = new URL(
					"file:/C:/Users/tofuser/git/uk.co.strangeskies/uk.co.strangeskies.p2bnd/generated/uk.co.strangeskies.p2bnd/jar/framework/org.eclipse.osgi.jar");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		new ContextClassLoaderRunner(frameworkUrl).run(() -> {
			try {
				Thread.currentThread().getContextClassLoader().loadClass("org.osgi.framework.launch.FrameworkFactory");
				System.out.println("kay..");

				Class<?> p2RepoClass = Thread.currentThread().getContextClassLoader()
						.loadClass(P2BndRepository.class.getName());
				p2RepoClass.getMethod("activate").invoke(p2RepoClass.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
