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

/** An ordered list. */
public class OrderedList extends ListBlock {

  private String markerDelimiter;
  private Integer markerStartNumber;

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  public Integer getMarkerStartNumber() {
    return markerStartNumber;
  }

  public void setMarkerStartNumber(Integer markerStartNumber) {
    this.markerStartNumber = markerStartNumber;
  }

  public String getMarkerDelimiter() {
    return markerDelimiter;
  }

  public void setMarkerDelimiter(String markerDelimiter) {
    this.markerDelimiter = markerDelimiter;
  }

  @Deprecated
  public int getStartNumber() {
    return markerStartNumber != null ? markerStartNumber : 0;
  }

  @Deprecated
  public void setStartNumber(int startNumber) {
    this.markerStartNumber = startNumber != 0 ? startNumber : null;
  }

  @Deprecated
  public char getDelimiter() {
    return markerDelimiter != null && !markerDelimiter.isEmpty() ? markerDelimiter.charAt(0) : '\0';
  }

  @Deprecated
  public void setDelimiter(char delimiter) {
    this.markerDelimiter = delimiter != '\0' ? String.valueOf(delimiter) : null;
  }
}
