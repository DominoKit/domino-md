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
package org.dominokit.markdown.parser.beta;

import java.util.Objects;
import org.dominokit.markdown.internal.inline.ParsedInlineImpl;
import org.dominokit.markdown.node.Node;

/**
 * Result of a single inline parser invocation.
 *
 * <p><em>This interface is not intended to be implemented by clients.</em>
 */
public interface ParsedInline {

  /** @return a result indicating that the parser did not handle the input */
  static ParsedInline none() {
    return null;
  }

  /**
   * Create a successful parse result.
   *
   * @param node parsed inline node
   * @param position scanner position after the parsed node
   * @return a parse result
   */
  static ParsedInline of(Node node, Position position) {
    Objects.requireNonNull(node, "node must not be null");
    Objects.requireNonNull(position, "position must not be null");
    return new ParsedInlineImpl(node, position);
  }
}
