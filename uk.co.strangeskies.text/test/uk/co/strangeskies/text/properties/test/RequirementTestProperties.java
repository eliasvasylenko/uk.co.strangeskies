/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.text.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.properties.test;

import static uk.co.strangeskies.text.properties.PropertyConfiguration.UNQUALIFIED_SLASHED;
import static uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase.LOWER;

import java.util.Optional;

import uk.co.strangeskies.text.properties.Properties;
import uk.co.strangeskies.text.properties.PropertyConfiguration;
import uk.co.strangeskies.text.properties.PropertyConfiguration.Defaults;
import uk.co.strangeskies.text.properties.PropertyConfiguration.Evaluation;

@SuppressWarnings("javadoc")
@PropertyConfiguration(key = UNQUALIFIED_SLASHED, keySplitString = ".", keyCase = LOWER, evaluation = Evaluation.DEFERRED)
public interface RequirementTestProperties extends Properties<RequirementTestProperties> {
	@PropertyConfiguration(defaults = Defaults.IGNORE)
	String requiredProperty();

	@PropertyConfiguration(defaults = Defaults.ALLOW)
	String defaultingRequiredProperty();

	@PropertyConfiguration(defaults = Defaults.IGNORE)
	Optional<String> optionalProperty();

	@PropertyConfiguration(defaults = Defaults.ALLOW)
	Optional<String> defaultingOptionalProperty();

	@PropertyConfiguration(evaluation = Evaluation.IMMEDIATE, defaults = Defaults.IGNORE)
	Optional<String> immediateOptionalProperty();

	@PropertyConfiguration(evaluation = Evaluation.IMMEDIATE, defaults = Defaults.ALLOW)
	String immediateDefaultingProperty();

	ImmediateRequirementTestProperties immediateRequirements();
}
