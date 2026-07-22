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
package org.dominokit.markdown.node;

import java.util.Objects;

/**
 * Describes a contiguous slice of the original source input.
 *
 * <p>A span records three coordinates for the start of the slice: the line index, the column index,
 * and the absolute input index. The length is measured in characters from that start position.
 * Source spans are used to preserve origin information for parsed nodes and for any derived spans
 * created while slicing the source.
 */
public class SourceSpan {

  private final int lineIndex;
  private final int columnIndex;
  private final int inputIndex;
  private final int length;

  /**
   * Create a new span with explicit line, column, and absolute input coordinates.
   *
   * @param line the 0-based line index
   * @param column the 0-based column index within the line
   * @param input the 0-based absolute character index in the full source
   * @param length the number of characters covered by the span
   * @return a new source span
   */
  public static SourceSpan of(int line, int column, int input, int length) {
    return new SourceSpan(line, column, input, length);
  }

  /**
   * Create a span using only line, column, and length.
   *
   * <p>This overload is retained for older call sites that do not track absolute input offsets. New
   * code should prefer {@link #of(int, int, int, int)}.
   */
  @Deprecated
  public static SourceSpan of(int lineIndex, int columnIndex, int length) {
    return of(lineIndex, columnIndex, 0, length);
  }

  /**
   * Create a span from explicit coordinates.
   *
   * @param lineIndex 0-based line index
   * @param columnIndex 0-based column index within the line
   * @param inputIndex 0-based absolute input index
   * @param length number of characters covered by the span
   */
  private SourceSpan(int lineIndex, int columnIndex, int inputIndex, int length) {
    if (lineIndex < 0) {
      throw new IllegalArgumentException("lineIndex " + lineIndex + " must be >= 0");
    }
    if (columnIndex < 0) {
      throw new IllegalArgumentException("columnIndex " + columnIndex + " must be >= 0");
    }
    if (inputIndex < 0) {
      throw new IllegalArgumentException("inputIndex " + inputIndex + " must be >= 0");
    }
    if (length < 0) {
      throw new IllegalArgumentException("length " + length + " must be >= 0");
    }
    this.lineIndex = lineIndex;
    this.columnIndex = columnIndex;
    this.inputIndex = inputIndex;
    this.length = length;
  }

  /**
   * @return the 0-based line index where the span starts
   */
  public int getLineIndex() {
    return lineIndex;
  }

  /**
   * @return the 0-based column index where the span starts
   */
  public int getColumnIndex() {
    return columnIndex;
  }

  /**
   * @return the 0-based absolute input index where the span starts
   */
  public int getInputIndex() {
    return inputIndex;
  }

  /**
   * @return the length of the span in characters
   */
  public int getLength() {
    return length;
  }

  /**
   * Return a span from the requested start offset through the end of this span.
   *
   * @param beginIndex the starting offset relative to this span
   * @return a derived span starting at {@code beginIndex}
   */
  public SourceSpan subSpan(int beginIndex) {
    return subSpan(beginIndex, length);
  }

  /**
   * Return a span covering a sub-range of this span.
   *
   * <p>The requested range is interpreted relative to this span, not the original source. The
   * returned span shares the same line number and advances the column and absolute input indices by
   * {@code beginIndex}.
   *
   * @param beginIndex the first character to include, relative to this span
   * @param endIndex the end offset, exclusive, relative to this span
   * @return the derived span
   */
  public SourceSpan subSpan(int beginIndex, int endIndex) {
    if (beginIndex < 0) {
      throw new IndexOutOfBoundsException("beginIndex " + beginIndex + " + must be >= 0");
    }
    if (beginIndex > length) {
      throw new IndexOutOfBoundsException(
          "beginIndex " + beginIndex + " must be <= length " + length);
    }
    if (endIndex < 0) {
      throw new IndexOutOfBoundsException("endIndex " + endIndex + " + must be >= 0");
    }
    if (endIndex > length) {
      throw new IndexOutOfBoundsException("endIndex " + endIndex + " must be <= length " + length);
    }
    if (beginIndex > endIndex) {
      throw new IndexOutOfBoundsException(
          "beginIndex " + beginIndex + " must be <= endIndex " + endIndex);
    }
    if (beginIndex == 0 && endIndex == length) {
      return this;
    }
    return new SourceSpan(
        lineIndex, columnIndex + beginIndex, inputIndex + beginIndex, endIndex - beginIndex);
  }

  /** Compare spans using all stored coordinates and length. */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    SourceSpan sourceSpan = (SourceSpan) other;
    return lineIndex == sourceSpan.lineIndex
        && columnIndex == sourceSpan.columnIndex
        && inputIndex == sourceSpan.inputIndex
        && length == sourceSpan.length;
  }

  /**
   * @return a stable hash derived from all span coordinates
   */
  @Override
  public int hashCode() {
    return Objects.hash(lineIndex, columnIndex, inputIndex, length);
  }

  /**
   * @return a human-readable description of the span coordinates
   */
  @Override
  public String toString() {
    return "SourceSpan{"
        + "line="
        + lineIndex
        + ", column="
        + columnIndex
        + ", input="
        + inputIndex
        + ", length="
        + length
        + "}";
  }
}
