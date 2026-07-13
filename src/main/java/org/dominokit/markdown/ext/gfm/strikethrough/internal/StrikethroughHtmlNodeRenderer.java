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
package org.dominokit.markdown.ext.gfm.strikethrough.internal;

import java.util.Map;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.html.HtmlNodeRendererContext;
import org.dominokit.markdown.renderer.html.HtmlWriter;

/**
 * HTML renderer for GFM strikethrough nodes.
 *
 * <p>The renderer emits a standard {@code <del>} wrapper and delegates child rendering back to the
 * HTML renderer context so nested markdown content is processed normally.
 */
public class StrikethroughHtmlNodeRenderer extends StrikethroughNodeRenderer {

  private final HtmlNodeRendererContext context;
  private final HtmlWriter html;

  /**
   * Create a renderer bound to the active HTML rendering context.
   *
   * @param context rendering context used for HTML emission and child traversal
   */
  public StrikethroughHtmlNodeRenderer(HtmlNodeRendererContext context) {
    this.context = context;
    this.html = context.getWriter();
  }

  /**
   * Render the strikethrough wrapper using HTML markup.
   *
   * @param node strikethrough node to render
   */
  @Override
  public void render(Node node) {
    html.tag("del", context.extendAttributes(node, "del", Map.of()));
    renderChildren(node);
    html.tag("/del");
  }

  /**
   * Render the children of the strikethrough node in document order.
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
