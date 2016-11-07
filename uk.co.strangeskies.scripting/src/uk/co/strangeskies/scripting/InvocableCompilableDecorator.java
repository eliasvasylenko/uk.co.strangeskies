package uk.co.strangeskies.scripting;

import java.io.Reader;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public interface InvocableCompilableDecorator extends Compilable, InvocableScriptEngineDecorator {
	@Override
	ScriptEngine getComponent();

	@Override
	default CompiledScript compile(Reader script) throws ScriptException {
		return ((Compilable) getComponent()).compile(script);
	}

	@Override
	default CompiledScript compile(String script) throws ScriptException {
		return ((Compilable) getComponent()).compile(script);
	}
}
