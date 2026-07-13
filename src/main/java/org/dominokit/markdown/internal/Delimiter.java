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
package org.dominokit.markdown.internal;

import java.util.List;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.delimiter.DelimiterRun;

/**
 * Delimiter run state for emphasis, strong emphasis, or custom delimiter processors.
 *
 * <p>The parser keeps each run as a linked list node so it can remove consumed delimiters without
 * rebuilding the entire inline sequence.
 */
public class Delimiter implements DelimiterRun {

  /** Text nodes that make up this delimiter run, ordered from closer to opener. */
  public final List<Text> characters;

  /** The delimiter character used by the run. */
  public final char delimiterChar;

  /** Original length of the delimiter run. */
  private final int originalLength;

  /** Whether this run can open emphasis. */
  private final boolean canOpen;

  /** Whether this run can close emphasis. */
  private final boolean canClose;

  /** Previous delimiter in the stack. */
  public Delimiter previous;

  /** Next delimiter in the stack. */
  public Delimiter next;

  /**
   * Create a delimiter run.
   *
   * @param characters text nodes that make up the run
   * @param delimiterChar delimiter character
   * @param canOpen whether the run can open
   * @param canClose whether the run can close
   * @param previous previous delimiter in the stack
   */
  public Delimiter(
      List<Text> characters,
      char delimiterChar,
      boolean canOpen,
      boolean canClose,
      Delimiter previous) {
    this.characters = characters;
    this.delimiterChar = delimiterChar;
    this.canOpen = canOpen;
    this.canClose = canClose;
    this.previous = previous;
    this.originalLength = characters.size();
  }

  /** @return whether this delimiter run can open emphasis */
  @Override
  public boolean canOpen() {
    return canOpen;
  }

  /** @return whether this delimiter run can close emphasis */
  @Override
  public boolean canClose() {
    return canClose;
  }

  /** @return the number of delimiter characters remaining for processing */
  @Override
  public int length() {
    return characters.size();
  }

  /** @return the original number of delimiters in this run */
  @Override
  public int originalLength() {
    return originalLength;
  }

  /** @return the opener text node for this run */
  @Override
  public Text getOpener() {
    return characters.get(characters.size() - 1);
  }

  /** @return the closer text node for this run */
  @Override
  public Text getCloser() {
    return characters.get(0);
  }

  /**
   * Return the subset of opener text nodes that should be consumed.
   *
   * @param length number of delimiters to take from the opening side
   * @return iterable view of the opener text nodes
   */
  @Override
  public Iterable<Text> getOpeners(int length) {
    if (!(length >= 1 && length <= length())) {
      throw new IllegalArgumentException(
          "length must be between 1 and " + length() + ", was " + length);
    }

    return characters.subList(characters.size() - length, characters.size());
  }

  /**
   * Return the subset of closer text nodes that should be consumed.
   *
   * @param length number of delimiters to take from the closing side
   * @return iterable view of the closer text nodes
   */
  @Override
  public Iterable<Text> getClosers(int length) {
    if (!(length >= 1 && length <= length())) {
      throw new IllegalArgumentException(
          "length must be between 1 and " + length() + ", was " + length);
    }

    return characters.subList(0, length);
  }
}
