package uk.co.strangeskies.scripting.engine.ruby;

import org.jruby.embed.jsr223.JRubyEngineFactory;

@SuppressWarnings("javadoc")
public class RubyScriptEngineFactory extends JRubyEngineFactory {
	public RubyScriptEngineFactory() {
		System.setProperty("org.jruby.embed.localvariable.behavior", "persistent");
	}
}
