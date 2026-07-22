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

/**
 * Stateful writer that emits canonical Markdown text.
 *
 * <p>The writer keeps track of block separators, nested line prefixes, and escape scopes so node
 * renderers can write Markdown incrementally without repeatedly re-implementing indentation and
 * escaping rules.
 */
public class MarkdownWriter {

  private final Appendable buffer;

  private int blockSeparator;
  private char lastChar;
  private boolean atLineStart = true;
  private final LinkedList<String> prefixes = new LinkedList<>();
  private final LinkedList<Boolean> tight = new LinkedList<>();
  private final LinkedList<CharMatcher> rawEscapes = new LinkedList<>();

  /**
   * Create a writer that emits Markdown into the supplied appendable.
   *
   * @param out destination for rendered Markdown
   */
  public MarkdownWriter(Appendable out) {
    this.buffer = Objects.requireNonNull(out, "out must not be null");
  }

  /**
   * Write raw text while honoring any scoped raw-escape matchers.
   *
   * <p>This is primarily used for syntax that must be reproduced verbatim, such as code spans or
   * raw HTML fragments.
   */
  public void raw(String s) {
    flushBlockSeparator();
    write(s, null);
  }

  /**
   * Write a raw character while honoring any scoped raw-escape matchers.
   *
   * @param c character to write verbatim unless it is currently escaped
   */
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

  /**
   * Write a newline and re-emit the active prefixes.
   *
   * <p>Prefixes such as block quote markers are preserved on every rendered line.
   */
  public void line() {
    write('\n');
    writePrefixes();
    atLineStart = true;
  }

  /**
   * Queue a block separator before the next write.
   *
   * <p>Tight nesting uses a single newline; loose nesting uses a blank line.
   */
  public void block() {
    blockSeparator = isTight() ? 1 : 2;
    atLineStart = true;
  }

  /** Push a prefix to be written at the beginning of each line. */
  public void pushPrefix(String prefix) {
    prefixes.addLast(prefix);
  }

  /**
   * Write a prefix immediately without changing the logical line-start state.
   *
   * <p>This is useful when a renderer needs to inject a prefix mid-line but still wants the next
   * write to behave as though the line had not been advanced yet.
   */
  public void writePrefix(String prefix) {
    boolean previousAtLineStart = atLineStart;
    raw(prefix);
    atLineStart = previousAtLineStart;
  }

  /** Remove the most recently pushed prefix. */
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

  /**
   * @return the last character that was written
   */
  public char getLastChar() {
    return lastChar;
  }

  /**
   * @return whether the next write is at the logical start of the line, excluding prefixes
   */
  public boolean isAtLineStart() {
    return atLineStart;
  }

  /**
   * Write a string with optional escaping.
   *
   * <p>If no escape rules are active, the string is appended directly for efficiency. Otherwise the
   * string is streamed character by character so both explicit and scoped escapes can be applied.
   */
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

  /** Write a single character with no explicit escaping matcher. */
  private void write(char c) {
    try {
      append(c, null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    lastChar = c;
    atLineStart = false;
  }

  /** Emit every currently active prefix in registration order. */
  private void writePrefixes() {
    for (String prefix : prefixes) {
      write(prefix, null);
    }
  }

  /** Flush any pending block separator before the next token is written. */
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

  /**
   * Append one character, escaping it when required by the active matchers.
   *
   * @param c character to append
   * @param escape explicit matcher for the current write scope, or {@code null}
   */
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

  /**
   * @return whether the current nested block context is tight
   */
  private boolean isTight() {
    return !tight.isEmpty() && tight.getLast();
  }

  /**
   * @return whether the character must be escaped in the current context
   */
  private boolean needsEscaping(char c, CharMatcher escape) {
    return (escape != null && escape.matches(c)) || rawNeedsEscaping(c);
  }

  /**
   * Check whether any scoped raw-escape matcher wants to escape the character.
   *
   * @param c character to test
   * @return {@code true} if any scoped raw-escape matcher wants to escape the character
   */
  private boolean rawNeedsEscaping(char c) {
    for (CharMatcher rawEscape : rawEscapes) {
      if (rawEscape.matches(c)) {
        return true;
      }
    }
    return false;
  }
}
