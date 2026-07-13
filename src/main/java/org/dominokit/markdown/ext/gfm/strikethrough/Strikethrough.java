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
package org.dominokit.markdown.ext.gfm.strikethrough;

import org.dominokit.markdown.node.CustomNode;
import org.dominokit.markdown.node.Delimited;

/**
 * Strikethrough custom node.
 *
 * <p>The node stores the delimiter sequence used to open and close the wrapper so Markdown
 * renderers can reproduce the original syntax when possible.
 */
public class Strikethrough extends CustomNode implements Delimited {

  private final String delimiter;

  /**
   * Create a strikethrough node with the supplied delimiter.
   *
   * @param delimiter delimiter text used for both the opening and closing wrapper
   */
  public Strikethrough(String delimiter) {
    this.delimiter = delimiter;
  }

  /** @return the opening delimiter sequence */
  @Override
  public String getOpeningDelimiter() {
    return delimiter;
  }

  /** @return the closing delimiter sequence */
  @Override
  public String getClosingDelimiter() {
    return delimiter;
  }
}
