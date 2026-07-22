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
 * An ordered list.
 *
 * <p>The list remembers both the delimiter character and the initial start number used in the
 * source.
 */
public class OrderedList extends ListBlock {

  private String markerDelimiter;
  private Integer markerStartNumber;

  /** Dispatch this ordered list to the visitor. */
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  /**
   * @return the starting marker number, or {@code null}
   */
  public Integer getMarkerStartNumber() {
    return markerStartNumber;
  }

  /** Set the starting marker number. */
  public void setMarkerStartNumber(Integer markerStartNumber) {
    this.markerStartNumber = markerStartNumber;
  }

  /**
   * @return the delimiter token, or {@code null}
   */
  public String getMarkerDelimiter() {
    return markerDelimiter;
  }

  /** Set the delimiter token. */
  public void setMarkerDelimiter(String markerDelimiter) {
    this.markerDelimiter = markerDelimiter;
  }

  /**
   * @return the start number, or {@code 0} when unset
   * @deprecated use {@link #getMarkerStartNumber()} instead
   */
  @Deprecated
  public int getStartNumber() {
    return markerStartNumber != null ? markerStartNumber : 0;
  }

  /**
   * Set the start number.
   *
   * @deprecated use {@link #setMarkerStartNumber(Integer)} instead
   */
  @Deprecated
  public void setStartNumber(int startNumber) {
    this.markerStartNumber = startNumber != 0 ? startNumber : null;
  }

  /**
   * @return the delimiter character, or {@code '\0'} when unset
   * @deprecated use {@link #getMarkerDelimiter()} instead
   */
  @Deprecated
  public char getDelimiter() {
    return markerDelimiter != null && !markerDelimiter.isEmpty() ? markerDelimiter.charAt(0) : '\0';
  }

  /**
   * Set the delimiter character.
   *
   * @deprecated use {@link #setMarkerDelimiter(String)} instead
   */
  @Deprecated
  public void setDelimiter(char delimiter) {
    this.markerDelimiter = delimiter != '\0' ? String.valueOf(delimiter) : null;
  }
}
