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

import org.dominokit.markdown.parser.block.BlockContinue;

/**
 * Concrete continuation result for block parsers.
 *
 * <p>The parser uses this implementation to carry the next index or column to resume from and to
 * signal whether the current block should be finalized immediately.
 */
public class BlockContinueImpl extends BlockContinue {

  private final int newIndex;
  private final int newColumn;
  private final boolean finalize;

  /**
   * Create a continuation result.
   *
   * @param newIndex next character index to continue at, or {@code -1} when unchanged
   * @param newColumn next column to continue at, or {@code -1} when unchanged
   * @param finalize whether the current block should be finalized instead of continued
   */
  public BlockContinueImpl(int newIndex, int newColumn, boolean finalize) {
    this.newIndex = newIndex;
    this.newColumn = newColumn;
    this.finalize = finalize;
  }

  /**
   * @return the next character index to continue at, or {@code -1} if unchanged
   */
  public int getNewIndex() {
    return newIndex;
  }

  /**
   * @return the next column to continue at, or {@code -1} if unchanged
   */
  public int getNewColumn() {
    return newColumn;
  }

  /**
   * @return whether the current block should be finalized instead of continued
   */
  public boolean isFinalize() {
    return finalize;
  }
}
