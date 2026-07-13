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
package org.dominokit.markdown.internal.util;

import org.dominokit.markdown.parser.beta.Scanner;

/**
 * Scanner routines for Markdown link destinations, titles, and labels.
 *
 * <p>These methods advance a shared scanner only as far as a syntactically valid construct allows,
 * which makes them useful as building blocks for the inline parser's link-resolution logic.
 */
public class LinkScanner {

  /**
   * Attempt to scan the contents of a link label.
   *
   * <p>The scanner stops at the closing {@code ]} or at the end of the line if the label is
   * unterminated. Escaped bracket characters are allowed; unescaped nested opening brackets are
   * rejected.
   *
   * @param scanner shared inline scanner
   * @return {@code true} if the scanner reached a possible label terminator, otherwise {@code false}
   */
  public static boolean scanLinkLabelContent(Scanner scanner) {
    while (scanner.hasNext()) {
      switch (scanner.peek()) {
        case '\\':
          scanner.next();
          if (isEscapable(scanner.peek())) {
            scanner.next();
          }
          break;
        case ']':
          return true;
        case '[':
          // spec: Unescaped square bracket characters are not allowed inside the opening and
          // closing
          // square brackets of link labels.
          return false;
        default:
          scanner.next();
      }
    }
    return true;
  }

  /**
   * Attempt to scan a link destination.
   *
   * <p>Angle-bracket destinations and bare destinations with balanced parentheses are both
   * supported.
   *
   * @param scanner shared inline scanner
   * @return {@code true} when a syntactically valid destination was consumed
   */
  public static boolean scanLinkDestination(Scanner scanner) {
    if (!scanner.hasNext()) {
      return false;
    }

    if (scanner.next('<')) {
      while (scanner.hasNext()) {
        switch (scanner.peek()) {
          case '\\':
            scanner.next();
            if (isEscapable(scanner.peek())) {
              scanner.next();
            }
            break;
          case '\n':
          case '<':
            return false;
          case '>':
            scanner.next();
            return true;
          default:
            scanner.next();
        }
      }
      return false;
    } else {
      return scanLinkDestinationWithBalancedParens(scanner);
    }
  }

  /**
   * Attempt to scan a link title, including the surrounding delimiters.
   *
   * <p>The scanner accepts single-quoted, double-quoted, or parenthesized titles.
   *
   * @param scanner shared inline scanner
   * @return {@code true} when a syntactically valid title was consumed
   */
  public static boolean scanLinkTitle(Scanner scanner) {
    if (!scanner.hasNext()) {
      return false;
    }

    char endDelimiter;
    switch (scanner.peek()) {
      case '"':
        endDelimiter = '"';
        break;
      case '\'':
        endDelimiter = '\'';
        break;
      case '(':
        endDelimiter = ')';
        break;
      default:
        return false;
    }
    scanner.next();

    if (!scanLinkTitleContent(scanner, endDelimiter)) {
      return false;
    }
    if (!scanner.hasNext()) {
      return false;
    }
    scanner.next();
    return true;
  }

  /**
   * Attempt to scan the body of a link title until the closing delimiter is reached.
   *
   * <p>Parenthesized titles are stricter because unescaped nested parentheses are not allowed.
   *
   * @param scanner shared inline scanner
   * @param endDelimiter closing delimiter to stop at
   * @return {@code true} when the title body is syntactically valid
   */
  public static boolean scanLinkTitleContent(Scanner scanner, char endDelimiter) {
    while (scanner.hasNext()) {
      char c = scanner.peek();
      if (c == '\\') {
        scanner.next();
        if (isEscapable(scanner.peek())) {
          scanner.next();
        }
      } else if (c == endDelimiter) {
        return true;
      } else if (endDelimiter == ')' && c == '(') {
        // unescaped '(' in title within parens is invalid
        return false;
      } else {
        scanner.next();
      }
    }
    return true;
  }

  // spec: a nonempty sequence of characters that does not start with <, does not include ASCII
  // space or control
  // characters, and includes parentheses only if (a) they are backslash-escaped or (b) they are
  // part of a balanced
  // pair of unescaped parentheses
  /**
   * Scan a bare link destination while tracking balanced parentheses.
   *
   * <p>The CommonMark specification allows bare destinations to contain parentheses only when they
   * are balanced or escaped. This method enforces that rule and also rejects pathological nesting
   * depth.
   *
   * @param scanner shared inline scanner
   * @return {@code true} when a valid bare destination was consumed
   */
  private static boolean scanLinkDestinationWithBalancedParens(Scanner scanner) {
    int parens = 0;
    boolean empty = true;
    while (scanner.hasNext()) {
      char c = scanner.peek();
      switch (c) {
        case ' ':
          return !empty;
        case '\\':
          scanner.next();
          if (isEscapable(scanner.peek())) {
            scanner.next();
          }
          break;
        case '(':
          parens++;
          // Limit to 32 nested parens for pathological cases
          if (parens > 32) {
            return false;
          }
          scanner.next();
          break;
        case ')':
          if (parens == 0) {
            return true;
          } else {
            parens--;
          }
          scanner.next();
          break;
        default:
          // or control character
          if (isAsciiControl(c)) {
            return !empty;
          }
          scanner.next();
          break;
      }
      empty = false;
    }
    return true;
  }

  /**
   * Determine whether a character can be escaped in Markdown link syntax.
   *
   * @param c character to test
   * @return {@code true} if the character can be backslash-escaped
   */
  private static boolean isEscapable(char c) {
    switch (c) {
      case '!':
      case '"':
      case '#':
      case '$':
      case '%':
      case '&':
      case '\'':
      case '(':
      case ')':
      case '*':
      case '+':
      case ',':
      case '-':
      case '.':
      case '/':
      case ':':
      case ';':
      case '<':
      case '=':
      case '>':
      case '?':
      case '@':
      case '[':
      case '\\':
      case ']':
      case '^':
      case '_':
      case '`':
      case '{':
      case '|':
      case '}':
      case '~':
        return true;
    }
    return false;
  }

  /**
   * Determine whether the character is an ASCII control character.
   *
   * @param c character to test
   * @return {@code true} when the character is in the ASCII control range
   */
  private static boolean isAsciiControl(char c) {
    return (c >= 0 && c <= 0x1F) || c == 0x7F;
  }
}
