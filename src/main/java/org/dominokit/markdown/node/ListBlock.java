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
 * Base class for list blocks.
 *
 * <p>List blocks carry the tight/loose state shared by ordered and unordered lists.
 */
public abstract class ListBlock extends Block {

  private boolean tight;

  /** @return whether the list is tight */
  public boolean isTight() {
    return tight;
  }

  /** Set whether the list is tight. */
  public void setTight(boolean tight) {
    this.tight = tight;
  }
}
