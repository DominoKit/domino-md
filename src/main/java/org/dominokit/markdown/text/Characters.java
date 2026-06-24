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

/** Character helpers for parsing Markdown input. */
public final class Characters {

  private Characters() {}

  public static int find(char c, CharSequence s, int startIndex) {
    int length = s.length();
    for (int i = startIndex; i < length; i++) {
      if (s.charAt(i) == c) {
        return i;
      }
    }
    return -1;
  }

  public static int findLineBreak(CharSequence s, int startIndex) {
    int length = s.length();
    for (int i = startIndex; i < length; i++) {
      switch (s.charAt(i)) {
        case '\n':
        case '\r':
          return i;
        default:
          break;
      }
    }
    return -1;
  }

  public static boolean isBlank(CharSequence s) {
    return skipSpaceTab(s, 0, s.length()) == s.length();
  }

  public static boolean hasNonSpace(CharSequence s) {
    int length = s.length();
    int skipped = skip(' ', s, 0, length);
    return skipped != length;
  }

  public static boolean isLetter(CharSequence s, int index) {
    int codePoint = Character.codePointAt(s, index);
    return Character.isLetter(codePoint);
  }

  public static boolean isSpaceOrTab(CharSequence s, int index) {
    if (index < s.length()) {
      char c = s.charAt(index);
      return c == ' ' || c == '\t';
    }
    return false;
  }

  public static int skip(char skip, CharSequence s, int startIndex, int endIndex) {
    for (int i = startIndex; i < endIndex; i++) {
      if (s.charAt(i) != skip) {
        return i;
      }
    }
    return endIndex;
  }

  public static int skipSpaceTab(CharSequence s, int startIndex, int endIndex) {
    for (int i = startIndex; i < endIndex; i++) {
      char c = s.charAt(i);
      if (c != ' ' && c != '\t') {
        return i;
      }
    }
    return endIndex;
  }
}
