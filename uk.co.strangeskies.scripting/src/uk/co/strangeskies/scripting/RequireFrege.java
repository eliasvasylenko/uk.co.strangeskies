package uk.co.strangeskies.scripting;

import static uk.co.strangeskies.scripting.impl.ScriptEngineManagerImpl.CAPABILITY_LANGUAGE_FILTER;
import static uk.co.strangeskies.scripting.impl.ScriptEngineManagerImpl.CAPABILITY_NAMESPACE;

import aQute.bnd.annotation.headers.RequireCapability;

/**
 * Create a requirement on the {@value RequireFrege#LANGUAGE} scripting engine.
 * 
 * @author Elias N Vasylenko
 */
@RequireCapability(ns = CAPABILITY_NAMESPACE, filter = "(" + CAPABILITY_LANGUAGE_FILTER + "=" + RequireFrege.LANGUAGE
		+ ")")
public @interface RequireFrege {
	@SuppressWarnings("javadoc")
	public static final String LANGUAGE = "frege";
}
