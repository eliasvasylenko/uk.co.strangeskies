package uk.co.strangeskies.scripting.engine.kotlin;

import static com.intellij.openapi.util.Disposer.newDisposable;
import static java.util.Arrays.asList;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineFactoryBase;
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine;

import kotlin.script.templates.standard.ScriptTemplateWithBindings;

@SuppressWarnings("javadoc")
public class KotlinScriptEngineFactory extends KotlinJsr223JvmScriptEngineFactoryBase {
	private static final URL TEMPLATE_CLASS_PATH = KotlinScriptEngineFactory.class.getProtectionDomain().getCodeSource()
			.getLocation();
	private static final String TEMPLATE_CLASS_NAME = ScriptTemplateWithBindings.class.getName();

	@Override
	public ScriptEngine getScriptEngine() {
		File templateClassPath;
		try {
			templateClassPath = new File(TEMPLATE_CLASS_PATH.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		return new KotlinJsr223JvmLocalScriptEngine(newDisposable(), this, asList(templateClassPath), TEMPLATE_CLASS_NAME,
				ctx -> new Bindings[] { ctx.getBindings(ScriptContext.ENGINE_SCOPE) }, new Class<?>[] { Map.class });
	}
}
