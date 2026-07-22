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

/**
 * A single logical line from the original source, optionally paired with a source span.
 *
 * <p>The parser uses {@code SourceLine} both for whole lines and for slices of a line when block
 * parsers consume leading indentation. The content and the source-span view stay aligned as long as
 * callers create derived instances through {@link #substring(int, int)}.
 */
public class SourceLine {

  private final CharSequence content;
  private final SourceSpan sourceSpan;

  /**
   * Create a source line from content and an optional source span.
   *
   * @param content the logical line content
   * @param sourceSpan the corresponding source span, or {@code null}
   * @return a new source line
   */
  public static SourceLine of(CharSequence content, SourceSpan sourceSpan) {
    return new SourceLine(content, sourceSpan);
  }

  private SourceLine(CharSequence content, SourceSpan sourceSpan) {
    this.content = Objects.requireNonNull(content, "content must not be null");
    this.sourceSpan = sourceSpan;
  }

  /**
   * @return the line content
   */
  public CharSequence getContent() {
    return content;
  }

  /**
   * @return the source span associated with this line, or {@code null}
   */
  public SourceSpan getSourceSpan() {
    return sourceSpan;
  }

  /**
   * Return a slice of this line with a correspondingly adjusted source span.
   *
   * <p>The new line reuses the requested content range and translates the span start forward by
   * {@code beginIndex}. Empty slices intentionally drop the span because they do not correspond to
   * a visible source range.
   */
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
