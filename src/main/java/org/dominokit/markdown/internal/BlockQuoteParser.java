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

import org.dominokit.markdown.internal.util.Parsing;
import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.BlockQuote;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.AbstractBlockParserFactory;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.BlockStart;
import org.dominokit.markdown.parser.block.MatchedBlockParser;
import org.dominokit.markdown.parser.block.ParserState;
import org.dominokit.markdown.text.Characters;

public class BlockQuoteParser extends AbstractBlockParser {

  private final BlockQuote block = new BlockQuote();

  @Override
  public boolean isContainer() {
    return true;
  }

  @Override
  public boolean canContain(Block block) {
    return true;
  }

  @Override
  public BlockQuote getBlock() {
    return block;
  }

  @Override
  public BlockContinue tryContinue(ParserState state) {
    int nextNonSpace = state.getNextNonSpaceIndex();
    if (isMarker(state, nextNonSpace)) {
      int newColumn = state.getColumn() + state.getIndent() + 1;
      if (Characters.isSpaceOrTab(state.getLine().getContent(), nextNonSpace + 1)) {
        newColumn++;
      }
      return BlockContinue.atColumn(newColumn);
    }
    return BlockContinue.none();
  }

  private static boolean isMarker(ParserState state, int index) {
    CharSequence line = state.getLine().getContent();
    return state.getIndent() < Parsing.CODE_BLOCK_INDENT
        && index < line.length()
        && line.charAt(index) == '>';
  }

  public static class Factory extends AbstractBlockParserFactory {

    @Override
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
      int nextNonSpace = state.getNextNonSpaceIndex();
      if (isMarker(state, nextNonSpace)) {
        int newColumn = state.getColumn() + state.getIndent() + 1;
        if (Characters.isSpaceOrTab(state.getLine().getContent(), nextNonSpace + 1)) {
          newColumn++;
        }
        return BlockStart.of(new BlockQuoteParser()).atColumn(newColumn);
      }
      return BlockStart.none();
    }
  }
}
