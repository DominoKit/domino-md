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

public class TableHtmlNodeRenderer extends TableNodeRenderer {

  private final HtmlNodeRendererContext context;
  private final HtmlWriter html;

  public TableHtmlNodeRenderer(HtmlNodeRendererContext context) {
    this.context = context;
    this.html = context.getWriter();
  }

  @Override
  protected void renderBlock(TableBlock node) {
    html.line();
    html.tag("table", attrs(node, "table"));
    renderChildren(node);
    html.tag("/table");
    html.line();
  }

  @Override
  protected void renderHead(TableHead node) {
    html.line();
    html.tag("thead", attrs(node, "thead"));
    renderChildren(node);
    html.tag("/thead");
    html.line();
  }

  @Override
  protected void renderBody(TableBody node) {
    html.line();
    html.tag("tbody", attrs(node, "tbody"));
    renderChildren(node);
    html.tag("/tbody");
    html.line();
  }

  @Override
  protected void renderRow(TableRow node) {
    html.line();
    html.tag("tr", attrs(node, "tr"));
    renderChildren(node);
    html.tag("/tr");
    html.line();
  }

  @Override
  protected void renderCell(TableCell node) {
    String tagName = node.isHeader() ? "th" : "td";
    html.line();
    html.tag(tagName, cellAttrs(node, tagName));
    renderChildren(node);
    html.tag("/" + tagName);
    html.line();
  }

  private Map<String, String> attrs(Node node, String tagName) {
    return context.extendAttributes(node, tagName, Map.of());
  }

  private Map<String, String> cellAttrs(TableCell node, String tagName) {
    if (node.getAlignment() == null) {
      return attrs(node, tagName);
    }
    return context.extendAttributes(node, tagName, Map.of("align", alignment(node.getAlignment())));
  }

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

  private void renderChildren(Node parent) {
    Node node = parent.getFirstChild();
    while (node != null) {
      Node next = node.getNext();
      context.render(node);
      node = next;
    }
  }
}
