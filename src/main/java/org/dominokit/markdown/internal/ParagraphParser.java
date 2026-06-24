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
import java.util.Collections;
import java.util.List;
import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.parser.InlineParser;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.SourceLines;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.ParserState;

public class ParagraphParser extends AbstractBlockParser {

  private final Paragraph block = new Paragraph();
  private final List<SourceLine> paragraphLines = new ArrayList<>();
  private final List<SourceSpan> paragraphSourceSpans = new ArrayList<>();

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
    return state.isBlank() ? BlockContinue.none() : BlockContinue.atIndex(state.getIndex());
  }

  @Override
  public void addLine(SourceLine line) {
    paragraphLines.add(line);
  }

  @Override
  public void addSourceSpan(SourceSpan sourceSpan) {
    paragraphSourceSpans.add(sourceSpan);
  }

  @Override
  public void closeBlock() {
    if (paragraphLines.isEmpty()) {
      block.unlink();
    } else {
      block.setSourceSpans(paragraphSourceSpans);
    }
  }

  @Override
  public void parseInlines(InlineParser inlineParser) {
    SourceLines lines = getParagraphLines();
    if (!lines.isEmpty()) {
      inlineParser.parse(lines, block);
    }
  }

  public SourceLines getParagraphLines() {
    return SourceLines.of(paragraphLines);
  }

  public List<SourceSpan> removeLines(int lines) {
    int start = Math.max(paragraphSourceSpans.size() - lines, 0);
    List<SourceSpan> removed =
        new ArrayList<>(paragraphSourceSpans.subList(start, paragraphSourceSpans.size()));
    removeLast(lines, paragraphLines);
    removeLast(lines, paragraphSourceSpans);
    return Collections.unmodifiableList(removed);
  }

  private static <T> void removeLast(int count, List<T> list) {
    if (count >= list.size()) {
      list.clear();
      return;
    }
    for (int i = 0; i < count; i++) {
      list.remove(list.size() - 1);
    }
  }
}
