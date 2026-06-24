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

/** Result object for starting parsing of a block. */
public abstract class BlockStart {

  protected BlockStart() {}

  public static BlockStart none() {
    return null;
  }

  public static BlockStart of(BlockParser... blockParsers) {
    return new BlockStartImpl(blockParsers);
  }

  public abstract BlockStart atIndex(int newIndex);

  public abstract BlockStart atColumn(int newColumn);

  @Deprecated
  public abstract BlockStart replaceActiveBlockParser();

  public abstract BlockStart replaceParagraphLines(int lines);
}
