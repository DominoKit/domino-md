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

import java.util.Objects;
import org.dominokit.markdown.node.SourceSpan;

/** A line or part of a line from the input source. */
public class SourceLine {

  private final CharSequence content;
  private final SourceSpan sourceSpan;

  public static SourceLine of(CharSequence content, SourceSpan sourceSpan) {
    return new SourceLine(content, sourceSpan);
  }

  private SourceLine(CharSequence content, SourceSpan sourceSpan) {
    this.content = Objects.requireNonNull(content, "content must not be null");
    this.sourceSpan = sourceSpan;
  }

  public CharSequence getContent() {
    return content;
  }

  public SourceSpan getSourceSpan() {
    return sourceSpan;
  }

  public SourceLine substring(int beginIndex, int endIndex) {
    CharSequence newContent = content.subSequence(beginIndex, endIndex);
    SourceSpan newSourceSpan = null;
    if (sourceSpan != null) {
      int length = endIndex - beginIndex;
      if (length != 0) {
        newSourceSpan =
            SourceSpan.of(
                sourceSpan.getLineIndex(),
                sourceSpan.getColumnIndex() + beginIndex,
                sourceSpan.getInputIndex() + beginIndex,
                length);
      }
    }
    return SourceLine.of(newContent, newSourceSpan);
  }
}
