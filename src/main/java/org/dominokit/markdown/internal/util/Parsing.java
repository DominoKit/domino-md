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

/**
 * Small parsing-related constants and helpers shared across block and inline parsers.
 */
public final class Parsing {

  /**
   * CommonMark's indentation threshold for code blocks and other four-space indentation checks.
   */
  public static final int CODE_BLOCK_INDENT = 4;

  /** Prevent instantiation. */
  private Parsing() {}

  /**
   * Calculate how many columns remain until the next tab stop.
   *
   * @param column current visual column
   * @return number of columns needed to reach the next tab stop
   */
  public static int columnsToNextTabStop(int column) {
    return 4 - (column % 4);
  }
}
