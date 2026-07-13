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
package org.dominokit.markdown.renderer.text;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;
import org.dominokit.markdown.text.Characters;

/**
 * Writer for plain-text rendering.
 *
 * <p>The writer collapses whitespace, preserves only the minimal punctuation needed for readable
 * output, and tracks block/prefix state so nested structures such as lists and quotes remain
 * legible.
 */
public class TextContentWriter {

  private final Appendable buffer;
  private final LineBreakRendering lineBreakRendering;
  private final LinkedList<String> prefixes = new LinkedList<>();
  private final LinkedList<Boolean> tight = new LinkedList<>();

  private String blockSeparator;
  private char lastChar;

  /**
   * Create a writer using the default compact line-break mode.
   *
   * @param out destination for generated text
   */
  public TextContentWriter(Appendable out) {
    this(out, LineBreakRendering.COMPACT);
  }

  /**
   * Create a writer with an explicit line-break mode.
   *
   * @param out destination for generated text
   * @param lineBreakRendering policy for block separators and line breaks
   */
  public TextContentWriter(Appendable out, LineBreakRendering lineBreakRendering) {
    this.buffer = Objects.requireNonNull(out, "out must not be null");
    this.lineBreakRendering =
        Objects.requireNonNull(lineBreakRendering, "lineBreakRendering must not be null");
  }

  /** Write a single collapsed whitespace character if the last output character requires it. */
  public void whitespace() {
    if (lastChar != 0 && lastChar != ' ') {
      write(' ');
    }
  }

  /** Write a colon if the previous character is not already a colon. */
  public void colon() {
    if (lastChar != 0 && lastChar != ':') {
      write(':');
    }
  }

  /** Write a newline and re-emit any active prefixes. */
  public void line() {
    append('\n');
    writePrefixes();
  }

  /**
   * Queue a block separator according to the current line-break mode and nesting tightness.
   *
   * <p>The actual separator is written lazily so adjacent block writes can still collapse or
   * expand spacing based on the surrounding content.
   */
  public void block() {
    if (lineBreakRendering == LineBreakRendering.STRIP) {
      blockSeparator = " ";
    } else if (lineBreakRendering == LineBreakRendering.COMPACT || isTight()) {
      blockSeparator = "\n";
    } else {
      blockSeparator = "\n\n";
    }
  }

  /** Clear any pending block separator. */
  public void resetBlock() {
    blockSeparator = null;
  }

  /**
   * Write text while collapsing internal whitespace to a single space.
   *
   * <p>This is the normal path for plain text output, because inline rendering often wants to
   * preserve words without preserving source formatting.
   */
  public void writeStripped(String s) {
    flushBlockSeparator();
    boolean pendingWhitespace = false;
    int length = s.length();
    for (int index = 0; index < length; ) {
      int codePoint = Character.codePointAt(s, index);
      if (Characters.isWhitespaceCodePoint(codePoint)) {
        pendingWhitespace = true;
      } else {
        if (pendingWhitespace && lastChar != 0 && lastChar != ' ') {
          append(' ');
        }
        appendCodePoint(codePoint);
        pendingWhitespace = false;
      }
      index += Character.charCount(codePoint);
    }
  }

  /** Write text without stripping or collapsing whitespace. */
  public void write(String s) {
    flushBlockSeparator();
    append(s);
  }

  /** Write a single raw character. */
  public void write(char c) {
    flushBlockSeparator();
    append(c);
  }

  /**
   * Push a prefix onto the top of the stack. All prefixes are written at the beginning of each line
   * until the prefix is popped again.
   *
   * @param prefix the prefix string
   */
  public void pushPrefix(String prefix) {
    prefixes.addLast(prefix);
  }

  /**
   * Write a prefix directly.
   *
   * @param prefix the prefix to write
   */
  public void writePrefix(String prefix) {
    write(prefix);
  }

  /** Remove the most recently pushed prefix. */
  public void popPrefix() {
    prefixes.removeLast();
  }

  /** Push whether upcoming blocks should be rendered as tight blocks. */
  public void pushTight(boolean tight) {
    this.tight.addLast(tight);
  }

  /** Remove the last tight-mode setting from the stack. */
  public void popTight() {
    this.tight.removeLast();
  }

  /** @return whether the current nested block context is tight */
  private boolean isTight() {
    return !tight.isEmpty() && tight.getLast();
  }

  /** Write every currently active prefix to the output. */
  private void writePrefixes() {
    for (String prefix : prefixes) {
      append(prefix);
    }
  }

  /**
   * If a block separator has been queued but not written yet, write it now.
   *
   * <p>Line-based separators are emitted character by character so active prefixes can be inserted
   * after each newline.
   */
  private void flushBlockSeparator() {
    if (blockSeparator == null) {
      return;
    }

    if ("\n".equals(blockSeparator) || "\n\n".equals(blockSeparator)) {
      for (int i = 0; i < blockSeparator.length(); i++) {
        append(blockSeparator.charAt(i));
        writePrefixes();
      }
    } else {
      append(blockSeparator);
    }
    blockSeparator = null;
  }

  /**
   * Append a Unicode code point by converting it to the corresponding UTF-16 sequence.
   *
   * @param codePoint Unicode code point to append
   */
  private void appendCodePoint(int codePoint) {
    append(new String(Character.toChars(codePoint)));
  }

  /**
   * Append a string to the underlying buffer.
   *
   * @param s text to append verbatim
   */
  private void append(String s) {
    try {
      buffer.append(s);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (!s.isEmpty()) {
      lastChar = s.charAt(s.length() - 1);
    }
  }

  /**
   * Append a single character to the underlying buffer.
   *
   * @param c character to append verbatim
   */
  private void append(char c) {
    try {
      buffer.append(c);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    lastChar = c;
  }
}
