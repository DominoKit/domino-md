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
package org.dominokit.markdown.parser.delimiter;

import org.dominokit.markdown.node.Text;

/** A delimiter run is one or more of the same delimiter character, e.g. {@code ***}. */
public interface DelimiterRun {

  /** @return whether this can open a delimiter */
  boolean canOpen();

  /** @return whether this can close a delimiter */
  boolean canClose();

  /** @return the number of characters in this delimiter run (that are left for processing) */
  int length();

  /**
   * @return the number of characters originally in this delimiter run; at the start of processing,
   *     this is the same as {{@link #length()}}
   */
  int originalLength();

  /** @return the innermost opening delimiter, e.g. for {@code ***} this is the last {@code *} */
  Text getOpener();

  /** @return the innermost closing delimiter, e.g. for {@code ***} this is the first {@code *} */
  Text getCloser();

  /**
   * Get the opening delimiter nodes for the specified length of delimiters. Length must be between
   * 1 and {@link #length()}.
   *
   * <p>For example, for a delimiter run {@code ***}, calling this with 1 would return the last
   * {@code *}. Calling it with 2 would return the second last {@code *} and the last {@code *}.
   */
  Iterable<Text> getOpeners(int length);

  /**
   * Get the closing delimiter nodes for the specified length of delimiters. Length must be between
   * 1 and {@link #length()}.
   *
   * <p>For example, for a delimiter run {@code ***}, calling this with 1 would return the first
   * {@code *}. Calling it with 2 would return the first {@code *} and the second {@code *}.
   */
  Iterable<Text> getClosers(int length);
}
