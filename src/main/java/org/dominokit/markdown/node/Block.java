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

/** Block nodes such as paragraphs, list blocks, code blocks, and quotes. */
public abstract class Block extends Node {

  @Override
  /**
   * @return the parent block, or {@code null} for the root
   */
  public Block getParent() {
    return (Block) super.getParent();
  }

  @Override
  /**
   * Enforce that block parents are also block nodes.
   *
   * <p>Block nodes cannot be attached directly under inline parents, because block-level parsing
   * and rendering assumes that ancestry stays within the block hierarchy.
   *
   * @param parent the candidate parent node
   */
  protected void setParent(Node parent) {
    if (!(parent instanceof Block)) {
      throw new IllegalArgumentException("Parent of block must also be block (can not be inline)");
    }
    super.setParent(parent);
  }
}
