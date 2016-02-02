package uk.co.strangeskies.p2.bnd.test;

import org.junit.Test;

import uk.co.strangeskies.p2.bnd.P2BndRepository;

public class P2BndPluginTest {
	@Test
	public void initialisationTest() {
		P2BndRepository plugin = new P2BndRepository();

		plugin.close();
	}
}
