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
package org.dominokit.markdown.internal.inline;

import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.parser.beta.ParsedInline;
import org.dominokit.markdown.parser.beta.Position;

/**
 * Concrete successful inline-parse result.
 *
 * <p>The parser uses this immutable wrapper to return both the parsed node and the scanner position
 * to resume from after the node has been consumed.
 */
public class ParsedInlineImpl implements ParsedInline {
  private final Node node;
  private final Position position;

  /**
   * Create a successful inline parse result.
   *
   * @param node parsed node
   * @param position scanner position after the parsed node
   */
  public ParsedInlineImpl(Node node, Position position) {
    this.node = node;
    this.position = position;
  }

  /**
   * @return the parsed node
   */
  public Node getNode() {
    return node;
  }

  /**
   * @return the scanner position after the parsed node
   */
  public Position getPosition() {
    return position;
  }
}
