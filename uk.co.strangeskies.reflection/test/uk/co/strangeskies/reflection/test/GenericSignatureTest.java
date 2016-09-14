package uk.co.strangeskies.reflection.test;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.GenericSignature;
import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.TypeVariableSignature;

public class GenericSignatureTest {
	@Test
	public void noParametersSignatureTest() {
		GenericDeclaration declaration = Object.class;

		Assert.assertEquals(Collections.emptyList(), new GenericSignature().getTypeVariables(declaration));
	}

	@Test
	public void unboundedParameterSignatureTest() {
		GenericDeclaration declaration = Set.class;

		GenericSignature signature = new GenericSignature().withTypeVariable();

		List<? extends TypeVariable<?>> typeVariables = signature.getTypeVariables(declaration);

		Assert.assertEquals(1, typeVariables.size());
	}

	@Test
	public void parameterNamesTest() {
		GenericDeclaration declaration = Set.class;

		GenericSignature signature = new GenericSignature().withTypeVariable().withTypeVariable().withTypeVariable();

		List<? extends TypeVariable<?>> typeVariables = signature.getTypeVariables(declaration);

		Assert.assertEquals(Arrays.asList("T0", "T1", "T2"),
				typeVariables.stream().map(t -> t.getName()).collect(Collectors.toList()));
	}

	@Test
	public void selfBoundingTypeVariableTest() {
		GenericDeclaration declaration = Set.class;

		GenericSignature signature = new GenericSignature();

		TypeVariableSignature typeVariableSignature = signature.addTypeVariable();
		typeVariableSignature.withUpperBounds(ParameterizedTypes.uncheckedFrom(Set.class, typeVariableSignature));

		TypeVariable<?> typeVariable = signature.getTypeVariables(declaration).get(0);

		Assert.assertEquals(typeVariable, ((ParameterizedType) typeVariable.getBounds()[0]).getActualTypeArguments()[0]);
	}
}
