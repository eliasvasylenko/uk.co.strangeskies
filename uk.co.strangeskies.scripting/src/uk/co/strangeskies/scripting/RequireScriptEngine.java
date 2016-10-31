package uk.co.strangeskies.scripting;

import aQute.bnd.annotation.headers.RequireCapability;

@RequireCapability(ns = "javax.script.engine", filter = "(javax.script.language=${language})")
public @interface RequireScriptEngine {
	String language();
}
