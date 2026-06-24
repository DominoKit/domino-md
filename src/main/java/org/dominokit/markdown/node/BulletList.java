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

/** A bullet list. */
public class BulletList extends ListBlock {

  private String marker;

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  public String getMarker() {
    return marker;
  }

  public void setMarker(String marker) {
    this.marker = marker;
  }

  @Deprecated
  public char getBulletMarker() {
    return marker != null && !marker.isEmpty() ? marker.charAt(0) : '\0';
  }

  @Deprecated
  public void setBulletMarker(char bulletMarker) {
    this.marker = bulletMarker != '\0' ? String.valueOf(bulletMarker) : null;
  }
}
