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
 * A thematic break.
 *
 * <p>The literal field stores the exact marker text that was recognized in the source.
 */
public class ThematicBreak extends Block {

  private String literal;

  /** Dispatch this thematic break to the visitor. */
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  /**
   * @return the literal marker text
   */
  public String getLiteral() {
    return literal;
  }

  /** Set the literal marker text. */
  public void setLiteral(String literal) {
    this.literal = literal;
  }
}
