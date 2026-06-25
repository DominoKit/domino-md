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

import java.util.Set;
import org.dominokit.markdown.ext.gfm.tables.TableBlock;
import org.dominokit.markdown.ext.gfm.tables.TableBody;
import org.dominokit.markdown.ext.gfm.tables.TableCell;
import org.dominokit.markdown.ext.gfm.tables.TableHead;
import org.dominokit.markdown.ext.gfm.tables.TableRow;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.NodeRenderer;

abstract class TableNodeRenderer implements NodeRenderer {

  @Override
  public Set<Class<? extends Node>> getNodeTypes() {
    return Set.of(
        TableBlock.class, TableHead.class, TableBody.class, TableRow.class, TableCell.class);
  }

  @Override
  public void render(Node node) {
    if (node instanceof TableBlock) {
      renderBlock((TableBlock) node);
    } else if (node instanceof TableHead) {
      renderHead((TableHead) node);
    } else if (node instanceof TableBody) {
      renderBody((TableBody) node);
    } else if (node instanceof TableRow) {
      renderRow((TableRow) node);
    } else if (node instanceof TableCell) {
      renderCell((TableCell) node);
    }
  }

  protected abstract void renderBlock(TableBlock node);

  protected abstract void renderHead(TableHead node);

  protected abstract void renderBody(TableBody node);

  protected abstract void renderRow(TableRow node);

  protected abstract void renderCell(TableCell node);
}
