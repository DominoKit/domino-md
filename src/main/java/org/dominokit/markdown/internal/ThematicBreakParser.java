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
import org.dominokit.markdown.node.ThematicBreak;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.AbstractBlockParserFactory;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.BlockStart;
import org.dominokit.markdown.parser.block.MatchedBlockParser;
import org.dominokit.markdown.parser.block.ParserState;

public class ThematicBreakParser extends AbstractBlockParser {

  private final ThematicBreak block = new ThematicBreak();

  public ThematicBreakParser(String literal) {
    block.setLiteral(literal);
  }

  @Override
  public Block getBlock() {
    return block;
  }

  @Override
  public BlockContinue tryContinue(ParserState state) {
    return BlockContinue.none();
  }

  public static class Factory extends AbstractBlockParserFactory {

    @Override
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
      if (state.getIndent() >= 4) {
        return BlockStart.none();
      }
      int nextNonSpace = state.getNextNonSpaceIndex();
      CharSequence line = state.getLine().getContent();
      if (isThematicBreak(line, nextNonSpace)) {
        String literal = String.valueOf(line.subSequence(state.getIndex(), line.length()));
        return BlockStart.of(new ThematicBreakParser(literal)).atIndex(line.length());
      }
      return BlockStart.none();
    }
  }

  private static boolean isThematicBreak(CharSequence line, int index) {
    int dashes = 0;
    int underscores = 0;
    int asterisks = 0;
    for (int i = index; i < line.length(); i++) {
      switch (line.charAt(i)) {
        case '-':
          dashes++;
          break;
        case '_':
          underscores++;
          break;
        case '*':
          asterisks++;
          break;
        case ' ':
        case '\t':
          break;
        default:
          return false;
      }
    }
    return (dashes >= 3 && underscores == 0 && asterisks == 0)
        || (underscores >= 3 && dashes == 0 && asterisks == 0)
        || (asterisks >= 3 && dashes == 0 && underscores == 0);
  }
}
