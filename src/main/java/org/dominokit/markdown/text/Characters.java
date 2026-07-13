/*
 * Copyright © 2019 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dominokit.markdown.text;

/**
 * Utility functions for searching text and classifying characters.
 *
 * <p>The methods here are intentionally small and allocation-free because they are used in hot
 * parsing paths.
 */
public class Characters {

  /**
   * Find the first occurrence of a character at or after {@code startIndex}.
   *
   * @param c character to find
   * @param s sequence to search
   * @param startIndex inclusive start index
   * @return the first matching index, or {@code -1}
   */
  public static int find(char c, CharSequence s, int startIndex) {
    int length = s.length();
    for (int i = startIndex; i < length; i++) {
      if (s.charAt(i) == c) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Find the first line-break character at or after {@code startIndex}.
   *
   * @param s sequence to search
   * @param startIndex inclusive start index
   * @return the first line-break index, or {@code -1}
   */
  public static int findLineBreak(CharSequence s, int startIndex) {
    int length = s.length();
    for (int i = startIndex; i < length; i++) {
      switch (s.charAt(i)) {
        case '\n':
        case '\r':
          return i;
      }
    }
    return -1;
  }

  /** @see <a href="https://spec.commonmark.org/0.31.2/#blank-line">CommonMark blank line</a> */
  public static boolean isBlank(CharSequence s) {
    return skipSpaceTab(s, 0, s.length()) == s.length();
  }

  /**
   * Determine whether the sequence contains at least one non-space character.
   *
   * @param s sequence to inspect
   * @return {@code true} if the sequence contains a non-space character
   */
  public static boolean hasNonSpace(CharSequence s) {
    int length = s.length();
    int skipped = skip(' ', s, 0, length);
    return skipped != length;
  }

  /**
   * Determine whether the character at {@code index} is a Unicode letter.
   *
   * @param s sequence to inspect
   * @param index character index
   * @return {@code true} when the code point is a letter
   */
  public static boolean isLetter(CharSequence s, int index) {
    int codePoint = Character.codePointAt(s, index);
    return UnicodeCharacterData.isLetter(codePoint);
  }

  /**
   * Determine whether the character at {@code index} is a space or tab.
   *
   * @param s sequence to inspect
   * @param index character index
   * @return {@code true} when the character is a space or tab
   */
  public static boolean isSpaceOrTab(CharSequence s, int index) {
    if (index < s.length()) {
      switch (s.charAt(index)) {
        case ' ':
        case '\t':
          return true;
      }
    }
    return false;
  }

  /**
   * @see <a href="https://spec.commonmark.org/0.31.2/#unicode-punctuation-character">CommonMark
   *     punctuation character</a>
   */
  public static boolean isPunctuationCodePoint(int codePoint) {
    if (UnicodeCharacterData.isPunctuationOrSymbol(codePoint)) {
      return true;
    }

    switch (codePoint) {
      case '$':
      case '+':
      case '<':
      case '=':
      case '>':
      case '^':
      case '`':
      case '|':
      case '~':
        return true;
      default:
        return false;
    }
  }

  /**
   * Check whether the provided code point is a Unicode whitespace character as defined in the
   * CommonMark spec.
   *
   * @see <a href="https://spec.commonmark.org/0.31.2/#unicode-whitespace-character">CommonMark
   *     whitespace character</a>
   */
  public static boolean isWhitespaceCodePoint(int codePoint) {
    switch (codePoint) {
      case ' ':
      case '\t':
      case '\n':
      case '\f':
      case '\r':
        return true;
      default:
        return UnicodeCharacterData.isSpaceSeparator(codePoint);
    }
  }

  /**
   * Skip characters equal to {@code skip} starting at {@code startIndex}.
   *
   * @param skip character to skip
   * @param s sequence to inspect
   * @param startIndex inclusive start index
   * @param endIndex exclusive end index
   * @return index of the first non-matching character, or {@code endIndex}
   */
  public static int skip(char skip, CharSequence s, int startIndex, int endIndex) {
    for (int i = startIndex; i < endIndex; i++) {
      if (s.charAt(i) != skip) {
        return i;
      }
    }
    return endIndex;
  }

  /**
   * Scan backwards while characters equal {@code skip}.
   *
   * @param skip character to skip
   * @param s sequence to inspect
   * @param startIndex inclusive start index
   * @param lastIndex inclusive lower bound
   * @return index of the last non-matching character, or {@code lastIndex - 1}
   */
  public static int skipBackwards(char skip, CharSequence s, int startIndex, int lastIndex) {
    for (int i = startIndex; i >= lastIndex; i--) {
      if (s.charAt(i) != skip) {
        return i;
      }
    }
    return lastIndex - 1;
  }

  /**
   * Skip spaces and tabs starting at {@code startIndex}.
   *
   * @param s sequence to inspect
   * @param startIndex inclusive start index
   * @param endIndex exclusive end index
   * @return index of the first non-space/tab character, or {@code endIndex}
   */
  public static int skipSpaceTab(CharSequence s, int startIndex, int endIndex) {
    for (int i = startIndex; i < endIndex; i++) {
      switch (s.charAt(i)) {
        case ' ':
        case '\t':
          break;
        default:
          return i;
      }
    }
    return endIndex;
  }

  /**
   * Scan backwards while characters are spaces or tabs.
   *
   * @param s sequence to inspect
   * @param startIndex inclusive start index
   * @param lastIndex inclusive lower bound
   * @return index of the last non-space/tab character, or {@code lastIndex - 1}
   */
  public static int skipSpaceTabBackwards(CharSequence s, int startIndex, int lastIndex) {
    for (int i = startIndex; i >= lastIndex; i--) {
      switch (s.charAt(i)) {
        case ' ':
        case '\t':
          break;
        default:
          return i;
      }
    }
    return lastIndex - 1;
  }
}
