package uk.co.strangeskies.gears.utilities.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.strangeskies.gears.utilities.IdentityComparator;

/**
 * It is very difficult or impossible to test this class properly, as the
 * important aspects of its behaviour either only occur in extremely rare
 * circumstances, or change slightly with implementation details of the VM, or
 * both.
 * 
 * @author tofuser
 *
 */
public class IdentityComparatorTest {
	@Test
	public void compareIdenticalReferences() {
		IdentityComparator<Object> identityComparator = new IdentityComparator<>();

		Object reference = new Object();
		Assert.assertEquals(identityComparator.compare(reference, reference), 0);
	}

	@Test
	public void compareDifferentReferences() {
		IdentityComparator<Object> identityComparator = new IdentityComparator<>();
		Assert.assertNotEquals(
				identityComparator.compare(new Object(), new Object()), 0);
	}
}
