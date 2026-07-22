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

/**
 * Block parser that accumulates paragraph text and trailing link-reference definitions.
 *
 * <p>Paragraph parsing is special because a paragraph can be repurposed into link-reference
 * definitions before inline parsing runs. The parser therefore stores raw lines separately, keeps
 * track of any definition-like prefixes, and only turns the remaining paragraph content into inline
 * children at the end.
 */
public class ParagraphParser extends AbstractBlockParser {

  private final Paragraph block = new Paragraph();
  private final LinkReferenceDefinitionParser linkReferenceDefinitionParser =
      new LinkReferenceDefinitionParser();

  /**
   * @return {@code true} when the parser can continue with the current line
   */
  @Override
  public boolean canHaveLazyContinuationLines() {
    return true;
  }

  /**
   * @return the paragraph block being built
   */
  @Override
  public Block getBlock() {
    return block;
  }

  /**
   * Continue the paragraph when the line is not blank; otherwise end the block.
   *
   * @param state current parser state
   * @return a continuation into the existing paragraph or a termination signal
   */
  @Override
  public BlockContinue tryContinue(ParserState state) {
    if (!state.isBlank()) {
      return BlockContinue.atIndex(state.getIndex());
    } else {
      return BlockContinue.none();
    }
  }

  /** Record raw line content for later paragraph and definition processing. */
  @Override
  public void addLine(SourceLine line) {
    linkReferenceDefinitionParser.parse(line);
  }

  /**
   * Record source spans for the portion of the line that belongs to the paragraph or any embedded
   * reference definitions.
   */
  @Override
  public void addSourceSpan(SourceSpan sourceSpan) {
    // Some source spans might belong to link reference definitions, others to the paragraph.
    // The parser will handle that.
    linkReferenceDefinitionParser.addSourceSpan(sourceSpan);
  }

  /**
   * Expose any link-reference definitions that were extracted from the paragraph text.
   *
   * @return a singleton list containing the paragraph's definitions map
   */
  @Override
  public List<DefinitionMap<?>> getDefinitions() {
    var map = new DefinitionMap<>(LinkReferenceDefinition.class);
    for (var def : linkReferenceDefinitionParser.getDefinitions()) {
      map.putIfAbsent(def.getLabel(), def);
    }
    return List.of(map);
  }

  /**
   * Finalize the paragraph by inserting extracted definitions before it and dropping the block when
   * no paragraph content remains.
   */
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

  /** Parse the accumulated paragraph lines into inline children. */
  @Override
  public void parseInlines(InlineParser inlineParser) {
    SourceLines lines = linkReferenceDefinitionParser.getParagraphLines();
    if (!lines.isEmpty()) {
      inlineParser.parse(lines, block);
    }
  }

  /**
   * @return the paragraph lines that still belong to the paragraph body
   */
  public SourceLines getParagraphLines() {
    return linkReferenceDefinitionParser.getParagraphLines();
  }

  /**
   * Remove a number of lines from the paragraph body so another block can take over them.
   *
   * @param lines number of leading lines to remove
   * @return source spans associated with the removed lines
   */
  public List<SourceSpan> removeLines(int lines) {
    return linkReferenceDefinitionParser.removeLines(lines);
  }
}
