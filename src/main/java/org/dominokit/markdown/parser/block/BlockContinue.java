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

import org.dominokit.markdown.internal.BlockContinueImpl;

/**
 * Result object returned by a block parser when deciding how to continue on the current line.
 *
 * <p>Factory methods return either a continuation at a specific index or column, a completion
 * signal, or {@code null} when the block cannot continue.
 */
public class BlockContinue {

  protected BlockContinue() {}

  /** @return no continuation result */
  public static BlockContinue none() {
    return null;
  }

  /**
   * Continue parsing from the supplied character index.
   *
   * @param newIndex character index to resume from
   * @return a continuation result
   */
  public static BlockContinue atIndex(int newIndex) {
    return new BlockContinueImpl(newIndex, -1, false);
  }

  /**
   * Continue parsing from the supplied visual column.
   *
   * @param newColumn column to resume from
   * @return a continuation result
   */
  public static BlockContinue atColumn(int newColumn) {
    return new BlockContinueImpl(-1, newColumn, false);
  }

  /** Signal that the current block should be finalized. */
  public static BlockContinue finished() {
    return new BlockContinueImpl(-1, -1, true);
  }
}
