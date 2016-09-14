package uk.co.strangeskies.reflection.test;

import java.io.Serializable;
import java.lang.reflect.AnnotatedType;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.AnnotatedTypeSubstitution;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Capture;
import uk.co.strangeskies.reflection.TypeToken.Infer;
import uk.co.strangeskies.reflection.TypeToken.Preserve;

@SuppressWarnings("javadoc")
public class AnnotatedTypeSubstitutionTest {
	@Test
	public <T extends Number> void noSubstitutionIdentityTest() {
		AnnotatedType type = new TypeToken<Map<@Capture ? extends Number[] @Preserve [], Map<? super @Infer Number, @Infer T>>>() {}
				.getAnnotatedDeclaration();

		AnnotatedType substitution = new AnnotatedTypeSubstitution().where(t -> false, t -> t).resolve(type);

		Assert.assertTrue(type == substitution);
	}

	@Test
	public <T extends Number> void doublyNestedWildcardSubstitutionTest() {
		AnnotatedType type = new TypeToken<Map<@Capture ? extends Comparable<?>[] @Preserve [], Map<? super @Infer Number, @Infer T>>>() {}
				.getAnnotatedDeclaration();

		AnnotatedType expected = new TypeToken<Map<@Capture ? extends Comparable<?>[] @Preserve [], Map<? super Serializable, @Infer T>>>() {}
				.getAnnotatedDeclaration();

		AnnotatedType substitution = new AnnotatedTypeSubstitution()
				.where(t -> t.getType().equals(Number.class), t -> AnnotatedTypes.over(Serializable.class)).resolve(type);

		Assert.assertEquals(expected.getType(), substitution.getType());
	}
}
