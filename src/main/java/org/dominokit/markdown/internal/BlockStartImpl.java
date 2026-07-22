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

import org.dominokit.markdown.parser.block.BlockParser;
import org.dominokit.markdown.parser.block.BlockStart;

/**
 * Concrete block-start result used by block parser factories.
 *
 * <p>It records the block parsers to activate, the offset adjustments to apply to the current
 * source position, and whether the matched block should replace the active parser or consume
 * paragraph lines from an already-open paragraph.
 */
public class BlockStartImpl extends BlockStart {

  private final BlockParser[] blockParsers;
  private int newIndex = -1;
  private int newColumn = -1;
  private boolean replaceActiveBlockParser;
  private int replaceParagraphLines;

  /**
   * Create a block-start result that activates the supplied parsers.
   *
   * @param blockParsers block parsers to push when the start is accepted
   */
  public BlockStartImpl(BlockParser... blockParsers) {
    this.blockParsers = blockParsers;
  }

  /**
   * @return the block parsers that should be pushed onto the open-block stack
   */
  public BlockParser[] getBlockParsers() {
    return blockParsers;
  }

  /**
   * @return the next character index the parser should resume from, or {@code -1}
   */
  public int getNewIndex() {
    return newIndex;
  }

  /**
   * @return the next column the parser should resume from, or {@code -1}
   */
  public int getNewColumn() {
    return newColumn;
  }

  /**
   * @return whether the currently active block parser should be replaced
   */
  public boolean isReplaceActiveBlockParser() {
    return replaceActiveBlockParser;
  }

  /**
   * @return the number of paragraph lines to replace, or {@code 0} if none
   */
  int getReplaceParagraphLines() {
    return replaceParagraphLines;
  }

  @Override
  /**
   * Request that parsing resume at a specific source index.
   *
   * @param newIndex character index to resume from
   * @return this result for chaining
   */
  public BlockStart atIndex(int newIndex) {
    this.newIndex = newIndex;
    return this;
  }

  @Override
  /**
   * Request that parsing resume at a specific visual column.
   *
   * @param newColumn column to resume from
   * @return this result for chaining
   */
  public BlockStart atColumn(int newColumn) {
    this.newColumn = newColumn;
    return this;
  }

  @Override
  /**
   * Mark the matched block parser as replaceable.
   *
   * @return this result for chaining
   */
  public BlockStart replaceActiveBlockParser() {
    replaceActiveBlockParser = true;
    return this;
  }

  @Override
  /**
   * Mark a number of paragraph lines as replaceable by the new block.
   *
   * @param lines number of paragraph lines to replace
   * @return this result for chaining
   */
  public BlockStart replaceParagraphLines(int lines) {
    if (lines < 1) {
      throw new IllegalArgumentException("Lines must be >= 1");
    }
    replaceParagraphLines = lines;
    return this;
  }
}
