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
package org.dominokit.markdown.ext.gfm.tables.internal;

import java.util.Map;
import org.dominokit.markdown.ext.gfm.tables.TableBlock;
import org.dominokit.markdown.ext.gfm.tables.TableBody;
import org.dominokit.markdown.ext.gfm.tables.TableCell;
import org.dominokit.markdown.ext.gfm.tables.TableHead;
import org.dominokit.markdown.ext.gfm.tables.TableRow;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.html.HtmlNodeRendererContext;
import org.dominokit.markdown.renderer.html.HtmlWriter;

/** Renders tables as HTML. */
public class TableHtmlNodeRenderer extends TableNodeRenderer {

  private final HtmlNodeRendererContext context;
  private final HtmlWriter html;

  /**
   * Create a renderer bound to the active HTML rendering context.
   *
   * @param context rendering context used for HTML emission and child traversal
   */
  public TableHtmlNodeRenderer(HtmlNodeRendererContext context) {
    this.context = context;
    this.html = context.getWriter();
  }

  @Override
  /** Render the outer table element. */
  protected void renderBlock(TableBlock node) {
    html.line();
    html.tag("table", attrs(node, "table"));
    renderChildren(node);
    html.tag("/table");
    html.line();
  }

  @Override
  /** Render the thead element. */
  protected void renderHead(TableHead node) {
    html.line();
    html.tag("thead", attrs(node, "thead"));
    renderChildren(node);
    html.tag("/thead");
    html.line();
  }

  @Override
  /** Render the tbody element. */
  protected void renderBody(TableBody node) {
    html.line();
    html.tag("tbody", attrs(node, "tbody"));
    renderChildren(node);
    html.tag("/tbody");
    html.line();
  }

  @Override
  /** Render a tr element. */
  protected void renderRow(TableRow node) {
    html.line();
    html.tag("tr", attrs(node, "tr"));
    renderChildren(node);
    html.tag("/tr");
    html.line();
  }

  @Override
  /** Render a th or td element. */
  protected void renderCell(TableCell node) {
    String tagName = node.isHeader() ? "th" : "td";
    html.line();
    html.tag(tagName, cellAttrs(node, tagName));
    renderChildren(node);
    html.tag("/" + tagName);
    html.line();
  }

  /**
   * Build the base attribute map for a rendered table element.
   *
   * @param node table node being rendered
   * @param tagName HTML tag name for the element
   * @return attributes extended by registered attribute providers
   */
  private Map<String, String> attrs(Node node, String tagName) {
    return context.extendAttributes(node, tagName, Map.of());
  }

  /**
   * Build the attributes for a table cell, including alignment when present.
   *
   * @param node table cell node
   * @param tagName HTML tag name used for the cell
   * @return attribute map to apply to the rendered cell
   */
  private Map<String, String> cellAttrs(TableCell node, String tagName) {
    if (node.getAlignment() == null) {
      return attrs(node, tagName);
    }
    return context.extendAttributes(node, tagName, Map.of("align", alignment(node.getAlignment())));
  }

  /**
   * Convert the table alignment enum to the corresponding HTML {@code align} value.
   *
   * @param alignment parsed column alignment
   * @return HTML alignment keyword
   */
  private String alignment(TableCell.Alignment alignment) {
    switch (alignment) {
      case LEFT:
        return "left";
      case CENTER:
        return "center";
      case RIGHT:
        return "right";
      default:
        throw new IllegalStateException("Unknown alignment: " + alignment);
    }
  }

  /**
   * Render the children of the supplied table node in document order.
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
