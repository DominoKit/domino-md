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

import org.dominokit.markdown.node.Text;

/**
 * Parsed link or image metadata.
 *
 * <p>The interface exposes the pieces of a parsed link or image so custom link processors can
 * inspect the original text, the optional marker, destination, title, and the scanner position at
 * which parsing should resume.
 */
public interface LinkInfo {

  /**
   * @return the marker if present, or {@code null}
   */
  Text marker();

  /**
   * @return the text node for the opening bracket {@code [}
   */
  Text openingBracket();

  /**
   * @return the text between the first brackets
   */
  String text();

  /**
   * @return the label, or {@code null} for inline and shortcut links
   */
  String label();

  /**
   * @return the destination if available, or {@code null}
   */
  String destination();

  /**
   * @return the title if available, or {@code null}
   */
  String title();

  /**
   * @return the scanner position after the closing text bracket
   *     <p>For example:
   *     <pre>
   * [foo][bar]
   *      ^
   * </pre>
   */
  Position afterTextBracket();
}
