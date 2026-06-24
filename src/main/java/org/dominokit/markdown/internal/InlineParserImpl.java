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
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.SoftLineBreak;
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.InlineParser;
import org.dominokit.markdown.parser.InlineParserContext;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.SourceLines;

/**
 * Temporary inline parser used during the block parser phase.
 *
 * <p>It preserves plain textual content and soft line breaks so the block parser produces usable
 * ASTs before full inline syntax parsing lands in Phase 4.
 */
public class InlineParserImpl implements InlineParser {

  public InlineParserImpl(InlineParserContext inlineParserContext) {}

  @Override
  public void parse(SourceLines lines, Node node) {
    List<SourceLine> sourceLines = lines.getLines();
    for (int i = 0; i < sourceLines.size(); i++) {
      if (i != 0) {
        node.appendChild(new SoftLineBreak());
      }
      SourceLine sourceLine = sourceLines.get(i);
      String literal = sourceLine.getContent().toString();
      if (!literal.isEmpty()) {
        Text text = new Text(literal);
        SourceSpan sourceSpan = sourceLine.getSourceSpan();
        if (sourceSpan != null) {
          text.setSourceSpans(List.of(sourceSpan));
        }
        node.appendChild(text);
      }
    }
  }
}
