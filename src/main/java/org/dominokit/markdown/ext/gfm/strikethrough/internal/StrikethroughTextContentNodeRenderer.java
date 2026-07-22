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
package org.dominokit.markdown.ext.gfm.strikethrough.internal;

import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.text.TextContentNodeRendererContext;

/**
 * Text-content renderer for strikethrough nodes.
 *
 * <p>The renderer strips the strikethrough wrapper itself and delegates all child rendering to the
 * surrounding text renderer so the markdown extension remains invisible in plain-text extraction.
 */
public class StrikethroughTextContentNodeRenderer extends StrikethroughNodeRenderer {

  private final TextContentNodeRendererContext context;

  /**
   * Create a renderer bound to the active text-content rendering context.
   *
   * @param context rendering context used for child traversal
   */
  public StrikethroughTextContentNodeRenderer(TextContentNodeRendererContext context) {
    this.context = context;
  }

  /**
   * Render only the children of the strikethrough node.
   *
   * @param node strikethrough node to render
   */
  @Override
  public void render(Node node) {
    renderChildren(node);
  }

  /**
   * Render all child nodes in document order.
   *
   * @param parent parent node whose children should be rendered
   */
  private void renderChildren(Node parent) {
    Node node = parent.getFirstChild();
    while (node != null) {
      Node next = node.getNext();
      context.render(node);
      node = next;
    }
  }
}
