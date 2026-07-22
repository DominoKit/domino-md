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
package org.dominokit.markdown.internal;

import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.beta.Position;

/**
 * Opening bracket state used while resolving links and images.
 *
 * <p>The parser keeps a linked stack of these objects so it can match closing brackets against
 * earlier openers and decide whether a bracket sequence forms a link, image, or literal text.
 */
public class Bracket {

  /** The node of a marker such as {@code !} if present, {@code null} otherwise. */
  public final Text markerNode;

  /** The position of the marker if present, {@code null} otherwise. */
  public final Position markerPosition;

  /** The node for the opening {@code [}. */
  public final Text bracketNode;

  /** The position of the opening {@code [}. */
  public final Position bracketPosition;

  /** The position of the content, immediately after the opening bracket. */
  public final Position contentPosition;

  /** Previous bracket in the opener stack. */
  public final Bracket previous;

  /** Previous delimiter (emphasis, etc.) that was active before this bracket. */
  public final Delimiter previousDelimiter;

  /** Whether this bracket is allowed to form a link or image. */
  public boolean allowed = true;

  /**
   * Whether there is an unescaped bracket (opening or closing) after this opening bracket in the
   * text parsed so far.
   */
  public boolean bracketAfter = false;

  public static Bracket link(
      Text bracketNode,
      Position bracketPosition,
      Position contentPosition,
      Bracket previous,
      Delimiter previousDelimiter) {
    return new Bracket(
        null, null, bracketNode, bracketPosition, contentPosition, previous, previousDelimiter);
  }

  public static Bracket withMarker(
      Text markerNode,
      Position markerPosition,
      Text bracketNode,
      Position bracketPosition,
      Position contentPosition,
      Bracket previous,
      Delimiter previousDelimiter) {
    return new Bracket(
        markerNode,
        markerPosition,
        bracketNode,
        bracketPosition,
        contentPosition,
        previous,
        previousDelimiter);
  }

  private Bracket(
      Text markerNode,
      Position markerPosition,
      Text bracketNode,
      Position bracketPosition,
      Position contentPosition,
      Bracket previous,
      Delimiter previousDelimiter) {
    this.markerNode = markerNode;
    this.markerPosition = markerPosition;
    this.bracketNode = bracketNode;
    this.bracketPosition = bracketPosition;
    this.contentPosition = contentPosition;
    this.previous = previous;
    this.previousDelimiter = previousDelimiter;
  }
}
