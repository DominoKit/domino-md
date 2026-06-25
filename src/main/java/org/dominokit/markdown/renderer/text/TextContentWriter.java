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

public class TextContentWriter {

  private final Appendable buffer;
  private final LineBreakRendering lineBreakRendering;
  private final LinkedList<String> prefixes = new LinkedList<>();
  private final LinkedList<Boolean> tight = new LinkedList<>();

  private String blockSeparator;
  private char lastChar;

  public TextContentWriter(Appendable out) {
    this(out, LineBreakRendering.COMPACT);
  }

  public TextContentWriter(Appendable out, LineBreakRendering lineBreakRendering) {
    this.buffer = Objects.requireNonNull(out, "out must not be null");
    this.lineBreakRendering =
        Objects.requireNonNull(lineBreakRendering, "lineBreakRendering must not be null");
  }

  public void whitespace() {
    if (lastChar != 0 && lastChar != ' ') {
      write(' ');
    }
  }

  public void colon() {
    if (lastChar != 0 && lastChar != ':') {
      write(':');
    }
  }

  public void line() {
    append('\n');
    writePrefixes();
  }

  public void block() {
    if (lineBreakRendering == LineBreakRendering.STRIP) {
      blockSeparator = " ";
    } else if (lineBreakRendering == LineBreakRendering.COMPACT || isTight()) {
      blockSeparator = "\n";
    } else {
      blockSeparator = "\n\n";
    }
  }

  public void resetBlock() {
    blockSeparator = null;
  }

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

  public void write(String s) {
    flushBlockSeparator();
    append(s);
  }

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

  /** Remove the last prefix from the stack. */
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

  private boolean isTight() {
    return !tight.isEmpty() && tight.getLast();
  }

  private void writePrefixes() {
    for (String prefix : prefixes) {
      append(prefix);
    }
  }

  /** If a block separator has been queued but not written yet, write it now. */
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

  private void appendCodePoint(int codePoint) {
    append(new String(Character.toChars(codePoint)));
  }

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

  private void append(char c) {
    try {
      buffer.append(c);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    lastChar = c;
  }
}
