/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.scripting.
 *
 * uk.co.strangeskies.scripting is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.scripting is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.scripting.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * An implementation of {@link ScriptEngineManager} for the OSGi environment.
 * Services registered for {@link ScriptEngineFactory} will be registered
 * automatically.
 * 
 * @author Elias N Vasylenko
 *
 */
@Component(service = ScriptEngineManager.class, immediate = true)
public class ScriptEngineManagerImpl extends ScriptEngineManager {
	private final Set<ScriptEngineFactory> scriptEngineFactories;

	private final Map<String, ScriptEngineFactory> nameAssociations;
	private final Map<String, ScriptEngineFactory> extensionAssociations;
	private final Map<String, ScriptEngineFactory> mimeTypeAssociations;

	/**
	 * Default constructor.
	 */
	public ScriptEngineManagerImpl() {
		scriptEngineFactories = new HashSet<>();

		nameAssociations = new HashMap<>();
		extensionAssociations = new HashMap<>();
		mimeTypeAssociations = new HashMap<>();
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	protected void addScriptEngineFactory(ScriptEngineFactory factory) {
		scriptEngineFactories.add(factory);
	}

	protected void removeScriptEngineFactory(ScriptEngineFactory factory) {
		scriptEngineFactories.remove(factory);
	}

	private ScriptEngine getEngine(ScriptEngineFactory factory) {
		try {
			ScriptEngine engine = factory.getScriptEngine();
			engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
			return engine;
		} catch (Exception exp) {
			return null;
		}
	}

	@Override
	public ScriptEngine getEngineByExtension(String extension) {
		ScriptEngineFactory assiciatedFactory = extensionAssociations.get(extension);
		if (assiciatedFactory != null) {
			ScriptEngine engine = getEngine(assiciatedFactory);
			if (engine != null)
				return engine;
		}

		for (ScriptEngineFactory factory : getEngineFactories()) {
			if (factory.getExtensions().contains(extension)) {
				ScriptEngine engine = getEngine(factory);
				if (engine != null)
					return engine;
			}
		}

		return null;
	}

	@Override
	public ScriptEngine getEngineByMimeType(String mimeType) {
		ScriptEngineFactory assiciatedFactory = mimeTypeAssociations.get(mimeType);
		if (assiciatedFactory != null) {
			ScriptEngine engine = getEngine(assiciatedFactory);
			if (engine != null)
				return engine;
		}

		for (ScriptEngineFactory factory : getEngineFactories()) {
			if (factory.getMimeTypes().contains(mimeType)) {
				ScriptEngine engine = getEngine(factory);
				if (engine != null)
					return engine;
			}
		}

		return null;
	}

	@Override
	public ScriptEngine getEngineByName(String name) {
		ScriptEngineFactory assiciatedFactory = nameAssociations.get(name);
		if (assiciatedFactory != null) {
			ScriptEngine engine = getEngine(assiciatedFactory);
			if (engine != null)
				return engine;
		}

		for (ScriptEngineFactory factory : getEngineFactories()) {
			if (factory.getNames().contains(name)) {
				ScriptEngine engine = getEngine(factory);
				if (engine != null)
					return engine;
			}
		}

		return null;
	}

	@Override
	public List<ScriptEngineFactory> getEngineFactories() {
		return new ArrayList<>(scriptEngineFactories);
	}

	@Override
	public void registerEngineExtension(String extension, ScriptEngineFactory factory) {
		extensionAssociations.put(extension, factory);
	}

	@Override
	public void registerEngineMimeType(String type, ScriptEngineFactory factory) {
		mimeTypeAssociations.put(type, factory);
	}

	@Override
	public void registerEngineName(String name, ScriptEngineFactory factory) {
		nameAssociations.put(name, factory);
	}
}
