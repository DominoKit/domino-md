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

import java.util.ArrayList;
import java.util.List;
import org.dominokit.markdown.internal.util.Parsing;
import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.IndentedCodeBlock;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.AbstractBlockParserFactory;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.BlockStart;
import org.dominokit.markdown.parser.block.MatchedBlockParser;
import org.dominokit.markdown.parser.block.ParserState;
import org.dominokit.markdown.text.Characters;

public class IndentedCodeBlockParser extends AbstractBlockParser {

  private final IndentedCodeBlock block = new IndentedCodeBlock();
  private final List<CharSequence> lines = new ArrayList<>();

  @Override
  public Block getBlock() {
    return block;
  }

  @Override
  public BlockContinue tryContinue(ParserState state) {
    if (state.getIndent() >= Parsing.CODE_BLOCK_INDENT) {
      return BlockContinue.atColumn(state.getColumn() + Parsing.CODE_BLOCK_INDENT);
    }
    if (state.isBlank()) {
      return BlockContinue.atIndex(state.getNextNonSpaceIndex());
    }
    return BlockContinue.none();
  }

  @Override
  public void addLine(SourceLine line) {
    lines.add(line.getContent());
  }

  @Override
  public void closeBlock() {
    int lastNonBlank = lines.size() - 1;
    while (lastNonBlank >= 0 && Characters.isBlank(lines.get(lastNonBlank))) {
      lastNonBlank--;
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < lastNonBlank + 1; i++) {
      sb.append(lines.get(i)).append('\n');
    }
    block.setLiteral(sb.toString());
  }

  public static class Factory extends AbstractBlockParserFactory {

    @Override
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
      if (state.getIndent() >= Parsing.CODE_BLOCK_INDENT
          && !state.isBlank()
          && !(state.getActiveBlockParser().getBlock() instanceof Paragraph)) {
        return BlockStart.of(new IndentedCodeBlockParser())
            .atColumn(state.getColumn() + Parsing.CODE_BLOCK_INDENT);
      }
      return BlockStart.none();
    }
  }
}
