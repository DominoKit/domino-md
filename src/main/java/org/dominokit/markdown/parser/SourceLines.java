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

/** A set of lines from the input source. */
public class SourceLines {

  private final List<SourceLine> lines = new ArrayList<>();

  public static SourceLines empty() {
    return new SourceLines();
  }

  public static SourceLines of(SourceLine sourceLine) {
    SourceLines sourceLines = new SourceLines();
    sourceLines.addLine(sourceLine);
    return sourceLines;
  }

  public static SourceLines of(List<SourceLine> sourceLines) {
    SourceLines result = new SourceLines();
    result.lines.addAll(sourceLines);
    return result;
  }

  public void addLine(SourceLine sourceLine) {
    lines.add(sourceLine);
  }

  public List<SourceLine> getLines() {
    return lines;
  }

  public boolean isEmpty() {
    return lines.isEmpty();
  }

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
