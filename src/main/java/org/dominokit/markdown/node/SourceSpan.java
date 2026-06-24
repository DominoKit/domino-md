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
package org.dominokit.markdown.node;

import java.util.Objects;

/** A source span references a snippet of text from the source input. */
public class SourceSpan {

  private final int lineIndex;
  private final int columnIndex;
  private final int inputIndex;
  private final int length;

  public static SourceSpan of(int line, int column, int input, int length) {
    return new SourceSpan(line, column, input, length);
  }

  @Deprecated
  public static SourceSpan of(int lineIndex, int columnIndex, int length) {
    return of(lineIndex, columnIndex, 0, length);
  }

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

  public int getLineIndex() {
    return lineIndex;
  }

  public int getColumnIndex() {
    return columnIndex;
  }

  public int getInputIndex() {
    return inputIndex;
  }

  public int getLength() {
    return length;
  }

  public SourceSpan subSpan(int beginIndex) {
    return subSpan(beginIndex, length);
  }

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

  @Override
  public int hashCode() {
    return Objects.hash(lineIndex, columnIndex, inputIndex, length);
  }

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
