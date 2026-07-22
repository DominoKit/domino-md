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
package org.dominokit.markdown.internal.inline;

import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.parser.beta.LinkResult;
import org.dominokit.markdown.parser.beta.Position;

/**
 * Concrete link-processing result.
 *
 * <p>The result stores whether the processor wants to wrap or replace the parsed link text, the
 * node to apply, the scanner position to resume from, and whether an image/link marker should be
 * included in the final decision.
 */
public class LinkResultImpl implements LinkResult {
  @Override
  /** Include the marker in the processing decision. */
  public LinkResult includeMarker() {
    includeMarker = true;
    return this;
  }

  /** Link-processing result types. */
  public enum Type {
    WRAP,
    REPLACE
  }

  /** Requested link-processing action. */
  private final Type type;

  /** Node produced by the processor. */
  private final Node node;

  /** Scanner position to resume from. */
  private final Position position;

  /** Whether the marker should be included in the processing decision. */
  private boolean includeMarker = false;

  /**
   * Create a link-processing result.
   *
   * @param type requested action
   * @param node node produced by the processor
   * @param position scanner position to resume from
   */
  public LinkResultImpl(Type type, Node node, Position position) {
    this.type = type;
    this.node = node;
    this.position = position;
  }

  /**
   * @return the requested action
   */
  public Type getType() {
    return type;
  }

  /**
   * @return the node produced by the processor
   */
  public Node getNode() {
    return node;
  }

  /**
   * @return the scanner position to resume from
   */
  public Position getPosition() {
    return position;
  }

  /**
   * @return whether the marker should be included in the processing decision
   */
  public boolean isIncludeMarker() {
    return includeMarker;
  }
}
