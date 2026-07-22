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

/**
 * Parses list item blocks and keeps track of the indentation that belongs to the item body.
 *
 * <p>The parser also tracks whether a blank line appeared inside the item so that the parent list
 * can be downgraded from tight to loose formatting when needed.
 */
public class ListItemParser extends AbstractBlockParser {

  private final ListItem block = new ListItem();
  private final int contentIndent;
  private boolean hadBlankLine;

  /**
   * Create a list item parser for the supplied marker and content indentation.
   *
   * @param markerIndent indentation of the list marker itself
   * @param contentIndent indentation required for item content
   */
  public ListItemParser(int markerIndent, int contentIndent) {
    this.contentIndent = contentIndent;
    block.setMarkerIndent(markerIndent);
    block.setContentIndent(contentIndent);
  }

  /**
   * @return {@code true} because a list item is a container block
   */
  @Override
  public boolean isContainer() {
    return true;
  }

  /**
   * List items always accept child blocks, but a blank line inside the item forces the parent list
   * to become loose.
   *
   * @param childBlock child block being attached
   * @return always {@code true}
   */
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

  /**
   * @return the list item node being constructed
   */
  @Override
  public Block getBlock() {
    return block;
  }

  /**
   * Continue while the line is blank or sufficiently indented to belong to the item body.
   *
   * @param state current parser state
   * @return continuation at the next content column or none when the item ends
   */
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
