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
 * An unordered list.
 *
 * <p>The marker stores the exact bullet token used in the source, such as {@code -}, {@code *}, or
 * {@code +}.
 */
public class BulletList extends ListBlock {

  private String marker;

  /** Dispatch this bullet list to the visitor. */
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  /**
   * @return the bullet marker token, or {@code null}
   */
  public String getMarker() {
    return marker;
  }

  /** Set the bullet marker token. */
  public void setMarker(String marker) {
    this.marker = marker;
  }

  /**
   * @return the bullet marker as a single character, or {@code '\0'} if not available
   * @deprecated use {@link #getMarker()} instead
   */
  @Deprecated
  public char getBulletMarker() {
    return marker != null && !marker.isEmpty() ? marker.charAt(0) : '\0';
  }

  /**
   * Set the bullet marker from a single character.
   *
   * @deprecated use {@link #setMarker(String)} instead
   */
  @Deprecated
  public void setBulletMarker(char bulletMarker) {
    this.marker = bulletMarker != '\0' ? String.valueOf(bulletMarker) : null;
  }
}
