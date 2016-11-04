package uk.co.strangeskies.scripting;

import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

public interface InvocableScriptEngineDecorator extends ScriptEngine, InvocableBase {
	ScriptEngine getComponent();

	@Override
	default void setContext(ScriptContext context) {
		getComponent().setContext(context);
	}

	@Override
	default void setBindings(Bindings bindings, int scope) {
		getComponent().setBindings(bindings, scope);
	}

	@Override
	default void put(String key, Object value) {
		getComponent().put(key, value);
	}

	@Override
	default ScriptEngineFactory getFactory() {
		return getComponent().getFactory();
	}

	@Override
	default ScriptContext getContext() {
		return getComponent().getContext();
	}

	@Override
	default Bindings getBindings(int scope) {
		return getComponent().getBindings(scope);
	}

	@Override
	default Object get(String key) {
		return getComponent().get(key);
	}

	@Override
	default Object eval(Reader reader, Bindings n) throws ScriptException {
		return getComponent().eval(reader, n);
	}

	@Override
	default Object eval(String script, Bindings n) throws ScriptException {
		return getComponent().eval(script, n);
	}

	@Override
	default Object eval(Reader reader, ScriptContext context) throws ScriptException {
		return getComponent().eval(reader, context);
	}

	@Override
	default Object eval(String script, ScriptContext context) throws ScriptException {
		return getComponent().eval(script, context);
	}

	@Override
	default Object eval(Reader reader) throws ScriptException {
		return getComponent().eval(reader);
	}

	@Override
	default Object eval(String script) throws ScriptException {
		return getComponent().eval(script);
	}

	@Override
	default Bindings createBindings() {
		return getComponent().createBindings();
	}
}
