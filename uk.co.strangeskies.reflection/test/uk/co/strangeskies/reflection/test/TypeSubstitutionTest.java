package uk.co.strangeskies.reflection.test;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.TypeSubstitution;
import uk.co.strangeskies.reflection.TypeToken;

@SuppressWarnings("javadoc")
public class TypeSubstitutionTest {
	@Test
	public <T extends Number> void noSubstitutionIdentityTest() {
		Type type = new TypeToken<Map<? extends Number[][], Map<? super Number, T>>>() {}.getType();

		Type substitution = new TypeSubstitution().where(t -> false, t -> t).resolve(type);

		Assert.assertTrue(type == substitution);
	}

	@Test
	public <T extends Number> void doublyNestedWildcardSubstitutionTest() {
		Type type = new TypeToken<Map<? extends Comparable<?>[][], Map<? super Number, T>>>() {}.getType();

		Type expected = new TypeToken<Map<? extends Comparable<?>[][], Map<? super Serializable, T>>>() {}.getType();

		Type substitution = new TypeSubstitution().where(Number.class, Serializable.class).resolve(type);

		Assert.assertEquals(expected, substitution);
	}
}
