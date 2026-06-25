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

import org.dominokit.markdown.ext.gfm.tables.TableBlock;
import org.dominokit.markdown.ext.gfm.tables.TableBody;
import org.dominokit.markdown.ext.gfm.tables.TableCell;
import org.dominokit.markdown.ext.gfm.tables.TableHead;
import org.dominokit.markdown.ext.gfm.tables.TableRow;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.text.TextContentNodeRendererContext;
import org.dominokit.markdown.renderer.text.TextContentWriter;

public class TableTextContentNodeRenderer extends TableNodeRenderer {

  private final TextContentNodeRendererContext context;
  private final TextContentWriter textContent;

  public TableTextContentNodeRenderer(TextContentNodeRendererContext context) {
    this.context = context;
    this.textContent = context.getWriter();
  }

  @Override
  protected void renderBlock(TableBlock node) {
    textContent.pushTight(true);
    renderChildren(node);
    textContent.popTight();
    textContent.block();
  }

  @Override
  protected void renderHead(TableHead node) {
    renderChildren(node);
  }

  @Override
  protected void renderBody(TableBody node) {
    renderChildren(node);
  }

  @Override
  protected void renderRow(TableRow node) {
    renderChildren(node);
    textContent.block();
  }

  @Override
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
