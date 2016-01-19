/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.test;

import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.Annotations.AnnotationParser;

/**
 * Tests for {@link AnnotationParser} class. This is necessary alongside
 * {@link AnnotationsTest}, for two reasons.
 * <p>
 * Firstly, though those tests do cover parsing, they only do so of annotation
 * strings formatted exactly according to the expected output of
 * {@link Annotations#toString(java.lang.annotation.Annotation)}. Secondly, they
 * do not cover partial
 * <p>
 * These test cases aim to cover formatting which is valid parsing input, but
 * differs from expected string output.
 * 
 * @author Elias N Vasylenko
 */
// @RunWith(Theories.class)
public class AnnotationParserTest {}
