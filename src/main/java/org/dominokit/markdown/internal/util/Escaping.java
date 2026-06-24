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

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Minimal escaping helpers needed by the AST and block parser layers.
 *
 * <p>This is intentionally scoped to label normalization for the initial AST port and will be
 * expanded when the parser and renderer utilities are ported.
 */
public final class Escaping {

  private static final Pattern WHITESPACE = Pattern.compile("[ \t\r\n]+");
  private static final String ESCAPABLE = "!\"#$%&'()*+,./:;<=>?@[\\]^_`{|}~-";

  private Escaping() {}

  public static String normalizeLabelContent(String input) {
    String trimmed = input.trim();
    String caseFolded = trimmed.toLowerCase(Locale.ROOT).toUpperCase(Locale.ROOT);
    return WHITESPACE.matcher(caseFolded).replaceAll(" ");
  }

  public static String unescapeString(String input) {
    int backslash = input.indexOf('\\');
    if (backslash == -1) {
      return input;
    }

    StringBuilder sb = new StringBuilder(input.length());
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (c == '\\' && i + 1 < input.length()) {
        char next = input.charAt(i + 1);
        if (isEscapable(next)) {
          sb.append(next);
          i++;
          continue;
        }
      }
      sb.append(c);
    }
    return sb.toString();
  }

  private static boolean isEscapable(char c) {
    return ESCAPABLE.indexOf(c) >= 0;
  }
}
