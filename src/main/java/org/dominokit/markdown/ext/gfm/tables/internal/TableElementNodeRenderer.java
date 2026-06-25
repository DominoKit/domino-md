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

import elemental2.dom.Element;
import java.util.Map;
import org.dominokit.markdown.ext.gfm.tables.TableBlock;
import org.dominokit.markdown.ext.gfm.tables.TableBody;
import org.dominokit.markdown.ext.gfm.tables.TableCell;
import org.dominokit.markdown.ext.gfm.tables.TableHead;
import org.dominokit.markdown.ext.gfm.tables.TableRow;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.elemental2.ElementNodeRenderer;
import org.dominokit.markdown.renderer.elemental2.ElementNodeRendererContext;

public class TableElementNodeRenderer extends TableNodeRenderer implements ElementNodeRenderer {

  private final ElementNodeRendererContext context;

  public TableElementNodeRenderer(ElementNodeRendererContext context) {
    this.context = context;
  }

  @Override
  protected void renderBlock(TableBlock node) {
    appendContainer(node, "table", Map.of());
  }

  @Override
  protected void renderHead(TableHead node) {
    appendContainer(node, "thead", Map.of());
  }

  @Override
  protected void renderBody(TableBody node) {
    appendContainer(node, "tbody", Map.of());
  }

  @Override
  protected void renderRow(TableRow node) {
    appendContainer(node, "tr", Map.of());
  }

  @Override
  protected void renderCell(TableCell node) {
    String tagName = node.isHeader() ? "th" : "td";
    Map<String, String> defaults =
        node.getAlignment() == null ? Map.of() : Map.of("align", alignment(node.getAlignment()));
    appendContainer(node, tagName, defaults);
  }

  private void appendContainer(Node node, String tagName, Map<String, String> defaultAttributes) {
    Element element = context.getDocument().createElement(tagName);
    for (var attribute : context.extendAttributes(node, tagName, defaultAttributes).entrySet()) {
      if (attribute.getValue() != null) {
        element.setAttribute(attribute.getKey(), attribute.getValue());
      }
    }
    context.append(element);
    context.renderChildren(node, element);
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
}
