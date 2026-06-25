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
package org.dominokit.markdown.renderer.text;

import org.dominokit.markdown.node.Node;

public interface TextContentNodeRendererContext {

  /**
   * Controls how line breaks should be rendered, see {@link LineBreakRendering}.
   *
   * @return the configured line-break mode
   */
  LineBreakRendering lineBreakRendering();

  /**
   * @return true when line breaks are stripped and the output is flattened
   * @deprecated Use {@link #lineBreakRendering()} instead.
   */
  @Deprecated
  boolean stripNewlines();

  /** @return the writer to use */
  TextContentWriter getWriter();

  /**
   * Render the specified node and its children using the configured renderers.
   *
   * @param node the node to render
   */
  void render(Node node);
}
