/*
 * Copyright © 2026 Dominokit
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
package org.dominokit.markdown.internal;

/**
 * Accumulates block-level text content with newline separation.
 *
 * <p>Block parsers use this helper when they need to collect raw lines before the inline parser
 * later consumes the joined text.
 */
final class BlockContent {

  private final StringBuilder sb = new StringBuilder();
  private int lineCount;

  /**
   * Append a line to the accumulated block content.
   *
   * <p>Newlines are inserted between lines, not after the final line.
   */
  public void add(CharSequence line) {
    if (lineCount != 0) {
      sb.append('\n');
    }
    sb.append(line);
    lineCount++;
  }

  /**
   * @return the concatenated block content
   */
  public String getString() {
    return sb.toString();
  }
}
