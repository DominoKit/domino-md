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

/**
 * A strong emphasis span.
 *
 * <p>The delimiter string records the exact marker sequence used to open and close the span.
 */
public class StrongEmphasis extends Node implements Delimited {

  private String delimiter;

  /** Create an empty strong-emphasis node that can be populated later. */
  public StrongEmphasis() {}

  /**
   * Create a strong-emphasis node using the supplied delimiter sequence.
   *
   * @param delimiter the exact delimiter marker used in the source
   */
  public StrongEmphasis(String delimiter) {
    this.delimiter = delimiter;
  }

  /** Set the delimiter marker string. */
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  /**
   * @return the opening delimiter sequence
   */
  @Override
  public String getOpeningDelimiter() {
    return delimiter;
  }

  /**
   * @return the closing delimiter sequence
   */
  @Override
  public String getClosingDelimiter() {
    return delimiter;
  }

  /** Dispatch this strong-emphasis node to the visitor. */
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
