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
package org.dominokit.markdown.parser;

import java.util.ArrayList;
import java.util.List;
import org.dominokit.markdown.node.SourceSpan;

/**
 * Ordered collection of source lines.
 *
 * <p>The parser uses this type to preserve the original line boundaries for block content while
 * still providing easy access to the concatenated content and the combined set of source spans.
 */
public class SourceLines {

  private final List<SourceLine> lines = new ArrayList<>();

  /**
   * @return an empty line collection
   */
  public static SourceLines empty() {
    return new SourceLines();
  }

  /**
   * Create a collection containing a single source line.
   *
   * @param sourceLine the initial line
   * @return the new collection
   */
  public static SourceLines of(SourceLine sourceLine) {
    SourceLines sourceLines = new SourceLines();
    sourceLines.addLine(sourceLine);
    return sourceLines;
  }

  /**
   * Create a collection containing the supplied lines.
   *
   * @param sourceLines the lines to copy into the new collection
   * @return the new collection
   */
  public static SourceLines of(List<SourceLine> sourceLines) {
    SourceLines result = new SourceLines();
    result.lines.addAll(sourceLines);
    return result;
  }

  /** Add a line to the end of the collection. */
  public void addLine(SourceLine sourceLine) {
    lines.add(sourceLine);
  }

  /**
   * @return the underlying ordered line list
   */
  public List<SourceLine> getLines() {
    return lines;
  }

  /**
   * @return whether this collection contains no lines
   */
  public boolean isEmpty() {
    return lines.isEmpty();
  }

  /**
   * Concatenate the line contents using newline separators.
   *
   * <p>The collection stores logical lines, so this method reconstructs the exact block text that
   * the inline parser will later consume.
   */
  public String getContent() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < lines.size(); i++) {
      if (i != 0) {
        sb.append('\n');
      }
      sb.append(lines.get(i).getContent());
    }
    return sb.toString();
  }

  /**
   * Collect the non-null source spans from the stored lines.
   *
   * <p>The returned list preserves input order and omits lines that were not tracked.
   */
  public List<SourceSpan> getSourceSpans() {
    List<SourceSpan> sourceSpans = new ArrayList<>();
    for (SourceLine line : lines) {
      SourceSpan sourceSpan = line.getSourceSpan();
      if (sourceSpan != null) {
        sourceSpans.add(sourceSpan);
      }
    }
    return sourceSpans;
  }
}
