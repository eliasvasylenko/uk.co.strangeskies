/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.utilities.text;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Allow the escape of special characters in a character sequence by prefixture
 * with a given special escape character.
 *
 * @author Elias N Vasylenko
 */
public class StringEscaper {
	@SuppressWarnings("serial")
	private final static StringEscaper JAVA = new StringEscaper("\"'",
			new HashMap<Character, String>() {
				{
					put('t', "\t");
					put('b', "\b");
					put('n', "\n");
					put('r', "\r");
					put('f', "\f");
				}
			});

	private final char escapeCharacter;
	private final Map<Character, String> escapedSequences;
	private final SortedMap<String, Character> sequenceEscapingCharacter;

	/**
	 * Create a new escaper with the escape character '\' and the escapable
	 * characters described by the given string.
	 * 
	 * @param escapingCharacters
	 *          The characters which should be escaped with the given escape
	 *          character
	 */
	public StringEscaper(String escapingCharacters) {
		this('\\', escapingCharacters);
	}

	/**
	 * Create a new escaper with the escape character '\' and the given escapable
	 * characters.
	 * 
	 * @param escapingCharacters
	 *          The characters which should be escaped with the given escape
	 *          character
	 */
	public StringEscaper(Collection<Character> escapingCharacters) {
		this('\\', escapingCharacters);
	}

	/**
	 * Create a new escaper with the escape character '\' and the escapable
	 * characters described by the given string.
	 * 
	 * @param escapingCharacters
	 *          The characters which should be escaped with the given escape
	 *          character
	 * @param escapingTransforms
	 *          Characters which should be escaped with the given escape
	 *          character, mapped to the strings they represent.
	 */
	public StringEscaper(String escapingCharacters,
			Map<Character, String> escapingTransforms) {
		this('\\', escapingCharacters, escapingTransforms);
	}

	/**
	 * Create a new escaper with the escape character '\' and the given escapable
	 * characters.
	 * 
	 * @param escapingCharacters
	 *          The characters which should be escaped with the given escape
	 *          character
	 * @param escapingTransforms
	 *          Characters which should be escaped with the given escape
	 *          character, mapped to the strings they represent.
	 */
	public StringEscaper(Collection<Character> escapingCharacters,
			Map<Character, String> escapingTransforms) {
		this('\\', escapingCharacters, escapingTransforms);
	}

	/**
	 * Create a new escaper with the given escape character and the escapable
	 * characters described by the given string.
	 * 
	 * @param escapeCharacer
	 *          The character with which to escape the given escaping characters
	 * @param escapingCharacters
	 *          The characters which should be escaped with the given escape
	 *          character
	 */
	public StringEscaper(char escapeCharacer, String escapingCharacters) {
		this(escapeCharacer, escapingCharacters, new HashMap<>());
	}

	/**
	 * Create a new escaper with the given escape character and the given
	 * escapable characters.
	 * 
	 * @param escapeCharacer
	 *          The character with which to escape the given escaping characters
	 * @param escapingCharacters
	 *          The characters which should be escaped with the given escape
	 *          character
	 */
	public StringEscaper(char escapeCharacer,
			Collection<Character> escapingCharacters) {
		this(escapeCharacer, escapingCharacters, new HashMap<>());
	}

	/**
	 * Create a new escaper with the given escape character and the escapable
	 * characters described by the given string.
	 * 
	 * @param escapeCharacer
	 *          The character with which to escape the given escaping characters
	 * @param escapingCharacters
	 *          The characters which should be escaped with the given escape
	 *          character
	 * @param escapingTransforms
	 *          Characters which should be escaped with the given escape
	 *          character, mapped to the strings they represent.
	 */
	public StringEscaper(char escapeCharacer, String escapingCharacters,
			Map<Character, String> escapingTransforms) {
		this(escapeCharacer,
				escapingCharacters.chars().mapToObj(i -> Character.valueOf((char) i))
						.collect(Collectors.toSet()), escapingTransforms);
	}

