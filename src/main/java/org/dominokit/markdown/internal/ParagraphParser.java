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

import java.util.List;
import org.dominokit.markdown.node.*;
import org.dominokit.markdown.parser.InlineParser;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.SourceLines;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.ParserState;

public class ParagraphParser extends AbstractBlockParser {

  private final Paragraph block = new Paragraph();
  private final LinkReferenceDefinitionParser linkReferenceDefinitionParser =
      new LinkReferenceDefinitionParser();

  @Override
  public boolean canHaveLazyContinuationLines() {
    return true;
  }

  @Override
  public Block getBlock() {
    return block;
  }

  @Override
  public BlockContinue tryContinue(ParserState state) {
    if (!state.isBlank()) {
      return BlockContinue.atIndex(state.getIndex());
    } else {
      return BlockContinue.none();
    }
  }

  @Override
  public void addLine(SourceLine line) {
    linkReferenceDefinitionParser.parse(line);
  }

  @Override
  public void addSourceSpan(SourceSpan sourceSpan) {
    // Some source spans might belong to link reference definitions, others to the paragraph.
    // The parser will handle that.
    linkReferenceDefinitionParser.addSourceSpan(sourceSpan);
  }

  @Override
  public List<DefinitionMap<?>> getDefinitions() {
    var map = new DefinitionMap<>(LinkReferenceDefinition.class);
    for (var def : linkReferenceDefinitionParser.getDefinitions()) {
      map.putIfAbsent(def.getLabel(), def);
    }
    return List.of(map);
  }

  @Override
  public void closeBlock() {
    for (var def : linkReferenceDefinitionParser.getDefinitions()) {
      block.insertBefore(def);
    }

    if (linkReferenceDefinitionParser.getParagraphLines().isEmpty()) {
      block.unlink();
    } else {
      block.setSourceSpans(linkReferenceDefinitionParser.getParagraphSourceSpans());
    }
  }

  @Override
  public void parseInlines(InlineParser inlineParser) {
    SourceLines lines = linkReferenceDefinitionParser.getParagraphLines();
    if (!lines.isEmpty()) {
      inlineParser.parse(lines, block);
    }
  }

  public SourceLines getParagraphLines() {
    return linkReferenceDefinitionParser.getParagraphLines();
  }

  public List<SourceSpan> removeLines(int lines) {
    return linkReferenceDefinitionParser.removeLines(lines);
  }
}
