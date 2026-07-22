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
package org.dominokit.markdown.parser.beta;

import java.util.List;
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.SourceLines;
import org.dominokit.markdown.text.CharMatcher;

/**
 * Scanner over a sequence of source lines.
 *
 * <p>The scanner hides the distinction between line boundaries and character positions so inline
 * parsing can reason in terms of a single cursor with save/restore support.
 */
public class Scanner {

  /**
   * Character representing the end of input source (or outside of the text in case of the
   * "previous" methods).
   *
   * <p>Note that we can use NULL to represent this because CommonMark does not allow those in the
   * input (we replace them in the beginning of parsing).
   */
  public static final char END = '\0';

  // Lines without newlines at the end. The scanner will yield `\n` between lines because they're
  // significant for
  // parsing and the final output. There is no `\n` after the last line.
  private final List<SourceLine> lines;
  // Which line we're at.
  private int lineIndex;
  // The index within the line. If index == length(), we pretend that there's a `\n` and only
  // advance after we yield
  // that.
  private int index;

  // Current line or "" if at the end of the lines (using "" instead of null saves a null check)
  private SourceLine line = SourceLine.of("", null);
  private int lineLength = 0;

  /**
   * Create a scanner positioned at a specific line and character index.
   *
   * @param lines source lines to scan
   * @param lineIndex line index to start on
   * @param index character index within the line
   */
  Scanner(List<SourceLine> lines, int lineIndex, int index) {
    this.lines = lines;
    this.lineIndex = lineIndex;
    this.index = index;
    if (!lines.isEmpty()) {
      checkPosition(lineIndex, index);
      setLine(lines.get(lineIndex));
    }
  }

  /** Create a scanner positioned at the beginning of the supplied lines. */
  public static Scanner of(SourceLines lines) {
    return new Scanner(lines.getLines(), 0, 0);
  }

  /**
   * Return the current character, treating line boundaries as explicit newline characters.
   *
   * <p>At the end of the final line this returns {@link #END} instead of a newline.
   *
   * @return the current character or {@link #END}
   */
  public char peek() {
    if (index < lineLength) {
      return line.getContent().charAt(index);
    } else {
      if (lineIndex < lines.size() - 1) {
        return '\n';
      } else {
        // Don't return newline for end of last line
        return END;
      }
    }
  }

  /**
   * Return the current Unicode code point, honoring surrogate pairs when present.
   *
   * @return the current code point or {@link #END}
   */
  public int peekCodePoint() {
    if (index < lineLength) {
      char c = line.getContent().charAt(index);
      if (Character.isHighSurrogate(c) && index + 1 < lineLength) {
        char low = line.getContent().charAt(index + 1);
        if (Character.isLowSurrogate(low)) {
          return Character.toCodePoint(c, low);
        }
      }
      return c;
    } else {
      if (lineIndex < lines.size() - 1) {
        return '\n';
      } else {
        // Don't return newline for end of last line
        return END;
      }
    }
  }

  /**
   * Return the previous Unicode code point, or {@link #END} before the start of input.
   *
   * @return the previous code point or {@link #END}
   */
  public int peekPreviousCodePoint() {
    if (index > 0) {
      int prev = index - 1;
      char c = line.getContent().charAt(prev);
      if (Character.isLowSurrogate(c) && prev > 0) {
        char high = line.getContent().charAt(prev - 1);
        if (Character.isHighSurrogate(high)) {
          return Character.toCodePoint(high, c);
        }
      }
      return c;
    } else {
      if (lineIndex > 0) {
        return '\n';
      } else {
        return END;
      }
    }
  }

  /**
   * @return whether advancing would still remain within the input
   */
  public boolean hasNext() {
    if (index < lineLength) {
      return true;
    } else {
      // No newline at end of last line
      return lineIndex < lines.size() - 1;
    }
  }

  /** Advance the scanner by one character or across a line boundary. */
  public void next() {
    index++;
    if (index > lineLength) {
      lineIndex++;
      if (lineIndex < lines.size()) {
        setLine(lines.get(lineIndex));
      } else {
        setLine(SourceLine.of("", null));
      }
      index = 0;
    }
  }

