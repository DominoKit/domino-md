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

import java.util.ArrayList;
import java.util.List;
import org.dominokit.markdown.ext.gfm.tables.TableBlock;
import org.dominokit.markdown.ext.gfm.tables.TableBody;
import org.dominokit.markdown.ext.gfm.tables.TableCell;
import org.dominokit.markdown.ext.gfm.tables.TableHead;
import org.dominokit.markdown.ext.gfm.tables.TableRow;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.markdown.MarkdownNodeRendererContext;
import org.dominokit.markdown.renderer.markdown.MarkdownWriter;
import org.dominokit.markdown.text.AsciiMatcher;

/**
 * Renders tables back to pipe-table Markdown.
 */
public class TableMarkdownNodeRenderer extends TableNodeRenderer {

  private final MarkdownWriter writer;
  private final MarkdownNodeRendererContext context;
  private final AsciiMatcher pipe = AsciiMatcher.builder().c('|').build();
  private final List<TableCell.Alignment> columns = new ArrayList<>();

  /**
   * Create a renderer bound to the active Markdown rendering context.
   *
   * @param context rendering context used for markdown emission and child traversal
   */
  public TableMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
    this.writer = context.getWriter();
    this.context = context;
  }

  @Override
  /** Start a table block and render its descendants. */
  protected void renderBlock(TableBlock node) {
    columns.clear();
    writer.pushTight(true);
    renderChildren(node);
    writer.popTight();
    writer.block();
  }

  @Override
  /** Render the header row and the separator line. */
  protected void renderHead(TableHead node) {
    renderChildren(node);
    for (TableCell.Alignment columnAlignment : columns) {
      writer.raw('|');
      if (columnAlignment == TableCell.Alignment.LEFT) {
        writer.raw(":---");
      } else if (columnAlignment == TableCell.Alignment.RIGHT) {
        writer.raw("---:");
      } else if (columnAlignment == TableCell.Alignment.CENTER) {
        writer.raw(":---:");
      } else {
        writer.raw("---");
      }
    }
    writer.raw("|");
    writer.block();
  }

  @Override
  /** Render the table body rows. */
  protected void renderBody(TableBody node) {
    renderChildren(node);
  }

  @Override
  /** Render one Markdown table row. */
  protected void renderRow(TableRow node) {
    renderChildren(node);
    writer.raw("|");
    writer.block();
  }

  @Override
  /** Render one Markdown table cell. */
  protected void renderCell(TableCell node) {
    if (node.getParent() != null && node.getParent().getParent() instanceof TableHead) {
      columns.add(node.getAlignment());
    }
    writer.raw("|");
    writer.pushRawEscape(pipe);
    renderChildren(node);
    writer.popRawEscape();
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
