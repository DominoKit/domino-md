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
package org.dominokit.markdown.renderer;

import org.dominokit.markdown.node.Node;

/**
 * Common abstraction for renderers that turn an AST into an output format.
 *
 * <p>Implementations typically provide both a streaming variant that writes to an {@link
 * Appendable} and a convenience overload that returns the rendered result as a string.
 */
public interface Renderer {

  /**
   * Render the tree of nodes to the supplied output.
   *
   * @param node the root node
   * @param output output for rendering
   */
  void render(Node node, Appendable output);

  /**
   * Render the tree of nodes to a string.
   *
   * @param node the root node
   * @return the rendered string
   */
  String render(Node node);
}
