package uk.co.strangeskies.scripting;

import static uk.co.strangeskies.scripting.ScriptEngines.CAPABILITY_LANGUAGE_FILTER;
import static uk.co.strangeskies.scripting.ScriptEngines.CAPABILITY_NAMESPACE;

import aQute.bnd.annotation.headers.RequireCapability;

/**
 * Create a requirement on the {@value RequireScalaScriptEngine#LANGUAGE} scripting engine.
 * 
 * @author Elias N Vasylenko
 */
@RequireCapability(
		ns = CAPABILITY_NAMESPACE,
		filter = "(" + CAPABILITY_LANGUAGE_FILTER + "=" + RequireScalaScriptEngine.LANGUAGE + ")")
public @interface RequireScalaScriptEngine {
	@SuppressWarnings("javadoc")
	public static final String LANGUAGE = "scala";
}
