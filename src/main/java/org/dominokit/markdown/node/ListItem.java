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
 * A list item block.
 *
 * <p>List items keep track of marker and content indentation so the parser and renderers can
 * preserve the original list layout when needed.
 */
public class ListItem extends Block {

  private Integer markerIndent;
  private Integer contentIndent;

  /** Dispatch this list item to the visitor. */
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  /** @return the indentation used by the list marker, or {@code null} if unknown */
  public Integer getMarkerIndent() {
    return markerIndent;
  }

  /** Set the indentation used by the list marker. */
  public void setMarkerIndent(Integer markerIndent) {
    this.markerIndent = markerIndent;
  }

  /** @return the indentation where the list content begins, or {@code null} if unknown */
  public Integer getContentIndent() {
    return contentIndent;
  }

  /** Set the indentation where the list content begins. */
  public void setContentIndent(Integer contentIndent) {
    this.contentIndent = contentIndent;
  }

  @Override
  @Deprecated
  public void appendChild(Node child) {
    super.appendChild(child);
  }

  /** Append a block child to this list item. */
  public void appendChild(Block child) {
    super.appendChild(child);
  }
}
