package uk.co.strangeskies.text;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Allow the escape of special characters in a character sequence by prefixture
 * with a given special escape character.
 *
 * @author Elias N Vasylenko
 */
public class EscapeFormatter {
  @SuppressWarnings("serial")
  private final static EscapeFormatter JAVA = new EscapeFormatter(
      '\\',
      "\"\'",
      new HashMap<String, String>() {
        {
          put("t", "\t");
          put("b", "\b");
          put("n", "\n");
          put("r", "\r");
          put("f", "\f");
        }
      });

  /**
   * @return A string escaper for java string literals.
   */
  public static EscapeFormatter java() {
    return JAVA;
  }

  @SuppressWarnings("serial")
  private final static EscapeFormatter XML = new EscapeFormatter(new HashMap<String, String>() {
    {
      put("&gt;", ">");
      put("&lt;", "<");
      put("&quot;", "\"");
      put("&amp;", "&");
      put("&apos;", "'");
    }
  });

  /**
   * @return A string escaper for java string literals.
   */
  public static EscapeFormatter xml() {
    return XML;
  }

  /*
   * 
   */
  private final SortedMap<String, String> escapeToSequence;
  private final SortedMap<String, String> sequenceToEscape;

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
  public EscapeFormatter(char escapeCharacer, String escapingCharacters) {
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
   * @param escapeTransforms
   *          Strings which should be escaped with the given escape character,
   *          mapped to the strings they represent.
   */
  public EscapeFormatter(
      char escapeCharacer,
      String escapingCharacters,
      Map<String, String> escapeTransforms) {
    this(composeEscapingTransforms(escapeCharacer, escapingCharacters, escapeTransforms));
  }

  private static SortedMap<String, String> composeEscapingTransforms(
      char escapeCharacer,
      String escapingCharacters,
      Map<String, String> escapeTransforms) {
    String escapeCharacterString = ((Character) escapeCharacer).toString();

    SortedMap<String, String> escapedCharacters = new TreeMap<>();

    for (Map.Entry<String, String> escapeTransform : escapeTransforms.entrySet())
      escapedCharacters
          .put(escapeCharacterString + escapeTransform.getKey(), escapeTransform.getValue());

    for (Character escapingCharacter : escapingCharacters.toCharArray())
      escapedCharacters
          .put(escapeCharacterString + escapingCharacter, escapingCharacter.toString());

    escapedCharacters.put(escapeCharacterString + escapeCharacterString, escapeCharacterString);

    return escapedCharacters;
  }

  /**
   * Create a new escaper with the given escape character and the given escapable
   * characters.
   * 
   * @param escapeTransforms
   *          Strings which should be escaped with the given escape character,
   *          mapped to the strings which represent them.
   */
  public EscapeFormatter(Map<String, String> escapeTransforms) {
    this(new TreeMap<>(escapeTransforms));
  }

  /**
   * Create a new escaper with the given escape character and the given escapable
   * characters.
   * 
   * @param escapeTransforms
   *          Strings which should be escaped with the given escape character,
   *          mapped to the strings which represent them.
   */
  private EscapeFormatter(SortedMap<String, String> escapeTransforms) {
    SortedMap<String, String> inverseTransforms = new TreeMap<>();
    for (Map.Entry<String, String> escapingCharacter : escapeTransforms.entrySet())
      inverseTransforms.put(escapingCharacter.getValue(), escapingCharacter.getKey());

    this.escapeToSequence = Collections.unmodifiableSortedMap(escapeTransforms);
    this.sequenceToEscape = Collections.unmodifiableSortedMap(inverseTransforms);
  }

  /**
   * @return A mapping of escape characters to the character sequences they
   *         escape.
   */
  public SortedMap<String, String> getEscapingCharacters() {
    return escapeToSequence;
  }

  /**
   * @return A stream of all character sequences which must be escaped.
   */
  public Stream<String> getEscapableStrings() {
    return sequenceToEscape.keySet().stream();
  }

  /**
   * @return A stream of all escape sequences.
   */
  public Stream<String> getEscapedStrings() {
    return escapeToSequence.keySet().stream();
  }

  /**
   * @param escapableString
   *          A string which is not directly representable in the format and must
   *          be escaped.
   * @return the escaped string
   */
  public String getEscapedString(String escapableString) {
    String escapedString = sequenceToEscape.get(requireNonNull(escapableString));
    if (escapedString == null)
      throw new IllegalArgumentException();
    return escapedString;
  }

  /**
   * @param escapedString
   *          An escaped string representing a source string that is not directly
   *          representable.
   * @return the escaped string
   */
  public String getEscapableString(String escapedString) {
    String escapableString = escapeToSequence.get(escapedString);
    if (escapableString == null)
      throw new IllegalArgumentException();
    return escapableString;
  }

  /**
   * @param string
   *          The string we wish to escape
   * @return The escaped form of the given string.
   */
  public String escape(String string) {
    return mapString(string, sequenceToEscape);
  }

  /**
   * @param string
   *          The string we wish to escape
   * @return The escaped form of the given string.
   */
  public String unescape(String string) {
    return mapString(string, escapeToSequence);
  }

  private String mapString(String string, SortedMap<String, String> mapping) {
    StringBuilder builder = new StringBuilder();

    char[] chars = string.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      /*
       * For each character in the string
       */
      char character = chars[i];

      StringBuilder sequenceSoFar = new StringBuilder().append(character);
      String escapedString = mapping.get(sequenceSoFar.toString());
      if (escapedString == null) {
        /*
         * No escapable sequence found for a single character, so look further
         */
        String afterSequenceSoFar = ((Character) (char) (character + 1)).toString();
        SortedMap<String, String> matches = mapping
            .subMap(sequenceSoFar.toString(), afterSequenceSoFar);
        int j = i + 1;
        while (!matches.isEmpty() && j < chars.length) {
          char nextLetter = chars[j++];

          afterSequenceSoFar = new StringBuilder(sequenceSoFar)
              .append((char) (nextLetter + 1))
              .toString();
          sequenceSoFar.append(nextLetter);

          escapedString = mapping.get(sequenceSoFar.toString());
          if (escapedString != null) {
            i = j - 1;
            break;
          }

          matches = matches.subMap(sequenceSoFar.toString(), afterSequenceSoFar);
        }
      }

      if (escapedString != null)
        builder.append(escapedString);
      else
        builder.append(chars[i]);
    }

    return builder.toString();
  }
}