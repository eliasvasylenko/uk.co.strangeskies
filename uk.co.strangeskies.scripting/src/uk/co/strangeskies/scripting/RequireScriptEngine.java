package uk.co.strangeskies.scripting;

import static uk.co.strangeskies.scripting.ScriptEngines.CAPABILITY_LANGUAGE_FILTER;
import static uk.co.strangeskies.scripting.ScriptEngines.CAPABILITY_NAMESPACE;

import aQute.bnd.annotation.headers.RequireCapability;

/**
 * Create a requirement on a scripting engine of the specified language.
 * 
 * @author Elias N Vasylenko
 */
@RequireCapability(ns = CAPABILITY_NAMESPACE, filter = "(" + CAPABILITY_LANGUAGE_FILTER + "=${language})")
public @interface RequireScriptEngine {
	@SuppressWarnings("javadoc")
	String language();
}
