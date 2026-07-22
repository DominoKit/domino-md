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
package org.dominokit.markdown.renderer;

import java.util.Set;
import org.dominokit.markdown.node.Node;

/** A renderer for a set of node types. */
public interface NodeRenderer {

  /**
   * @return the types of nodes that this renderer handles
   */
  Set<Class<? extends Node>> getNodeTypes();

  /**
   * Render the specified node.
   *
   * @param node the node to render, will be an instance of one of {@link #getNodeTypes()}
   */
  void render(Node node);

  /**
   * Called before the root node is rendered, to do any initial processing at the start.
   *
   * @param rootNode the root (top-level) node
   */
  default void beforeRoot(Node rootNode) {}

  /**
   * Called after the root node is rendered, to do any final processing at the end.
   *
   * @param rootNode the root (top-level) node
   */
  default void afterRoot(Node rootNode) {}
}
