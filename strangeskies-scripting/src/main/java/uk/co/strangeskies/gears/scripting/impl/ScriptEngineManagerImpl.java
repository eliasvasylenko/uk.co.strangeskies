package uk.co.strangeskies.gears.scripting.impl;

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

@Component(service = ScriptEngineManagerImpl.class)
public class ScriptEngineManagerImpl extends ScriptEngineManager {
	private final Set<ScriptEngineFactory> scriptEngineFactories;

	private final Map<String, ScriptEngineFactory> nameAssociations;
	private final Map<String, ScriptEngineFactory> extensionAssociations;
	private final Map<String, ScriptEngineFactory> mimeTypeAssociations;

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
		ScriptEngineFactory assiciatedFactory = extensionAssociations
				.get(extension);
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
	public void registerEngineExtension(String extension,
			ScriptEngineFactory factory) {
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