	/**
	 * Create a new escaper with the given escape character and the given
	 * escapable characters.
	 * 
	 * @param escapeCharacer
	 *          The character with which to escape the given escaping characters
	 * @param escapingCharacterSet
	 *          The characters which should be escaped with the given escape
	 *          character
	 * @param escapingTransforms
	 *          Characters which should be escaped with the given escape
	 *          character, mapped to the strings they represent.
	 */
	public StringEscaper(char escapeCharacer,
			Collection<Character> escapingCharacterSet,
			Map<Character, String> escapingTransforms) {
		this.escapeCharacter = escapeCharacer;

		Map<Character, String> escapingCharacters = new HashMap<>(
				escapingTransforms);
		for (Character character : escapingCharacterSet)
			escapingCharacters.put(character, character.toString());
		escapingCharacters.put(escapeCharacer, Character.valueOf(escapeCharacer)
				.toString());

		SortedMap<String, Character> escapedCharacters = new TreeMap<>();
		for (Map.Entry<Character, String> escapingCharacter : escapingCharacters
				.entrySet())
			escapedCharacters.put(escapingCharacter.getValue(),
					escapingCharacter.getKey());

		this.escapedSequences = Collections.unmodifiableMap(escapingCharacters);
		this.sequenceEscapingCharacter = Collections
				.unmodifiableSortedMap(escapedCharacters);
	}

	/**
	 * @return The escape character
	 */
	public char getEscapeCharacer() {
		return escapeCharacter;
	}

	/**
	 * @return A mapping of escape characters to the character sequences they
	 *         escape.
	 */
	public Map<Character, String> getEscapingCharacters() {
		return escapedSequences;
	}

	/**
	 * @return A mapping of character sequences to their escaped characters.
	 */
	public SortedMap<String, Character> getEscapedCharacters() {
		return sequenceEscapingCharacter;
	}

	/**
	 * @param string
	 *          The string we wish to escape
	 * @return The escaped form of the given string.
	 */
	public String escape(String string) {
		StringBuilder builder = new StringBuilder();

		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char character = chars[i];

			StringBuilder sequenceSoFar = new StringBuilder(character);
			Character escapedCharacter = sequenceEscapingCharacter.get(sequenceSoFar
					.toString());
			if (escapedCharacter == null) {
				/*
				 * No escapable sequence found for a single character, so look further
				 */
				String afterSequenceSoFar = Character.valueOf((char) (character + 1))
						.toString();
				SortedMap<String, Character> matches = sequenceEscapingCharacter
						.subMap(sequenceSoFar.toString(), afterSequenceSoFar);
				int j = i;
				while (!matches.isEmpty()) {
					char nextLetter = chars[++j];

					afterSequenceSoFar = new StringBuilder(sequenceSoFar).append(
							(char) (nextLetter + 1)).toString();
					sequenceSoFar.append(nextLetter);

					escapedCharacter = sequenceEscapingCharacter.get(sequenceSoFar
							.toString());
					if (escapedCharacter != null)
						break;

					matches = matches
							.subMap(sequenceSoFar.toString(), afterSequenceSoFar);

					escapedCharacter = sequenceEscapingCharacter.get(Character.valueOf(
							character).toString());
				}
			}

			if (escapedCharacter != null)
				builder.append('\\').append(escapedCharacter);
			else
				builder.append(character);
		}

		return builder.toString();
	}

	/**
	 * @param string
	 *          The string we wish to escape
	 * @return The escaped form of the given string.
	 */
	public String unescape(String string) {
		StringBuilder builder = new StringBuilder();

		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char character = chars[i];

			if (character == '\\') {
				character = chars[++i];

				String escapedSequence = escapedSequences.get(character);
				if (escapedSequence != null)
					builder.append(escapedSequence);
				else
					throw new IllegalArgumentException("String '" + string
							+ "' contains illegal escape character '" + character
							+ "' at index '" + i + "'");
			} else
				builder.append(character);
		}

		return builder.toString();
	}

	/**
	 * @return A string escaper for java string literals.
	 */
	public static StringEscaper java() {
		return JAVA;
	}
}
