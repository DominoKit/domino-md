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
package org.dominokit.markdown.renderer.markdown;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;
import org.dominokit.markdown.text.CharMatcher;

/** Writer for canonical Markdown text. */
public class MarkdownWriter {

  private final Appendable buffer;

  private int blockSeparator;
  private char lastChar;
  private boolean atLineStart = true;
  private final LinkedList<String> prefixes = new LinkedList<>();
  private final LinkedList<Boolean> tight = new LinkedList<>();
  private final LinkedList<CharMatcher> rawEscapes = new LinkedList<>();

  public MarkdownWriter(Appendable out) {
    this.buffer = Objects.requireNonNull(out, "out must not be null");
  }

  /** Write raw text, still honoring scoped raw escape matchers. */
  public void raw(String s) {
    flushBlockSeparator();
    write(s, null);
  }

  /** Write a raw character, still honoring scoped raw escape matchers. */
  public void raw(char c) {
    flushBlockSeparator();
    write(c);
  }

  /**
   * Write escaped text.
   *
   * @param s the string to write
   * @param escape which characters to escape
   */
  public void text(String s, CharMatcher escape) {
    if (s.isEmpty()) {
      return;
    }
    flushBlockSeparator();
    write(s, escape);
  }

  /** Write a newline and re-emit the active prefixes. */
  public void line() {
    write('\n');
    writePrefixes();
    atLineStart = true;
  }

  /** Queue a tight or loose block separator before the next write. */
  public void block() {
    blockSeparator = isTight() ? 1 : 2;
    atLineStart = true;
  }

  /** Push a prefix to be written at the beginning of each line. */
  public void pushPrefix(String prefix) {
    prefixes.addLast(prefix);
  }

  /** Write a prefix immediately without changing the external line-start state. */
  public void writePrefix(String prefix) {
    boolean previousAtLineStart = atLineStart;
    raw(prefix);
    atLineStart = previousAtLineStart;
  }

  /** Remove the last prefix from the stack. */
  public void popPrefix() {
    prefixes.removeLast();
  }

  /** Push whether nested blocks should render as tight. */
  public void pushTight(boolean tight) {
    this.tight.addLast(tight);
  }

  /** Pop the last tight-mode setting. */
  public void popTight() {
    this.tight.removeLast();
  }

  /**
   * Escape the supplied raw characters for all nested writes, including code spans or other raw
   * output.
   */
  public void pushRawEscape(CharMatcher rawEscape) {
    rawEscapes.add(Objects.requireNonNull(rawEscape, "rawEscape must not be null"));
  }

  /** Pop the last scoped raw escape matcher. */
  public void popRawEscape() {
    rawEscapes.removeLast();
  }

  /** @return the last character that was written */
  public char getLastChar() {
    return lastChar;
  }

  /** @return whether the next write is at the logical start of the line, excluding prefixes */
  public boolean isAtLineStart() {
    return atLineStart;
  }

  private void write(String s, CharMatcher escape) {
    try {
      if (rawEscapes.isEmpty() && escape == null) {
        buffer.append(s);
      } else {
        for (int i = 0; i < s.length(); i++) {
          append(s.charAt(i), escape);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (!s.isEmpty()) {
      lastChar = s.charAt(s.length() - 1);
    }
    atLineStart = false;
  }

  private void write(char c) {
    try {
      append(c, null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    lastChar = c;
    atLineStart = false;
  }

  private void writePrefixes() {
    for (String prefix : prefixes) {
      write(prefix, null);
    }
  }

  private void flushBlockSeparator() {
    if (blockSeparator == 0) {
      return;
    }

    write('\n');
    writePrefixes();
    if (blockSeparator > 1) {
      write('\n');
      writePrefixes();
    }
    blockSeparator = 0;
  }

  private void append(char c, CharMatcher escape) throws IOException {
    if (needsEscaping(c, escape)) {
      if (c == '\n') {
        buffer.append("&#10;");
      } else {
        buffer.append('\\');
        buffer.append(c);
      }
    } else {
      buffer.append(c);
    }
  }

  private boolean isTight() {
    return !tight.isEmpty() && tight.getLast();
  }

  private boolean needsEscaping(char c, CharMatcher escape) {
    return (escape != null && escape.matches(c)) || rawNeedsEscaping(c);
  }

  private boolean rawNeedsEscaping(char c) {
    for (CharMatcher rawEscape : rawEscapes) {
      if (rawEscape.matches(c)) {
        return true;
      }
    }
    return false;
  }
}
