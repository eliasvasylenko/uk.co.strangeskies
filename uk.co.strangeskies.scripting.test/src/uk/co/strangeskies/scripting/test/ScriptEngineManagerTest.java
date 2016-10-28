package uk.co.strangeskies.scripting.test;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import javax.script.ScriptEngineManager;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import aQute.bnd.annotation.headers.RequireCapability;

@RequireCapability(ns = "osgi.service", filter = "(" + Constants.OBJECTCLASS + "=javax.script.ScriptEngineManager)")
public class ScriptEngineManagerTest {
	private static final int TIMEOUT_MILLISECONDS = 2000;

	private BundleContext getBundleContext() {
		return FrameworkUtil.getBundle(this.getClass()).getBundleContext();
	}

	private <T> T getService(Class<T> clazz) {
		try {
			BundleContext context = getBundleContext();

			ServiceTracker<T, T> serviceTracker = new ServiceTracker<>(context, clazz, null);
			serviceTracker.open();
			try {
				return serviceTracker.waitForService(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	@Test(timeout = TIMEOUT_MILLISECONDS)
	public void loadRunnableScriptTest() {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);

		assertThat(manager, notNullValue());
	}
}
