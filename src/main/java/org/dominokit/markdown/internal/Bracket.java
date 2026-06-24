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

import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.beta.Position;

/** Opening bracket for links ({@code [}), images ({@code ![}), or links with other markers. */
public class Bracket {

  /** The node of a marker such as {@code !} if present, null otherwise. */
  public final Text markerNode;

  /** The position of the marker if present, null otherwise. */
  public final Position markerPosition;

  /** The node of {@code [}. */
  public final Text bracketNode;

  /** The position of {@code [}. */
  public final Position bracketPosition;

  /** The position of the content (after the opening bracket) */
  public final Position contentPosition;

  /** Previous bracket. */
  public final Bracket previous;

  /** Previous delimiter (emphasis, etc) before this bracket. */
  public final Delimiter previousDelimiter;

  /** Whether this bracket is allowed to form a link/image (also known as "active"). */
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