  /**
   * Check if the specified char is next and advance the position.
   *
   * @param c the char to check, including newline characters
   * @return true if matched and position was advanced, false otherwise
   */
  public boolean next(char c) {
    if (peek() == c) {
      next();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Check if we have the specified content on the line and advanced the position. Note that if you
   * want to match newline characters, use {@link #next(char)}.
   *
   * @param content the text content to match on a single line, excluding newline characters
   * @return true if matched and position was advanced, false otherwise
   */
  public boolean next(String content) {
    if (index < lineLength && index + content.length() <= lineLength) {
      // Can't use startsWith because it's not available on CharSequence
      for (int i = 0; i < content.length(); i++) {
        if (line.getContent().charAt(index + i) != content.charAt(i)) {
          return false;
        }
      }
      index += content.length();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Consume and count consecutive occurrences of the supplied character.
   *
   * @param c character to match
   * @return number of matched characters
   */
  public int matchMultiple(char c) {
    int count = 0;
    while (peek() == c) {
      count++;
      next();
    }
    return count;
  }

  /**
   * Consume and count consecutive characters matching the supplied matcher.
   *
   * @param matcher character matcher
   * @return number of matched characters
   */
  public int match(CharMatcher matcher) {
    int count = 0;
    while (matcher.matches(peek())) {
      count++;
      next();
    }
    return count;
  }

  /**
   * Consume and count consecutive whitespace characters.
   *
   * @return number of whitespace characters consumed
   */
  public int whitespace() {
    int count = 0;
    while (true) {
      switch (peek()) {
        case ' ':
        case '\t':
        case '\n':
        case '\u000B':
        case '\f':
        case '\r':
          count++;
          next();
          break;
        default:
          return count;
      }
    }
  }

  /**
   * Find the next occurrence of {@code c}, returning its relative offset or {@code -1}.
   *
   * @param c character to find
   * @return relative offset to the next occurrence, or {@code -1}
   */
  public int find(char c) {
    int count = 0;
    while (true) {
      char cur = peek();
      if (cur == Scanner.END) {
        return -1;
      } else if (cur == c) {
        return count;
      }
      count++;
      next();
    }
  }

  /**
   * Find the next character that matches the supplied matcher.
   *
   * @param matcher character matcher
   * @return relative offset to the next match, or {@code -1}
   */
  public int find(CharMatcher matcher) {
    int count = 0;
    while (true) {
      char c = peek();
      if (c == END) {
        return -1;
      } else if (matcher.matches(c)) {
        return count;
      }
      count++;
      next();
    }
  }

  // Don't expose the int index, because it would be good if we could switch input to a List<String>
  // of lines later
  // instead of one contiguous String.
  /**
   * @return the current scanner position
   */
  public Position position() {
    return new Position(lineIndex, index);
  }

  /**
   * Restore the scanner to a previously saved position.
   *
   * @param position position to restore
   */
  public void setPosition(Position position) {
    checkPosition(position.lineIndex, position.index);
    this.lineIndex = position.lineIndex;
    this.index = position.index;
    setLine(lines.get(this.lineIndex));
  }

  // For cases where the caller appends the result to a StringBuilder, we could offer another method
  // to avoid some
  // unnecessary copying.
  /**
   * Extract the source between two positions.
   *
   * <p>The returned source lines preserve line breaks and source spans for the selected range.
   *
   * @param begin inclusive start position
   * @param end exclusive end position
   * @return source lines representing the requested slice
   */
  public SourceLines getSource(Position begin, Position end) {
    if (begin.lineIndex == end.lineIndex) {
      // Shortcut for common case of text from a single line
      SourceLine line = lines.get(begin.lineIndex);
      CharSequence newContent = line.getContent().subSequence(begin.index, end.index);
      SourceSpan newSourceSpan = null;
      SourceSpan sourceSpan = line.getSourceSpan();
      if (sourceSpan != null) {
        newSourceSpan = sourceSpan.subSpan(begin.index, end.index);
      }
      return SourceLines.of(SourceLine.of(newContent, newSourceSpan));
    } else {
      SourceLines sourceLines = SourceLines.empty();

      SourceLine firstLine = lines.get(begin.lineIndex);
      sourceLines.addLine(firstLine.substring(begin.index, firstLine.getContent().length()));

      // Lines between begin and end (we are appending the full line)
      for (int line = begin.lineIndex + 1; line < end.lineIndex; line++) {
        sourceLines.addLine(lines.get(line));
      }

      SourceLine lastLine = lines.get(end.lineIndex);
      sourceLines.addLine(lastLine.substring(0, end.index));
      return sourceLines;
    }
  }

  /**
   * Set the scanner's current line and cache its length.
   *
   * @param line current source line
   */
  private void setLine(SourceLine line) {
    this.line = line;
    this.lineLength = line.getContent().length();
  }

  /**
   * Validate that a saved position refers to a legal line and character offset.
   *
   * @param lineIndex line index to validate
   * @param index character index to validate
   */
  private void checkPosition(int lineIndex, int index) {
    if (lineIndex < 0 || lineIndex >= lines.size()) {
      throw new IllegalArgumentException(
          "Line index " + lineIndex + " out of range, number of lines: " + lines.size());
    }
    SourceLine line = lines.get(lineIndex);
    if (index < 0 || index > line.getContent().length()) {
      throw new IllegalArgumentException(
          "Index " + index + " out of range, line length: " + line.getContent().length());
    }
  }
}
