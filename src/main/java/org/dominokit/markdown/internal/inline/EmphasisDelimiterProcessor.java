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
package org.dominokit.markdown.internal.inline;

import org.dominokit.markdown.node.*;
import org.dominokit.markdown.parser.delimiter.DelimiterProcessor;
import org.dominokit.markdown.parser.delimiter.DelimiterRun;

/**
 * Shared implementation for emphasis and strong emphasis delimiters.
 *
 * <p>The processor applies the CommonMark multiple-of-three rule, chooses between emphasis and
 * strong emphasis based on the number of available delimiters, and then moves all nodes between the
 * opener and closer inside the new wrapper node while preserving source spans.
 */
public abstract class EmphasisDelimiterProcessor implements DelimiterProcessor {

  private final char delimiterChar;

  /**
   * Create a processor for the requested delimiter character.
   *
   * @param delimiterChar delimiter character handled by this processor
   */
  protected EmphasisDelimiterProcessor(char delimiterChar) {
    this.delimiterChar = delimiterChar;
  }

  /**
   * @return the delimiter character that can open this run
   */
  @Override
  public char getOpeningCharacter() {
    return delimiterChar;
  }

  /**
   * @return the delimiter character that can close this run
   */
  @Override
  public char getClosingCharacter() {
    return delimiterChar;
  }

  /**
   * @return the minimum number of delimiters required to activate the processor
   */
  @Override
  public int getMinLength() {
    return 1;
  }

  /**
   * Combine matching delimiter runs into emphasis or strong emphasis nodes.
   *
   * <p>The implementation first applies the multiple-of-three restriction used by CommonMark to
   * avoid ambiguous matches. It then prefers strong emphasis whenever both the opening and closing
   * runs still contain at least two delimiters, otherwise it falls back to a single emphasis node.
   * All nodes between the opener and closer are moved inside the new wrapper, and the source spans
   * from the consumed delimiters are copied onto the wrapper node.
   *
   * @param openingRun opening delimiter run
   * @param closingRun closing delimiter run
   * @return the number of delimiters consumed from the closer
   */
  @Override
  public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
    // "multiple of 3" rule for internal delimiter runs
    if ((openingRun.canClose() || closingRun.canOpen())
        && closingRun.originalLength() % 3 != 0
        && (openingRun.originalLength() + closingRun.originalLength()) % 3 == 0) {
      return 0;
    }

    int usedDelimiters;
    Node emphasis;
    // calculate actual number of delimiters used from this closer
    if (openingRun.length() >= 2 && closingRun.length() >= 2) {
      usedDelimiters = 2;
      emphasis = new StrongEmphasis(String.valueOf(delimiterChar) + delimiterChar);
    } else {
      usedDelimiters = 1;
      emphasis = new Emphasis(String.valueOf(delimiterChar));
    }

    SourceSpans sourceSpans = SourceSpans.empty();
    sourceSpans.addAllFrom(openingRun.getOpeners(usedDelimiters));

    Text opener = openingRun.getOpener();
    for (Node node : Nodes.between(opener, closingRun.getCloser())) {
      emphasis.appendChild(node);
      sourceSpans.addAll(node.getSourceSpans());
    }

    sourceSpans.addAllFrom(closingRun.getClosers(usedDelimiters));

    emphasis.setSourceSpans(sourceSpans.getSourceSpans());
    opener.insertAfter(emphasis);

    return usedDelimiters;
  }
}
