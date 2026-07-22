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

import org.dominokit.markdown.ext.gfm.tables.TableBlock;
import org.dominokit.markdown.ext.gfm.tables.TableBody;
import org.dominokit.markdown.ext.gfm.tables.TableCell;
import org.dominokit.markdown.ext.gfm.tables.TableHead;
import org.dominokit.markdown.ext.gfm.tables.TableRow;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.text.TextContentNodeRendererContext;
import org.dominokit.markdown.renderer.text.TextContentWriter;

/** Renders tables as readable plain text. */
public class TableTextContentNodeRenderer extends TableNodeRenderer {

  private final TextContentNodeRendererContext context;
  private final TextContentWriter textContent;

  /**
   * Create a renderer bound to the active text-content rendering context.
   *
   * @param context rendering context used for text emission and child traversal
   */
  public TableTextContentNodeRenderer(TextContentNodeRendererContext context) {
    this.context = context;
    this.textContent = context.getWriter();
  }

  @Override
  /** Render the table block in text mode. */
  protected void renderBlock(TableBlock node) {
    textContent.pushTight(true);
    renderChildren(node);
    textContent.popTight();
    textContent.block();
  }

  @Override
  /** Render the header section. */
  protected void renderHead(TableHead node) {
    renderChildren(node);
  }

  @Override
  /** Render the body section. */
  protected void renderBody(TableBody node) {
    renderChildren(node);
  }

  @Override
  /** Render one text table row. */
  protected void renderRow(TableRow node) {
    renderChildren(node);
    textContent.block();
  }

  @Override
  /** Render one text table cell. */
  protected void renderCell(TableCell node) {
    if (node.getPrevious() != null) {
      textContent.write(" | ");
    }
    renderChildren(node);
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
