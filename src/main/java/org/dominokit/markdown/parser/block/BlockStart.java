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
package org.dominokit.markdown.parser.block;

import org.dominokit.markdown.internal.BlockStartImpl;

/**
 * Result object returned by a block parser factory when a new block should start.
 *
 * <p>The object records which block parsers to push and how the current scanner position should be
 * adjusted before parsing continues.
 */
public abstract class BlockStart {

  protected BlockStart() {}

  /**
   * @return no block start
   */
  public static BlockStart none() {
    return null;
  }

  /**
   * Create a start result that opens the supplied block parsers in order.
   *
   * @param blockParsers the parsers to activate
   * @return the start result
   */
  public static BlockStart of(BlockParser... blockParsers) {
    return new BlockStartImpl(blockParsers);
  }

  /**
   * Move the scanner to the supplied character index before continuing.
   *
   * @param newIndex character index to resume from
   * @return the configured start result
   */
  public abstract BlockStart atIndex(int newIndex);

  /**
   * Move the scanner to the supplied visual column before continuing.
   *
   * @param newColumn visual column to resume from
   * @return the configured start result
   */
  public abstract BlockStart atColumn(int newColumn);

  /**
   * Replace the currently active block parser instead of adding a new child.
   *
   * @return the configured start result
   * @deprecated prefer {@link #replaceParagraphLines(int)} or a more specific block replacement
   *     strategy
   */
  @Deprecated
  public abstract BlockStart replaceActiveBlockParser();

  /**
   * Replace a number of paragraph lines with the new block.
   *
   * @param lines how many paragraph lines should be replaced
   * @return the configured start result
   */
  public abstract BlockStart replaceParagraphLines(int lines);
}
