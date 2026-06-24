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
package org.dominokit.markdown.internal.inline;

import org.dominokit.markdown.node.*;
import org.dominokit.markdown.parser.delimiter.DelimiterProcessor;
import org.dominokit.markdown.parser.delimiter.DelimiterRun;

public abstract class EmphasisDelimiterProcessor implements DelimiterProcessor {

  private final char delimiterChar;

  protected EmphasisDelimiterProcessor(char delimiterChar) {
    this.delimiterChar = delimiterChar;
  }

  @Override
  public char getOpeningCharacter() {
    return delimiterChar;
  }

  @Override
  public char getClosingCharacter() {
    return delimiterChar;
  }

  @Override
  public int getMinLength() {
    return 1;
  }

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
