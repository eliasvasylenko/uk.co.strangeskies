/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.Properties;
import uk.co.strangeskies.text.properties.PropertyConfiguration;
import uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase;

/**
 * Properties and localized strings relating to types.
 * 
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
@PropertyConfiguration(keyCase = KeyCase.LOWER, keySplitString = ".")
public interface ReflectionProperties extends Properties<ReflectionProperties> {
	Localized<String> unsupportedType(Type type);

	Localized<String> invalidAssignmentObject(Object object, Class<?> type);

	default Localized<String> invalidTypeVariableCaptureBounds(TypeVariableCapture capture) {
		return invalidTypeVariableCaptureBounds(capture, capture.getLowerBounds(), capture.getUpperBounds());
	}

	Localized<String> invalidTypeVariableCaptureBounds(
			TypeVariableCapture capture,
			Type[] lowerBounds,
			Type[] upperBounds);

	Localized<String> improperCaptureType(TypeVariableCapture capture);

	Localized<String> improperUpperBound(Type t, InferenceVariable inferenceVariable, BoundSet bounds);

	Localized<String> cannotCaptureInferenceVariable(InferenceVariable key, Type value, BoundSet bounds);

	Localized<String> cannotInstantiateInferenceVariable(InferenceVariable variable, BoundSet bounds);

	Localized<String> cannotFindSubstitution(Type i);

	Localized<String> invalidAnnotationValue(Method method, Object propertyValue);

	Localized<String> invalidAnnotationProperties(Class<? extends Annotation> annotationClass, Set<String> keySet);

	Localized<String> invalidAnnotationValue(
			Class<? extends Annotation> annotationClass,
			String name,
			Object propertyValue);

	Localized<String> invalidEquality(Type first, Type second, BoundSet bounds);

	Localized<String> invalidSubtype(Type subtype, Type supertype, BoundSet boundSet);

	Localized<String> invalidCaptureConversion(CaptureConversion captureConversion, BoundSet boundSet);

	Localized<String> invalidBoundSet(String message, BoundSet boundSet);

	Localized<String> cannotReduceConstraint(ConstraintFormula constraintFormula, BoundSet bounds);

	Localized<String> invalidIntersectionTypes(Collection<? extends Type> flattenedTypes, Type iType, Type jType);

	Localized<String> invalidIntersectionType(Collection<? extends Type> flattenedTypes);

	Localized<String> incompatibleImports(Class<?> class1, Class<?> class2);

	Localized<String> invalidUpperBound(WildcardType wildcardType);

	Localized<String> cannotCopyInferenceVariable(InferenceVariable inferenceVariable, BoundSet boundSet);

	Localized<String> cannotFilterCapture(CaptureConversion capture);

	Localized<String> cannotCaptureMultipleTimes(
			InferenceVariable inferenceVariable,
			CaptureConversion capture,
			CaptureConversion captureConversion);

	Localized<String> invalidStaticMethodArguments(Method method, List<?> a);

	Localized<String> invalidCastObject(Object object, Type objectType, Type castType);

	Localized<String> invalidVariableArityInvocation(Executable executableMember);

	Localized<String> invalidConstructorArguments(Constructor<?> constructor, Type t, List<?> a);

	Localized<String> invalidMethodArguments(Method method, Type receiver, List<?> a);

	Localized<String> cannotResolveOverride(Executable executableMember, Type type);

	Localized<String> cannotResolveAmbiguity(Executable firstCandidate, Executable secondCandidate);

	Localized<String> cannotResolveApplicable(Set<? extends Executable> candidates, List<? extends Type> parameters);

	Localized<String> incompatibleArgument(
			Type givenArgumentCaptured,
			Type genericParameterCaptured,
			int i,
			Executable executableMember);

	Localized<String> incompatibleArgument(
			Object object,
			Type objectType,
			Type genericParameterCaptured,
			int i,
			Executable executableMember);

	Localized<String> cannotResolveInvocationType(Executable executableMember, List<? extends Type> arguments);

	Localized<String> cannotGetField(Object target, Field fieldMember);

	Localized<String> cannotSetField(Object target, Object value, Field fieldMember);

	Localized<String> cannotFindMethodOn(Type type);

	default Localized<String> incorrectTypeArgumentCount(GenericDeclaration declaration, List<Type> typeArguments) {
		return incorrectTypeArgumentCount(declaration, asList(declaration.getTypeParameters()), typeArguments);
	}

	Localized<String> incorrectTypeArgumentCount(
			GenericDeclaration declaration,
			List<TypeVariable<?>> typeParameters,
			List<Type> typeArguments);

	Localized<String> duplicateTypeVariable(String n);

	Localized<String> cannotResolveSupertype(Type type, Class<?> superclass);

	Localized<String> incorrectEnclosingDeclaration(Type rawType, GenericDeclaration declaration);
}
