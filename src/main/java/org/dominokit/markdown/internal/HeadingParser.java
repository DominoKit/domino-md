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
import org.dominokit.markdown.node.Heading;
import org.dominokit.markdown.parser.InlineParser;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.SourceLines;
import org.dominokit.markdown.parser.beta.Position;
import org.dominokit.markdown.parser.beta.Scanner;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.AbstractBlockParserFactory;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.BlockStart;
import org.dominokit.markdown.parser.block.MatchedBlockParser;
import org.dominokit.markdown.parser.block.ParserState;
import org.dominokit.markdown.text.Characters;

public class HeadingParser extends AbstractBlockParser {

  private final Heading block = new Heading();
  private final SourceLines content;

  public HeadingParser(int level, SourceLines content) {
    block.setLevel(level);
    this.content = content;
  }

  @Override
  public Block getBlock() {
    return block;
  }

  @Override
  public BlockContinue tryContinue(ParserState parserState) {
    return BlockContinue.none();
  }

  @Override
  public void parseInlines(InlineParser inlineParser) {
    inlineParser.parse(content, block);
  }

  public static class Factory extends AbstractBlockParserFactory {

    @Override
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
      if (state.getIndent() >= Parsing.CODE_BLOCK_INDENT) {
        return BlockStart.none();
      }

      SourceLine line = state.getLine();
      int nextNonSpace = state.getNextNonSpaceIndex();
      if (line.getContent().charAt(nextNonSpace) == '#') {
        HeadingParser atxHeading =
            getAtxHeading(line.substring(nextNonSpace, line.getContent().length()));
        if (atxHeading != null) {
          return BlockStart.of(atxHeading).atIndex(line.getContent().length());
        }
      }

      int setextHeadingLevel = getSetextHeadingLevel(line.getContent(), nextNonSpace);
      if (setextHeadingLevel > 0) {
        SourceLines paragraph = matchedBlockParser.getParagraphLines();
        if (!paragraph.isEmpty()) {
          return BlockStart.of(new HeadingParser(setextHeadingLevel, paragraph))
              .atIndex(line.getContent().length())
              .replaceParagraphLines(paragraph.getLines().size());
        }
      }

      return BlockStart.none();
    }
  }

  private static HeadingParser getAtxHeading(SourceLine line) {
    Scanner scanner = Scanner.of(SourceLines.of(line));
    int level = scanner.matchMultiple('#');

    if (level == 0 || level > 6) {
      return null;
    }

    if (!scanner.hasNext()) {
      return new HeadingParser(level, SourceLines.empty());
    }

    char next = scanner.peek();
    if (next != ' ' && next != '\t') {
      return null;
    }

    scanner.whitespace();
    Position start = scanner.position();
    Position end = start;
    boolean hashCanEnd = true;

    while (scanner.hasNext()) {
      char c = scanner.peek();
      switch (c) {
        case '#':
          if (hashCanEnd) {
            scanner.matchMultiple('#');
            int whitespace = scanner.whitespace();
            if (scanner.hasNext()) {
              end = scanner.position();
            }
            hashCanEnd = whitespace > 0;
          } else {
            scanner.next();
            end = scanner.position();
          }
          break;
        case ' ':
        case '\t':
          hashCanEnd = true;
          scanner.next();
          break;
        default:
          hashCanEnd = false;
          scanner.next();
          end = scanner.position();
      }
    }

    SourceLines source = scanner.getSource(start, end);
    if (source.getContent().isEmpty()) {
      return new HeadingParser(level, SourceLines.empty());
    }
    return new HeadingParser(level, source);
  }

  private static int getSetextHeadingLevel(CharSequence line, int index) {
    char marker = line.charAt(index);
    if (marker == '=' && isSetextHeadingRest(line, index + 1, '=')) {
      return 1;
    }
    if (marker == '-' && isSetextHeadingRest(line, index + 1, '-')) {
      return 2;
    }
    return 0;
  }

  private static boolean isSetextHeadingRest(CharSequence line, int index, char marker) {
    int afterMarker = Characters.skip(marker, line, index, line.length());
    int afterSpace = Characters.skipSpaceTab(line, afterMarker, line.length());
    return afterSpace >= line.length();
  }
}
