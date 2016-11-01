package uk.co.strangeskies.scripting.engine.python;

import org.python.jsr223.PyScriptEngineFactory;

@SuppressWarnings("javadoc")
public class PythonScriptEngineFactory extends PyScriptEngineFactory {
	public PythonScriptEngineFactory() {
		System.setProperty("python.console.encoding", "UTF-8");
		System.setProperty("python.home", "/home/eli/Downloads/jython-standalone-2.7.0/Lib");
		System.setProperty("python.import.site", "false");
	}
}
