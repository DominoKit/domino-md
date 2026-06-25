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

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Escaping {

  public static final String ESCAPABLE = "[!\"#$%&'()*+,./:;<=>?@\\[\\\\\\]^_`{|}~-]";

  public static final String ENTITY = "&(?:#x[a-f0-9]{1,6}|#[0-9]{1,7}|[a-z][a-z0-9]{1,31});";

  private static final char[] HEX_DIGITS =
      new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  public static String escapeHtml(String input) {
    // Avoid building a new string in the majority of cases (nothing to escape)
    StringBuilder sb = null;

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      String replacement;
      switch (c) {
        case '&':
          replacement = "&amp;";
          break;
        case '<':
          replacement = "&lt;";
          break;
        case '>':
          replacement = "&gt;";
          break;
        case '\"':
          replacement = "&quot;";
          break;
        default:
          if (sb != null) {
            sb.append(c);
          }
          continue;
      }
      if (sb == null) {
        sb = new StringBuilder();
        sb.append(input, 0, i);
      }
      sb.append(replacement);
    }

    return sb != null ? sb.toString() : input;
  }

  /** Replace entities and backslash escapes with literal characters. */
  public static String unescapeString(String s) {
    if (!containsBackslashOrAmp(s)) {
      return s;
    }

    StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\' && i + 1 < s.length() && isEscapable(s.charAt(i + 1))) {
        sb.append(s.charAt(i + 1));
        i++;
      } else if (c == '&') {
        int entityEnd = findEntityEnd(s, i);
        if (entityEnd != -1) {
          sb.append(Html5Entities.entityToString(s.substring(i, entityEnd)));
          i = entityEnd - 1;
        } else {
          sb.append(c);
        }
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  public static String percentEncodeUrl(String s) {
    StringBuilder sb = new StringBuilder(s.length() + 16);
    for (int i = 0; i < s.length(); ) {
      int codePoint = Character.codePointAt(s, i);
      int charCount = Character.charCount(codePoint);

      if (codePoint == '%') {
        if (i + 2 < s.length() && isHexDigit(s.charAt(i + 1)) && isHexDigit(s.charAt(i + 2))) {
          sb.append(s, i, i + 3);
          i += 3;
        } else {
          sb.append("%25");
          i++;
        }
      } else if (isUriSafeCodePoint(codePoint)) {
        sb.appendCodePoint(codePoint);
        i += charCount;
      } else {
        appendPercentEncodedCodePoint(sb, s.substring(i, i + charCount));
        i += charCount;
      }
    }
    return sb.toString();
  }

  public static String normalizeLabelContent(String input) {
    String trimmed = input.trim();

    // This is necessary to correctly case fold "\u1E9E" (LATIN CAPITAL LETTER SHARP S) to "SS":
    // "\u1E9E".toLowerCase(Locale.ROOT)  -> "\u00DF" (LATIN SMALL LETTER SHARP S)
    // "\u00DF".toUpperCase(Locale.ROOT)  -> "SS"
    // Note that doing upper first (or only upper without lower) wouldn't work because:
    // "\u1E9E".toUpperCase(Locale.ROOT)  -> "\u1E9E"
    String caseFolded = trimmed.toLowerCase(Locale.ROOT).toUpperCase(Locale.ROOT);

    return collapseWhitespace(caseFolded);
  }

  public static boolean isEscapable(char c) {
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
      default:
        return false;
    }
  }

  private static boolean containsBackslashOrAmp(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\' || c == '&') {
        return true;
      }
    }
    return false;
  }

  private static int findEntityEnd(String s, int start) {
    int length = s.length();
    if (start + 3 >= length || s.charAt(start) != '&') {
      return -1;
    }

    int index = start + 1;
    if (s.charAt(index) == '#') {
      index++;
      if (index >= length) {
        return -1;
      }
      if (s.charAt(index) == 'x' || s.charAt(index) == 'X') {
        index++;
        int digitsStart = index;
        while (index < length && index - digitsStart < 6 && isHexDigit(s.charAt(index))) {
          index++;
        }
        if (index == digitsStart || index >= length || s.charAt(index) != ';') {
          return -1;
        }
        return index + 1;
      }

      int digitsStart = index;
      while (index < length && index - digitsStart < 7 && isDigit(s.charAt(index))) {
        index++;
      }
      if (index == digitsStart || index >= length || s.charAt(index) != ';') {
        return -1;
      }
      return index + 1;
    }

    if (!isAsciiLetter(s.charAt(index))) {
      return -1;
    }
    index++;
    while (index < length && index - start - 1 < 32 && isAsciiLetterOrDigit(s.charAt(index))) {
      index++;
    }
    if (index >= length || s.charAt(index) != ';') {
      return -1;
    }
    return index + 1;
  }

  private static String collapseWhitespace(String input) {
    StringBuilder sb = new StringBuilder(input.length());
    boolean previousWhitespace = false;
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (isAsciiWhitespace(c)) {
        if (!previousWhitespace) {
          sb.append(' ');
          previousWhitespace = true;
        }
      } else {
        sb.append(c);
        previousWhitespace = false;
      }
    }
    return sb.toString();
  }

  private static boolean isUriSafeCodePoint(int codePoint) {
    return (codePoint >= 'A' && codePoint <= 'Z')
        || (codePoint >= 'a' && codePoint <= 'z')
        || (codePoint >= '0' && codePoint <= '9')
        || codePoint == ':'
        || codePoint == '/'
        || codePoint == '?'
        || codePoint == '#'
        || codePoint == '@'
        || codePoint == '!'
        || codePoint == '$'
        || codePoint == '&'
        || codePoint == '\''
        || codePoint == '('
        || codePoint == ')'
        || codePoint == '*'
        || codePoint == '+'
        || codePoint == ','
        || codePoint == ';'
        || codePoint == '='
        || codePoint == '-'
        || codePoint == '.'
        || codePoint == '_'
        || codePoint == '~';
  }

  private static void appendPercentEncodedCodePoint(StringBuilder sb, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    for (byte b : bytes) {
      sb.append('%');
      sb.append(HEX_DIGITS[(b >> 4) & 0xF]);
      sb.append(HEX_DIGITS[b & 0xF]);
    }
  }

  private static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isAsciiLetter(char c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
  }

  private static boolean isAsciiLetterOrDigit(char c) {
    return isAsciiLetter(c) || isDigit(c);
  }

  private static boolean isAsciiWhitespace(char c) {
    return c == ' ' || c == '\t' || c == '\r' || c == '\n';
  }
}
