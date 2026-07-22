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

import org.dominokit.markdown.ext.gfm.strikethrough.Strikethrough;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.markdown.MarkdownNodeRendererContext;
import org.dominokit.markdown.renderer.markdown.MarkdownWriter;

/**
 * Markdown renderer for GFM strikethrough nodes.
 *
 * <p>The renderer re-emits the original opening and closing delimiters around the rendered child
 * content so the markdown output preserves the extension syntax.
 */
public class StrikethroughMarkdownNodeRenderer extends StrikethroughNodeRenderer {

  private final MarkdownNodeRendererContext context;
  private final MarkdownWriter writer;

  /**
   * Create a renderer bound to the active Markdown rendering context.
   *
   * @param context rendering context used for markdown emission and child traversal
   */
  public StrikethroughMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
    this.context = context;
    this.writer = context.getWriter();
  }

  /**
   * Render the strikethrough node by replaying its delimiters around the child content.
   *
   * @param node strikethrough node to render
   */
  @Override
  public void render(Node node) {
    Strikethrough strikethrough = (Strikethrough) node;
    writer.raw(strikethrough.getOpeningDelimiter());
    renderChildren(node);
    writer.raw(strikethrough.getClosingDelimiter());
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
