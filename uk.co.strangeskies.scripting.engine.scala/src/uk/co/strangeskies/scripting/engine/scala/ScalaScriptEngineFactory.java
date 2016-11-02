package uk.co.strangeskies.scripting.engine.scala;

import static org.osgi.framework.FrameworkUtil.getBundle;

import javax.script.ScriptEngine;

import org.osgi.framework.wiring.BundleWiring;

import scala.tools.nsc.interpreter.IMain;

@SuppressWarnings("javadoc")
public class ScalaScriptEngineFactory extends IMain.Factory {
	private ClassLoader getBundleClassLoader() {
		return getBundle(getClass()).adapt(BundleWiring.class).getClassLoader();
	}

	@Override
	public ScriptEngine getScriptEngine() {
		IMain engine = (IMain) super.getScriptEngine();
		engine.settings().embeddedDefaults(getBundleClassLoader());
		engine.settings().usejavacp().tryToSetFromPropertyValue("true");
		engine.put("scala.settings", engine.settings());
		return engine;
	}
}
