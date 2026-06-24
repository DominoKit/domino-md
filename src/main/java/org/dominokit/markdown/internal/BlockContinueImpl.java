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

public class BlockContinueImpl extends BlockContinue {

  private final int newIndex;
  private final int newColumn;
  private final boolean finalize;

  public BlockContinueImpl(int newIndex, int newColumn, boolean finalize) {
    this.newIndex = newIndex;
    this.newColumn = newColumn;
    this.finalize = finalize;
  }

  public int getNewIndex() {
    return newIndex;
  }

  public int getNewColumn() {
    return newColumn;
  }

  public boolean isFinalize() {
    return finalize;
  }
}
