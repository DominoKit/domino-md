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
 * An inline code span.
 *
 * <p>Code spans store the literal text content after backtick delimiter processing but before any
 * HTML escaping by renderers.
 */
public class Code extends Node {

  private String literal;

  /** Create an empty code span that can be populated later. */
  public Code() {}

  /**
   * Create a code span with the supplied literal text.
   *
   * @param literal literal code content
   */
  public Code(String literal) {
    this.literal = literal;
  }

  /** Dispatch this code span to the visitor. */
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  /**
   * @return the literal code content
   */
  public String getLiteral() {
    return literal;
  }

  /** Set the literal code content. */
  public void setLiteral(String literal) {
    this.literal = literal;
  }
}
