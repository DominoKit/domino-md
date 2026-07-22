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

/** Factory that decides whether the current line starts a block parser. */
public interface BlockParserFactory {

  /**
   * Try to start a block at the current parser state.
   *
   * @param state the current parser state
   * @param matchedBlockParser the block parser that already matched the current line
   * @return a block-start result, or {@code null}
   */
  BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser);
}
