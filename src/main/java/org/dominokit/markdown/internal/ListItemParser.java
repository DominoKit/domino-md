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
package org.dominokit.markdown.internal;

import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.ListBlock;
import org.dominokit.markdown.node.ListItem;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.ParserState;

public class ListItemParser extends AbstractBlockParser {

  private final ListItem block = new ListItem();
  private final int contentIndent;
  private boolean hadBlankLine;

  public ListItemParser(int markerIndent, int contentIndent) {
    this.contentIndent = contentIndent;
    block.setMarkerIndent(markerIndent);
    block.setContentIndent(contentIndent);
  }

  @Override
  public boolean isContainer() {
    return true;
  }

  @Override
  public boolean canContain(Block childBlock) {
    if (hadBlankLine) {
      Block parent = block.getParent();
      if (parent instanceof ListBlock) {
        ((ListBlock) parent).setTight(false);
      }
    }
    return true;
  }

  @Override
  public Block getBlock() {
    return block;
  }

  @Override
  public BlockContinue tryContinue(ParserState state) {
    if (state.isBlank()) {
      if (block.getFirstChild() == null) {
        return BlockContinue.none();
      }
      Block activeBlock = state.getActiveBlockParser().getBlock();
      hadBlankLine = activeBlock instanceof Paragraph || activeBlock instanceof ListItem;
      return BlockContinue.atIndex(state.getNextNonSpaceIndex());
    }

    if (state.getIndent() >= contentIndent) {
      return BlockContinue.atColumn(state.getColumn() + contentIndent);
    }
    return BlockContinue.none();
  }
}
