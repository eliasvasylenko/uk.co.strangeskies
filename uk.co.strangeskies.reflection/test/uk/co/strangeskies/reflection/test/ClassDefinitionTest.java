package uk.co.strangeskies.reflection.test;

import java.util.Set;

import org.junit.Test;

import uk.co.strangeskies.reflection.ClassDefinition;
import uk.co.strangeskies.reflection.ClassDefinition.ClassSignature;
import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.TypeVariableSignature;
import uk.co.strangeskies.utilities.Self;

public class ClassDefinitionTest {
	@Test
	public void selfBoundingSetTest() {
		ClassSignature<?> classSignature = ClassDefinition.declare(getClass().getPackage().getName() + ".SelfSet");
		TypeVariableSignature typeVariableSignature = classSignature.addTypeVariable();
		typeVariableSignature.withUpperBounds(ParameterizedTypes.uncheckedFrom(Self.class, typeVariableSignature));

		ClassDefinition<?> classDefinition = classSignature
				.withSuperType(ParameterizedTypes.from(Set.class, typeVariableSignature)).define();

		System.out.println(classDefinition);
	}
}
