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
package org.dominokit.markdown.ext.gfm.tables;

import org.dominokit.markdown.node.CustomNode;

/**
 * A table cell containing inline nodes.
 *
 * <p>Cells may carry header state, alignment, and width metadata parsed from the separator line.
 */
public class TableCell extends CustomNode {

  private boolean header;
  private Alignment alignment;
  private int width;

  /**
   * @return whether this cell belongs to the header row
   */
  public boolean isHeader() {
    return header;
  }

  /** Set whether this cell belongs to the header row. */
  public void setHeader(boolean header) {
    this.header = header;
  }

  /**
   * @return the parsed alignment, or {@code null}
   */
  public Alignment getAlignment() {
    return alignment;
  }

  /** Set the parsed alignment. */
  public void setAlignment(Alignment alignment) {
    this.alignment = alignment;
  }

  /**
   * @return the separator width parsed for this column
   */
  public int getWidth() {
    return width;
  }

  /** Set the separator width parsed for this column. */
  public void setWidth(int width) {
    this.width = width;
  }

  public enum Alignment {
    LEFT,
    CENTER,
    RIGHT
  }
}
