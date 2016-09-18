package uk.co.strangeskies.utilities.collection.test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.utilities.collection.StreamUtilities;

@SuppressWarnings("javadoc")
public class StreamUtilitiesTest {
	interface A {}

	interface B extends A {}

	interface C extends B {}

	interface D extends B, C {}

	@Test
	public void flatMapSingleLevelTest() {
		Assert.assertEquals(Arrays.asList(B.class, A.class), StreamUtilities
				.<Class<?>>flatMapRecursive(B.class, c -> Stream.of(c.getInterfaces())).collect(Collectors.toList()));
	}

	@Test
	public void flatMapTwoLevelsTest() {
		Assert.assertEquals(Arrays.asList(C.class, B.class, A.class), StreamUtilities
				.<Class<?>>flatMapRecursive(C.class, c -> Stream.of(c.getInterfaces())).collect(Collectors.toList()));
	}

	@Test
	public void flatMapThreeLevelsRepeatsTest() {
		Assert.assertEquals(Arrays.asList(D.class, B.class, A.class, C.class, B.class, A.class), StreamUtilities
				.<Class<?>>flatMapRecursive(D.class, c -> Stream.of(c.getInterfaces())).collect(Collectors.toList()));
	}

	@Test
	public void flatMapThreeLevelsDistinctTest() {
		Assert.assertEquals(Arrays.asList(D.class, B.class, A.class, C.class), StreamUtilities
				.<Class<?>>flatMapRecursiveDistinct(D.class, c -> Stream.of(c.getInterfaces())).collect(Collectors.toList()));
	}
}
