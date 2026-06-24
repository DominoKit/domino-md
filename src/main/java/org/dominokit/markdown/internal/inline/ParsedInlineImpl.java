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
package org.dominokit.markdown.internal.inline;

import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.parser.beta.ParsedInline;
import org.dominokit.markdown.parser.beta.Position;

public class ParsedInlineImpl implements ParsedInline {
  private final Node node;
  private final Position position;

  public ParsedInlineImpl(Node node, Position position) {
    this.node = node;
    this.position = position;
  }

  public Node getNode() {
    return node;
  }

  public Position getPosition() {
    return position;
  }
}
