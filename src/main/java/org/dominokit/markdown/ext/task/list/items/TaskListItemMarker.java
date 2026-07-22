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
package org.dominokit.markdown.ext.task.list.items;

import org.dominokit.markdown.node.CustomNode;

/** Marker node indicating that a list item represents a task checkbox. */
public class TaskListItemMarker extends CustomNode {

  private final boolean checked;

  /** Create a marker with the given checked state. */
  public TaskListItemMarker(boolean checked) {
    this.checked = checked;
  }

  /**
   * @return whether the task checkbox is checked
   */
  public boolean isChecked() {
    return checked;
  }
}
