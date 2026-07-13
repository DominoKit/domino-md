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

/**
 * Parses thematic breaks such as {@code ---}, {@code ***}, or {@code ___}.
 *
 * <p>The parser treats the opening line as the complete block literal and never continues onto
 * subsequent lines because thematic breaks are always single-line constructs.
 */
public class ThematicBreakParser extends AbstractBlockParser {

  private final ThematicBreak block = new ThematicBreak();

  /**
   * Create a thematic break node with the literal content already extracted from the source line.
   *
   * @param literal raw markdown source for the break line
   */
  public ThematicBreakParser(String literal) {
    block.setLiteral(literal);
  }

  /** @return the thematic break node being built */
  @Override
  public Block getBlock() {
    return block;
  }

  /**
   * Thematic breaks never continue on later lines.
   *
   * @param state current parser state
   * @return always none
   */
  @Override
  public BlockContinue tryContinue(ParserState state) {
    return BlockContinue.none();
  }

  /**
   * Recognizes thematic break lines.
   *
   * <p>The factory rejects lines indented by four or more spaces, then checks whether the
   * non-indented content is a valid thematic break candidate. The full original line is preserved
   * as the block literal.
   */
  public static class Factory extends AbstractBlockParserFactory {

    /**
     * Try to start a thematic break block at the current line.
     *
     * @param state current parser state
     * @param matchedBlockParser most recent matched block parser
     * @return a parser start when the line is a thematic break, otherwise none
     */
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

  /**
   * Check whether the line content from the supplied index forms a valid thematic break.
   *
   * <p>The method counts one delimiter family at a time and rejects any line that mixes {@code -},
   * {@code _}, or {@code *} with other non-whitespace characters. A valid break requires at least
   * three markers of a single family.
   *
   * @param line line content to inspect
   * @param index first non-space character on the line
   * @return {@code true} when the line is a thematic break candidate
   */
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
