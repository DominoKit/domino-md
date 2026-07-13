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

/**
 * Literal text content.
 *
 * <p>Text nodes are the leaf nodes that carry unformatted character data after the inline parser
 * has resolved all special syntax.
 */
public class Text extends Node {

  private String literal;

  /** Create an empty text node that can be populated later. */
  public Text() {}

  /**
   * Create a text node with the supplied literal content.
   *
   * @param literal literal text content
   */
  public Text(String literal) {
    this.literal = literal;
  }

  /** Dispatch this text node to the visitor. */
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  /** @return the literal text content */
  public String getLiteral() {
    return literal;
  }

  /** Replace the literal text content. */
  public void setLiteral(String literal) {
    this.literal = literal;
  }

  /** Include the literal value in debug output. */
  @Override
  protected String toStringAttributes() {
    return "literal=" + literal;
  }
}
