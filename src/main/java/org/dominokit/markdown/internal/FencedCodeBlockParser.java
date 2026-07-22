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

import static org.dominokit.markdown.internal.util.Escaping.unescapeString;

import org.dominokit.markdown.internal.util.Parsing;
import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.FencedCodeBlock;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.AbstractBlockParserFactory;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.BlockStart;
import org.dominokit.markdown.parser.block.MatchedBlockParser;
import org.dominokit.markdown.parser.block.ParserState;
import org.dominokit.markdown.text.Characters;

/**
 * Parses fenced code blocks delimited by backticks or tildes.
 *
 * <p>The parser stores the opening fence metadata, preserves the first info string line separately
 * from the remaining literal content, and stops when a valid closing fence appears at or beyond the
 * opening fence length.
 */
public class FencedCodeBlockParser extends AbstractBlockParser {

  private final FencedCodeBlock block = new FencedCodeBlock();
  private final char fenceChar;
  private final int openingFenceLength;
  private String firstLine;
  private final StringBuilder otherLines = new StringBuilder();

  /**
   * Create a fenced code block parser for a specific fence character and opening fence length.
   *
   * @param fenceChar the fence delimiter character
   * @param fenceLength number of fence characters in the opening fence
   * @param fenceIndent indentation before the opening fence
   */
  public FencedCodeBlockParser(char fenceChar, int fenceLength, int fenceIndent) {
    this.fenceChar = fenceChar;
    this.openingFenceLength = fenceLength;
    block.setFenceCharacter(String.valueOf(fenceChar));
    block.setOpeningFenceLength(fenceLength);
    block.setFenceIndent(fenceIndent);
  }

  /**
   * @return the fenced code block being constructed
   */
  @Override
  public Block getBlock() {
    return block;
  }

  /**
   * Continue until a closing fence with enough delimiter characters appears.
   *
   * @param state current parser state
   * @return the continuation result, or finished when a valid closing fence is found
   */
  @Override
  public BlockContinue tryContinue(ParserState state) {
    int nextNonSpace = state.getNextNonSpaceIndex();
    int newIndex = state.getIndex();
    CharSequence line = state.getLine().getContent();
    if (state.getIndent() < Parsing.CODE_BLOCK_INDENT
        && nextNonSpace < line.length()
        && tryClosing(line, nextNonSpace)) {
      return BlockContinue.finished();
    }

    int i = block.getFenceIndent();
    while (i > 0 && newIndex < line.length() && line.charAt(newIndex) == ' ') {
      newIndex++;
      i--;
    }
    return BlockContinue.atIndex(newIndex);
  }

  /**
   * Preserve the first body line separately so it can be used as the info string when the block
   * closes.
   *
   * @param line source line to append
   */
  @Override
  public void addLine(SourceLine line) {
    if (firstLine == null) {
      firstLine = line.getContent().toString();
    } else {
      otherLines.append(line.getContent()).append('\n');
    }
  }

  /**
   * Finalize the code block by normalizing the info string and concatenating the remaining body
   * lines.
   */
  @Override
  public void closeBlock() {
    block.setInfo(unescapeString(firstLine.trim()));
    block.setLiteral(otherLines.toString());
  }

  /** Recognizes fenced code block openings. */
  public static class Factory extends AbstractBlockParserFactory {

    /**
     * Try to start a fenced code block at the current line.
     *
     * @param state current parser state
     * @param matchedBlockParser most recent matched block parser
     * @return a parser start when the line opens a fenced code block, otherwise none
     */
    @Override
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
      int indent = state.getIndent();
      if (indent >= Parsing.CODE_BLOCK_INDENT) {
        return BlockStart.none();
      }
      int nextNonSpace = state.getNextNonSpaceIndex();
      FencedCodeBlockParser blockParser =
          checkOpener(state.getLine().getContent(), nextNonSpace, indent);
      if (blockParser != null) {
        return BlockStart.of(blockParser)
            .atIndex(nextNonSpace + blockParser.block.getOpeningFenceLength());
      }
      return BlockStart.none();
    }
  }

  /**
   * Inspect the line for a valid opening fence and build a parser when one is present.
   *
   * @param line line content
   * @param index first non-space character
   * @param indent indentation before the fence
   * @return a parser when a valid opening fence is found, otherwise {@code null}
   */
  private static FencedCodeBlockParser checkOpener(CharSequence line, int index, int indent) {
    int backticks = 0;
    int tildes = 0;
    for (int i = index; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '`') {
        backticks++;
      } else if (c == '~') {
        tildes++;
      } else {
        break;
      }
    }

    if (backticks >= 3 && tildes == 0) {
      if (Characters.find('`', line, index + backticks) != -1) {
        return null;
      }
      return new FencedCodeBlockParser('`', backticks, indent);
    }
    if (tildes >= 3 && backticks == 0) {
      return new FencedCodeBlockParser('~', tildes, indent);
    }
    return null;
  }

  /**
   * Determine whether the current line closes the fence.
   *
   * <p>The closer must use the same fence character, must be at least as long as the opener, and
   * may only be followed by optional spaces or tabs.
   *
   * @param line line content
   * @param index first non-space character
   * @return {@code true} when the line closes the fenced code block
   */
  private boolean tryClosing(CharSequence line, int index) {
    int fences = Characters.skip(fenceChar, line, index, line.length()) - index;
    if (fences < openingFenceLength) {
      return false;
    }
    int after = Characters.skipSpaceTab(line, index + fences, line.length());
    if (after == line.length()) {
      block.setClosingFenceLength(fences);
      return true;
    }
    return false;
  }
}
