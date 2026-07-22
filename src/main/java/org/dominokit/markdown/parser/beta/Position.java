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
package org.dominokit.markdown.parser.beta;

/**
 * Opaque cursor position within a {@link Scanner}.
 *
 * <p>The position is intentionally lightweight so callers can save and restore scanner locations
 * without depending on the scanner's internal representation.
 */
public class Position {

  final int lineIndex;
  final int index;

  /**
   * Create a scanner position.
   *
   * @param lineIndex line index within the scanned source
   * @param index character index within the line
   */
  Position(int lineIndex, int index) {
    this.lineIndex = lineIndex;
    this.index = index;
  }
}
