/*
 * Copyright © 2026 Dominokit
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
package org.dominokit.markdown.ext.gfm.strikethrough.internal;

import org.dominokit.markdown.ext.gfm.strikethrough.Strikethrough;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.Nodes;
import org.dominokit.markdown.node.SourceSpans;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.delimiter.DelimiterProcessor;
import org.dominokit.markdown.parser.delimiter.DelimiterRun;

/** Delimiter processor for {@code ~} strikethrough runs. */
public class StrikethroughDelimiterProcessor implements DelimiterProcessor {

  private final boolean requireTwoTildes;

  /** Create a processor with the requested minimum delimiter length. */
  public StrikethroughDelimiterProcessor(boolean requireTwoTildes) {
    this.requireTwoTildes = requireTwoTildes;
  }

  @Override
  /**
   * @return the opening delimiter character
   */
  public char getOpeningCharacter() {
    return '~';
  }

  @Override
  /**
   * @return the closing delimiter character
   */
  public char getClosingCharacter() {
    return '~';
  }

  @Override
  /**
   * @return the minimum delimiter length
   */
  public int getMinLength() {
    return requireTwoTildes ? 2 : 1;
  }

  @Override
  /** Build a strikethrough node when the opening and closing runs are compatible. */
  public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
    if (openingRun.length() == closingRun.length() && openingRun.length() <= 2) {
      Text opener = openingRun.getOpener();
      String delimiter =
          openingRun.length() == 1
              ? opener.getLiteral()
              : opener.getLiteral() + opener.getLiteral();

      Node strikethrough = new Strikethrough(delimiter);
      SourceSpans sourceSpans = SourceSpans.empty();
      sourceSpans.addAllFrom(openingRun.getOpeners(openingRun.length()));

      for (Node node : Nodes.between(opener, closingRun.getCloser())) {
        strikethrough.appendChild(node);
        sourceSpans.addAll(node.getSourceSpans());
      }

      sourceSpans.addAllFrom(closingRun.getClosers(closingRun.length()));
      strikethrough.setSourceSpans(sourceSpans.getSourceSpans());
      opener.insertAfter(strikethrough);
      return openingRun.length();
    }

    return 0;
  }
}
