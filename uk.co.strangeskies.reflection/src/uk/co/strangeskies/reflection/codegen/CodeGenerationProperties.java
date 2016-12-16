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
package uk.co.strangeskies.reflection.codegen;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;

import uk.co.strangeskies.reflection.ReflectionProperties;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.Properties;
import uk.co.strangeskies.text.properties.PropertyConfiguration;
import uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase;

@SuppressWarnings("javadoc")
@PropertyConfiguration(keyCase = KeyCase.LOWER, keySplitString = ".")
public interface CodeGenerationProperties extends Properties<CodeGenerationProperties> {
	ReflectionProperties reflection();

	Localized<String> invalidExpressionForStatement(Expression expression);

	Localized<String> cannotInstantiateClassDefinition(ClassDefinition<?, ?> classDefinition, TypeToken<?> superType);

	Localized<String> cannotAccessPlaceholderExpression(VariableExpressionProxy<?> variableExpressionProxy);

	Localized<String> cannotOverrideMethod(Method overriddenMethod);

	Localized<String> incompatibleReturnTypes(Type override, Method inherited);

	Localized<String> incompatibleOverride(MethodDefinition<?, ?> override);

	Localized<String> incompatibleParameterTypes(Type[] parameterTypes, Method inherited);

	Localized<String> duplicateMethodDeclaration(MethodDeclaration<?, ?> override);

	Localized<String> mustOverrideMethods(Collection<Method> classMethod);

	Localized<String> undefinedVariable(VariableSignature<?> variable);

	Localized<String> cannotResolveEnclosingInstance(ClassDeclaration<?, ?> receiverClass);

	Localized<String> cannotRedeclareVariable(VariableSignature<?> variable);

	Localized<String> incompleteStatementExecution();

	Localized<String> incompleteExpressionEvaluation();
}
